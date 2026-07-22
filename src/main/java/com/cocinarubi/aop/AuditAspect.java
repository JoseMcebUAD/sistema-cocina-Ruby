package com.cocinarubi.aop;

import com.cocinarubi.DBConstants.TipoOperacion;
import com.cocinarubi.domain.service.AuditoriaService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.aop.mixin.ClienteAuditMixin;
import com.cocinarubi.aop.mixin.PedidoAuditMixin;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

/**
 * Aspecto que intercepta automáticamente {@code @PostMapping}, {@code @PutMapping},
 * {@code @PatchMapping} y {@code @DeleteMapping} en todos los Controllers y registra
 * la operación de forma asíncrona en la tabla auditoria.
 *
 * <p>Para excluir un método o clase completa usar {@link SkipAudit}.</p>
 *
 * <p>La tabla se deriva del nombre del Controller usando conversión camelCase→snake_case:
 * "PedidoController" → "pedido", "InventarioComidaController" → "inventario_comida".</p>
 *
 * <p>Para PUT/PATCH con path variable id, se captura datos_antes: se carga la entidad
 * vía EntityManager.find() ANTES de proceed() y se serializa a JSON inmediatamente para
 * congelar el snapshot antes de que el controller mute la entidad managed.
 * Los DELETE no capturan snapshot previo por decisión del proyecto.</p>
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditoriaService auditoriaService;
    private final EntityManager entityManager;
    private final ObjectMapper auditObjectMapper;
    private final EntityClassResolver entityClassResolver;

    public AuditAspect(AuditoriaService auditoriaService,
                       EntityManager entityManager,
                       ObjectMapper objectMapper,
                       EntityClassResolver entityClassResolver) {
        this.auditoriaService = auditoriaService;
        this.entityManager = entityManager;
        this.entityClassResolver = entityClassResolver;

        // Copia del mapper global + Hibernate6Module para serializar proxies LAZY como null
        // sin romper el ObjectMapper principal ni afectar las respuestas HTTP de la API
        Hibernate6Module hm = new Hibernate6Module();
        hm.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        this.auditObjectMapper = objectMapper.copy()
                .registerModule(hm)
                .addMixIn(Cliente.class, ClienteAuditMixin.class)
                .addMixIn(Pedido.class, PedidoAuditMixin.class);
    }

    @Around("within(com.cocinarubi.presentation.controller..*) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping)   || " +
            " @annotation(org.springframework.web.bind.annotation.PutMapping)    || " +
            " @annotation(org.springframework.web.bind.annotation.PatchMapping)  || " +
            " @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object auditar(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> clazz = joinPoint.getTarget().getClass();

        boolean skip = AnnotationUtils.findAnnotation(method, SkipAudit.class) != null ||
                AnnotationUtils.findAnnotation(clazz, SkipAudit.class) != null;
        TipoOperacion operacion = skip ? null : detectarOperacion(method);
        if (operacion == null) {
            return joinPoint.proceed();
        }

        String tabla = detectarTabla(clazz.getSimpleName());
        Integer idPrevio = extraerIdDesdeArgs(joinPoint);

        String datosAntesJson = capturarSnapshot(operacion, tabla, idPrevio);

        Object resultado = joinPoint.proceed();

        try {
            Integer idRegistro = idPrevio;
            if (idRegistro == null && operacion == TipoOperacion.POST) {
                Object datos = extraerDatosRespuesta(resultado);
                if (datos != null) idRegistro = extraerIdDeObjeto(datos);
            }
            Integer idUsuario = extraerIdUsuario();
            Object datosDespues = extraerDatosRespuesta(resultado);

            auditoriaService.registrar(tabla, operacion, idRegistro, idUsuario,
                    datosAntesJson, datosDespues);
        } catch (Exception e) {
            log.error("AuditAspect: error al registrar auditoría error={}", e.getMessage());
        }

        return resultado;
    }

    // Congela el estado actual del registro en JSON. Solo aplica a PUT/PATCH (que aquí llegan como PUT).
    private String capturarSnapshot(TipoOperacion operacion, String tabla, Integer id) {
        if (id == null) return null;
        if (operacion != TipoOperacion.PUT) return null;
        try {
            Class<?> entityClass = entityClassResolver.resolver(tabla).orElse(null);
            if (entityClass == null) return null;
            Object snapshot = entityManager.find(entityClass, id);
            if (snapshot == null) return null;
            String json = auditObjectMapper.writeValueAsString(snapshot);
            entityManager.detach(snapshot);
            return json;
        } catch (Exception e) {
            log.warn("AuditAspect: no se pudo capturar snapshot tabla={} id={} error={}",
                    tabla, id, e.getMessage());
            return null;
        }
    }

    private TipoOperacion detectarOperacion(Method method) {
        if (method.isAnnotationPresent(PostMapping.class))   return TipoOperacion.POST;
        if (method.isAnnotationPresent(PutMapping.class) ||
            method.isAnnotationPresent(PatchMapping.class))  return TipoOperacion.PUT;
        if (method.isAnnotationPresent(DeleteMapping.class)) return TipoOperacion.DELETE;
        return null;
    }

    private String detectarTabla(String simpleClassName) {
        return simpleClassName
                .replace("Controller", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    private Integer extraerIdUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof com.cocinarubi.domain.entity.Usuario u) return u.getIdUsuario();
        return null;
    }

    private Integer extraerIdDesdeArgs(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Integer i) return i;
            if (arg instanceof Long l) return l.intValue();
        }
        return null;
    }

    private static final String[] GETTERS_ID = {
            "getIdPedido", "getIdComida", "getIdComplemento", "getIdBasico", "getIdDesayuno",
            "getIdCliente", "getIdProductoCocina", "getIdInventarioComida", "getIdUsuario",
            "getIdAnuncio", "getIdRuta", "getIdTarifaLluvia", "getIdFavoritoCliente",
            "getIdCodigoCliente", "getIdPagoRepartidor", "getIdHorarioAtencionComidas",
            "getIdComidaPedido", "getIdBasicoPedido", "getIdDesayunoPedido",
            "getIdBasicoComplemento", "getIdComplementoComidaPedido",
            "getIdProductoCocinaPedido", "getIdPedidoDomicilio",
            "getIdArchivo", "getIdArchivoModulo", "getIdRol", "getId"
    };

    private Integer extraerIdDeObjeto(Object obj) {
        for (String nombre : GETTERS_ID) {
            try {
                Method m = obj.getClass().getMethod(nombre);
                Object val = m.invoke(obj);
                if (val instanceof Integer i) return i;
                if (val instanceof Long l) return l.intValue();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Object extraerDatosRespuesta(Object resultado) {
        if (resultado instanceof ResponseEntity<?> re) {
            Object body = re.getBody();
            if (body instanceof ApiResponse<?> ar) {
                return ar.getData();
            }
        }
        return resultado;
    }
}
