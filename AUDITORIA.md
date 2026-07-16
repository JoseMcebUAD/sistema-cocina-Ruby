# Módulo de Auditorías AOP

Sistema de registro automático de operaciones de escritura (POST/PUT/PATCH/DELETE) a nivel de controlador, con snapshots JSON y persistencia asíncrona.

---

## Flujo de ejecución

```
HTTP Request (POST/PUT/PATCH/DELETE)
        ↓
AuditAspect [@Around]
  1. ¿Tiene @SkipAudit?  → sí: bypass sin registrar
  2. Detecta operación   → POST / PUT / DELETE
  3. Detecta tabla       → nombre del Controller → snake_case
  4. PUT/PATCH/DELETE con id: carga snapshot actual vía EntityManager → datos_antes (JSON)
  5. entityManager.detach() para no contaminar el contexto JPA
  6. joinPoint.proceed() → Controller ejecuta
  7. Extrae datos_despues del body de la respuesta
  8. Extrae idRegistro (del path variable o del objeto retornado)
  9. Extrae idUsuario del SecurityContext
        ↓
AuditoriaService.registrar() [ASYNC — pool "auditExecutor"]
  - Transacción propia (REQUIRES_NEW): fallo aquí no revierte el negocio
  - Jackson serializa datosDespues a JSON
  - AuditoriaRepository.save()
        ↓
HTTP Response retorna al cliente  ← no espera la auditoría
```

---

## Archivos del módulo

```
src/main/java/com/
├── aop/
│   ├── AuditAspect.java           # Aspecto @Around principal
│   ├── SkipAudit.java             # Anotación para excluir métodos/clases
│   └── EntityClassResolver.java   # Mapea tabla → clase @Entity
├── entity/
│   └── Auditoria.java             # Entidad JPA tabla auditoria
├── dao/
│   └── AuditoriaRepository.java   # Repo con findConFiltros() paginado
├── service/
│   ├── AuditoriaService.java      # Persistencia @Async + REQUIRES_NEW
│   └── auditoria/
│       └── AuditoriaParser.java   # Genera descripción legible por tabla
├── dto/response/
│   └── AuditoriaResponseDTO.java  # DTO de salida
├── Controller/
│   └── AuditoriaController.java   # GET /auditoria con filtros
└── config/
    └── AsyncConfig.java           # Thread pool "auditExecutor"

src/main/resources/db/migration/
└── V29__crear_tabla_auditoria.sql
```

---

## Esquema de base de datos

```sql
CREATE TABLE auditoria (
    id_auditoria   INT         NOT NULL AUTO_INCREMENT,
    tabla          VARCHAR(60) NOT NULL,
    tipo_operacion ENUM('POST','PUT','DELETE') NOT NULL,
    id_registro    INT         NULL,
    id_usuario     INT         NULL,
    datos_antes    JSON        NULL,
    datos_despues  JSON        NULL,
    creado_en      DATETIME    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_auditoria),
    INDEX idx_auditoria_tabla (tabla, id_registro),
    INDEX idx_auditoria_usuario (id_usuario),
    INDEX idx_auditoria_fecha (creado_en)
);
```

| Campo | Descripción |
|---|---|
| `tabla` | Nombre en snake_case de la entidad afectada |
| `tipo_operacion` | POST, PUT o DELETE (PATCH se mapea como PUT) |
| `id_registro` | ID de la fila afectada, null si no aplica |
| `id_usuario` | ID del usuario autenticado que ejecutó la operación |
| `datos_antes` | Snapshot JSON del registro antes de la mutación (PUT/DELETE) |
| `datos_despues` | Snapshot JSON del body retornado por el controller (POST/PUT) |
| `creado_en` | Timestamp llenado por `@PrePersist` |

---

## Código fuente

### AuditAspect.java

```java
package com.aop;

import com.DBEnums.TipoOperacion;
import com.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.AuditoriaService;
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

// Aspecto que intercepta automáticamente @PostMapping, @PutMapping, @PatchMapping y
// @DeleteMapping en todos los Controllers y registra la operación de forma asíncrona
// en la tabla auditoria. Para excluir un método o clase completa usar @SkipAudit.
// La tabla se deriva del nombre del Controller usando conversión camelCase→snake_case:
//   "MovimientoInventarioController" → "movimiento_inventario"
//   "PedidoPushController"           → "pedido_push"
//
// Para PUT/PATCH/DELETE con path variable id, se captura datos_antes: se carga la entidad
// vía EntityManager.find() ANTES de proceed() y se serializa a JSON inmediatamente para
// congelar el snapshot antes de que el controller mute la entidad managed.
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditoriaService auditoriaService;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final EntityClassResolver entityClassResolver;

    public AuditAspect(AuditoriaService auditoriaService,
                       EntityManager entityManager,
                       ObjectMapper objectMapper,
                       EntityClassResolver entityClassResolver) {
        this.auditoriaService = auditoriaService;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.entityClassResolver = entityClassResolver;
    }

    // Intercepta todos los métodos mutantes dentro de com.Controller.*
    // @SkipAudit a nivel de clase o método cancela el registro.
    // Si proceed() lanza excepción, no se registra auditoría (se re-lanza sin tocar).
    @Around("within(com.Controller..*) && " +
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

        // Snapshot previo solo para PUT/DELETE (PATCH también cae en PUT) con id conocido
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

    // Congela el estado actual del registro en JSON. Devuelve null si no aplica.
    // Se detach() para que mutaciones posteriores no contaminen el objeto ni disparen flush automáticos.
    private String capturarSnapshot(TipoOperacion operacion, String tabla, Integer id) {
        if (id == null) return null;
        if (operacion != TipoOperacion.PUT && operacion != TipoOperacion.DELETE) return null;
        try {
            Class<?> entityClass = entityClassResolver.resolver(tabla).orElse(null);
            if (entityClass == null) return null;
            Object snapshot = entityManager.find(entityClass, id);
            if (snapshot == null) return null;
            String json = objectMapper.writeValueAsString(snapshot);
            entityManager.detach(snapshot);
            return json;
        } catch (Exception e) {
            log.warn("AuditAspect: no se pudo capturar snapshot tabla={} id={} error={}",
                    tabla, id, e.getMessage());
            return null;
        }
    }

    // @PostMapping → POST, @PutMapping/@PatchMapping → PUT, @DeleteMapping → DELETE
    private TipoOperacion detectarOperacion(Method method) {
        if (method.isAnnotationPresent(PostMapping.class))   return TipoOperacion.POST;
        if (method.isAnnotationPresent(PutMapping.class) ||
            method.isAnnotationPresent(PatchMapping.class))  return TipoOperacion.PUT;
        if (method.isAnnotationPresent(DeleteMapping.class)) return TipoOperacion.DELETE;
        return null;
    }

    // Convierte "MovimientoInventarioController" → "movimiento_inventario"
    // eliminando el sufijo "Controller" y aplicando regex camelCase → snake_case
    private String detectarTabla(String simpleClassName) {
        return simpleClassName
                .replace("Controller", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    // El principal es un Usuario (implementa UserDetails) inyectado por JwtAuthenticationFilter.
    // auth.getName() devolvería el username, no el id — por eso se usa el principal.
    private Integer extraerIdUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof com.entity.Usuario u) return u.getIdUsuario();
        return null;
    }

    // Primer argumento int/long — típicamente el @PathVariable id.
    // Para endpoints body-only (PUT sin path variable) retorna null y datos_antes queda null.
    private Integer extraerIdDesdeArgs(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Integer i) return i;
            if (arg instanceof Long l) return l.intValue();
        }
        return null;
    }

    // Busca por reflexión el getter de ID de la entidad retornada; prueba nombres convencionales
    private Integer extraerIdDeObjeto(Object obj) {
        for (String nombre : new String[]{"getIdComida", "getIdPedido", "getIdCliente",
                "getIdComplemento", "getIdRefresco", "getIdExtra", "getIdInventario",
                "getIdInventarioComida", "getIdSucursal", "getIdUsuario",
                "getIdMovimientoInventario", "getId"}) {
            try {
                Method m = obj.getClass().getMethod(nombre);
                Object val = m.invoke(obj);
                if (val instanceof Integer i) return i;
                if (val instanceof Long l) return l.intValue();
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Desenvuelve ResponseEntity<ApiResponse<T>> para obtener el objeto de negocio real
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
```

---

### SkipAudit.java

```java
package com.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Excluye un método o clase completa del registro automático de auditoría en AuditAspect.
// Nivel clase: todos los métodos POST/PUT/PATCH/DELETE del Controller son ignorados.
// Nivel método: solo ese método específico es ignorado.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipAudit {}
```

---

### EntityClassResolver.java

```java
package com.aop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Resuelve el nombre de una tabla (snake_case) a la clase @Entity correspondiente.
// Usado por AuditAspect para cargar el snapshot previo (datos_antes) vía EntityManager.find()
// en operaciones PUT/PATCH/DELETE. El mapa se construye una sola vez al arrancar la app.
@Component
public class EntityClassResolver {

    private static final Logger log = LoggerFactory.getLogger(EntityClassResolver.class);
    private static final String ENTITY_PACKAGE = "com.entity"; // ← actualizar al migrar

    private final Map<String, Class<?>> tablaAClase = new HashMap<>();

    @PostConstruct
    void escanear() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        provider.findCandidateComponents(ENTITY_PACKAGE).forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                String tabla = obtenerNombreTabla(clazz);
                tablaAClase.put(tabla, clazz);
            } catch (ClassNotFoundException e) {
                log.warn("EntityClassResolver: no se pudo cargar {}", bd.getBeanClassName());
            }
        });

        log.info("EntityClassResolver: registradas {} entidades", tablaAClase.size());
    }

    public Optional<Class<?>> resolver(String tabla) {
        return Optional.ofNullable(tablaAClase.get(tabla));
    }

    // Usa @Table.name() si está presente; si no, infiere snake_case desde el nombre de la clase
    private String obtenerNombreTabla(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }
}
```

---

### Auditoria.java

```java
package com.entity;

import com.DBEnums;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Trail de auditoría para operaciones de escritura (POST/PUT/DELETE).
// Se persiste de forma asíncrona por AuditoriaService.
// datosAntes: se llena para PUT/PATCH/DELETE con path variable id (snapshot pre-mutación);
//   queda null para POST y para endpoints body-only sin id en la ruta.
// datosDespues: se llena con el body retornado por el controller; queda null para DELETE
//   porque los endpoints DELETE retornan ApiResponse<Void> (data = null).
@Entity
@Table(name = "auditoria")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private int idAuditoria;

    @Column(name = "tabla", nullable = false, length = 60)
    private String tabla;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private DBEnums.TipoOperacion tipoOperacion;

    @Column(name = "id_registro")
    private Integer idRegistro;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "datos_antes", columnDefinition = "JSON")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "JSON")
    private String datosDespues;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = LocalDateTime.now();
    }
}
```

---

### AuditoriaRepository.java

```java
package com.dao;

import com.DBEnums;
import com.dto.response.AuditoriaResponseDTO;
import com.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    @Query(value = """
            SELECT new com.dto.response.AuditoriaResponseDTO(
                a.idAuditoria, u.idUsuario, u.nombreUsuario,
                s.idSucursal, s.nombreSucursal,
                CASE
                  WHEN a.tabla = 'movimiento_inventario' THEN 'Cargar inventario'
                  WHEN a.tabla = 'pedido'                THEN 'Pedidos'
                  WHEN a.tabla = 'comida'                THEN 'Comidas'
                  WHEN a.tabla = 'complemento'           THEN 'Complementos'
                  WHEN a.tabla = 'refresco'              THEN 'Bebidas'
                  WHEN a.tabla = 'cliente'               THEN 'Clientes'
                  WHEN a.tabla = 'corte_caja'            THEN 'Arqueo de caja'
                  WHEN a.tabla = 'gasto_corte_caja'      THEN 'Merma de caja'
                  WHEN a.tabla = 'inventario_comida'     THEN 'Contador de comidas'
                  ELSE 'Desconocido'
                END,
                a.creadoEn,
                a.tipoOperacion,
                a.datosDespues,
                a.idRegistro,
                a.tabla,
                a.datosAntes,
                a.datosDespues
            )
            FROM Auditoria a
            JOIN Usuario u ON u.idUsuario = a.idUsuario
            JOIN u.sucursal s
            WHERE (:desde IS NULL OR a.creadoEn >= :desde)
              AND (:hasta IS NULL OR a.creadoEn <= :hasta)
              AND (:idUsuario IS NULL OR a.idUsuario = :idUsuario)
              AND (:tipoOperacion IS NULL OR a.tipoOperacion = :tipoOperacion)
            ORDER BY a.creadoEn DESC
            """,
            countQuery = """
            SELECT COUNT(a)
            FROM Auditoria a
            JOIN Usuario u ON u.idUsuario = a.idUsuario
            WHERE (:desde IS NULL OR a.creadoEn >= :desde)
              AND (:hasta IS NULL OR a.creadoEn <= :hasta)
              AND (:idUsuario IS NULL OR a.idUsuario = :idUsuario)
              AND (:tipoOperacion IS NULL OR a.tipoOperacion = :tipoOperacion)
            """)
    Page<AuditoriaResponseDTO> findConFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("idUsuario") Integer idUsuario,
            @Param("tipoOperacion") DBEnums.TipoOperacion tipoOperacion,
            Pageable pageable
    );
}
```

---

### AuditoriaService.java

```java
package com.service;

import com.DBEnums;
import com.DBEnums.TipoOperacion;
import com.dao.AuditoriaRepository;
import com.dto.response.AuditoriaResponseDTO;
import com.entity.Auditoria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.auditoria.AuditoriaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Servicio que persiste registros de auditoría (POST/PUT/DELETE) en la tabla auditoria.
// Se ejecuta de forma asíncrona con transacción independiente (REQUIRES_NEW) para que
// un fallo en la auditoría no haga rollback de la operación de negocio principal.
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;
    private final AuditoriaParser auditoriaParser;

    public AuditoriaService(AuditoriaRepository auditoriaRepository,
                            ObjectMapper objectMapper,
                            AuditoriaParser auditoriaParser) {
        this.auditoriaRepository = auditoriaRepository;
        this.objectMapper = objectMapper;
        this.auditoriaParser = auditoriaParser;
    }

    // @Async("auditExecutor") → pool dedicado (AsyncConfig) para no compartir threads con otros @Async.
    // REQUIRES_NEW → transacción propia: si falla, solo revierte la auditoría, no el negocio.
    // datosAntesJson llega ya serializado desde AuditAspect porque el snapshot se toma
    // ANTES de que el controller mute la entidad. Serializarlo aquí (en el thread @Async)
    // sería tarde: la entidad managed ya reflejaría el estado nuevo.
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String tabla,
                          TipoOperacion operacion,
                          Integer idRegistro,
                          Integer idUsuario,
                          String datosAntesJson,
                          Object datosDespues) {
        try {
            Auditoria auditoria = Auditoria.builder()
                    .tabla(tabla)
                    .tipoOperacion(operacion)
                    .idRegistro(idRegistro)
                    .idUsuario(idUsuario)
                    .datosAntes(datosAntesJson)
                    .datosDespues(serializarJson(datosDespues))
                    .build();

            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.warn("Auditoría falló para tabla={} operacion={}: {}", tabla, operacion, e.getMessage());
        }
    }

    public Page<AuditoriaResponseDTO> findConFiltros(LocalDate desde, LocalDate hasta,
                                                      Integer idUsuario,
                                                      DBEnums.TipoOperacion tipoOperacion,
                                                      Pageable pageable) {
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;
        Page<AuditoriaResponseDTO> pagina = auditoriaRepository.findConFiltros(
                desdeTs, hastaTs, idUsuario, tipoOperacion, pageable);
        return auditoriaParser.parsear(pagina);
    }

    // Convierte el objeto a JSON usando Jackson; respeta @JsonIgnore (ej. pin en Usuario)
    private String serializarJson(Object objeto) {
        if (objeto == null) return null;
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"No se pudo serializar el objeto\"}";
        }
    }
}
```

---

### AuditoriaParser.java

```java
package com.service.auditoria;

import com.DBEnums.TipoOperacion;
import com.dto.response.AuditoriaResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaParser {

    private final ObjectMapper objectMapper;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("eee yyyy/M/d HH:mm", Locale.forLanguageTag("es"));

    public AuditoriaParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Page<AuditoriaResponseDTO> parsear(Page<AuditoriaResponseDTO> pagina) {
        return pagina.map(this::enriquecer);
    }

    private AuditoriaResponseDTO enriquecer(AuditoriaResponseDTO dto) {
        dto.setDescripcion(generarDescripcion(dto));
        return dto;
    }

    private String generarDescripcion(AuditoriaResponseDTO dto) {
        String tabla = dto.getTabla();
        TipoOperacion accion = dto.getAccion();
        Integer id = dto.getIdRegistro();
        LocalDateTime fecha = dto.getFecha();
        JsonNode despues = parsearJson(dto.getDatosDespues());
        JsonNode antes   = parsearJson(dto.getDatosAntes());

        if (tabla == null || accion == null) return "Operación registrada";

        return switch (tabla) {
            case "pedido"                -> describirPedido(accion, despues, antes, id, fecha);
            case "comida"                -> describirComida(accion, despues, antes, id, fecha);
            case "refresco"              -> describirRefresco(accion, despues, antes, id, fecha);
            case "complemento"           -> describirComplemento(accion, despues, antes, id, fecha);
            case "cliente"               -> describirCliente(accion, despues, antes, id, fecha);
            case "corte_caja"            -> describirCorteCaja(accion, despues, antes, id, fecha);
            case "gasto_corte_caja"      -> describirGastoCorteCaja(accion, despues, antes, id, fecha);
            case "inventario_comida"     -> describirInventarioComida(accion, despues, antes, id, fecha);
            case "movimiento_inventario" -> describirMovimientoInventario(accion, despues, antes, id, fecha);
            default                      -> accion + " en " + tabla + (id != null ? " #" + id : "");
        };
    }

    private String describirPedido(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                LocalDateTime dateTime = parseLocalDateTime(textOrElse(despues, "fechaExpedicionPedido", null));
                if (dateTime == null) dateTime = fecha;
                String fechaFormateada = dateTime != null ? dateTime.format(formatter) : "fecha desconocida";
                String tipo = textOrElse(despues, "tipoCliente", "desconocido");
                yield "Se creó el pedido #" + id + " de tipo " + tipo + " creado el " + fechaFormateada;
            }
            case PUT    -> "Se actualizó el pedido #" + id;
            case DELETE -> "Se eliminó el pedido #" + id;
        };
    }

    private String describirComida(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreComida", "desconocida");
                yield "Se creó la comida '" + nombre + "'";
            }
            case PUT -> {
                String nombre = textOrElse(despues, "nombreComida", "#" + id);
                yield "Se actualizó la comida '" + nombre + "'";
            }
            case DELETE -> "Se eliminó la comida #" + id;
        };
    }

    private String describirRefresco(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre   = textOrElse(despues, "nombreRefresco", "desconocida");
                String tamanio  = textOrElse(despues, "tamanio", "");
                String medicion = textOrElse(despues, "medicion", "");
                String sufijo   = tamanio.isBlank() ? "" : " " + tamanio + medicion;
                yield "Se creó la bebida '" + nombre + sufijo + "'";
            }
            case PUT -> {
                String nombre = textOrElse(despues, "nombreRefresco", "#" + id);
                yield "Se actualizó la bebida '" + nombre + "'";
            }
            case DELETE -> "Se eliminó la bebida #" + id;
        };
    }

    private String describirComplemento(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreComplemento", "desconocido");
                yield "Se creó el complemento '" + nombre + "'";
            }
            case PUT -> {
                String nombre = textOrElse(despues, "nombreComplemento", "#" + id);
                yield "Se actualizó el complemento '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el complemento #" + id;
        };
    }

    private String describirCliente(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreCliente", "desconocido");
                yield "Se registró el cliente '" + nombre + "'";
            }
            case PUT -> {
                String nombre = textOrElse(despues, "nombreCliente", "#" + id);
                yield "Se actualizó el cliente '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el cliente #" + id;
        };
    }

    private String describirCorteCaja(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String fechaCorte = textOrElse(despues, "fecha", "desconocida");
                String diferencia = textOrElse(despues, "diferencia", "0");
                yield "Se realizó el arqueo de caja del " + fechaCorte + " (diferencia: $" + diferencia + ")";
            }
            case PUT    -> "Se actualizó el arqueo de caja #" + id;
            case DELETE -> "Se eliminó el arqueo de caja #" + id;
        };
    }

    private String describirGastoCorteCaja(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String monto  = textOrElse(despues, "monto", "0");
                String motivo = textOrElse(despues, "motivo", "sin motivo");
                yield "Se registró una merma de $" + monto + " por: " + motivo;
            }
            case PUT    -> "Se actualizó la merma de caja #" + id;
            case DELETE -> "Se eliminó la merma de caja #" + id;
        };
    }

    private String describirInventarioComida(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST   -> "Se creó el contador de comidas #" + id;
            case PUT    -> "Se actualizó el contador de comidas #" + id;
            case DELETE -> "Se eliminó el contador de comidas #" + id;
        };
    }

    private String describirMovimientoInventario(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String motivo = textOrElse(despues, "razonInventario", "sin motivo");
                yield "Se cargó un inventario de platillos con el motivo de " + motivo;
            }
            case PUT    -> "Se actualizó el movimiento de inventario #" + id;
            case DELETE -> "Se eliminó el movimiento de inventario #" + id;
        };
    }

    private JsonNode parsearJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDateTime.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private String textOrElse(JsonNode node, String campo, String defaultVal) {
        if (node == null || !node.has(campo) || node.get(campo).isNull()) return defaultVal;
        return node.get(campo).asText(defaultVal);
    }
}
```

---

### AuditoriaResponseDTO.java

```java
package com.dto.response;

import com.DBEnums;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditoriaResponseDTO {
    private int idAuditoria;
    private int idUsuario;
    private String nombreUsuario;
    private int idSucursal;           // ← adaptar si el modelo destino no tiene sucursal
    private String nombreSucursal;    // ← adaptar si el modelo destino no tiene sucursal
    private String nombreTabla;

    @JsonFormat(pattern = "eee yyyy/M/d HH:mm", locale = "es")
    private LocalDateTime fecha;

    private DBEnums.TipoOperacion accion;
    private String descripcion;
    private Integer idRegistro;

    @JsonIgnore private String tabla;       // usado internamente por AuditoriaParser
    @JsonIgnore private String datosAntes;  // no se expone en la API
    @JsonIgnore private String datosDespues;
}
```

---

### AuditoriaController.java

```java
package com.Controller;

import com.DBEnums;
import com.aop.SkipAudit;
import com.dto.response.ApiResponse;
import com.dto.response.AuditoriaResponseDTO;
import com.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@SkipAudit                                         // no audita sus propios accesos
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditoriaResponseDTO>>> findConFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) DBEnums.TipoOperacion tipoOperacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditoriaResponseDTO> resultado = auditoriaService.findConFiltros(
                desde, hasta, idUsuario, tipoOperacion, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de auditoría obtenidos correctamente", resultado));
    }
}
```

---

### AsyncConfig.java

```java
package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

// Pool dedicado para las tareas @Async de AuditoriaService.
// Sin este bean, @EnableAsync usa SimpleAsyncTaskExecutor (crea un thread por tarea sin reutilizar),
// que bajo carga alta agota threads del SO y degrada latencia. Con core=2, max=4 y queue=500
// el pool es suficiente para ~40 escrituras/segundo sostenidas. DiscardOldestPolicy asegura que
// bajo saturación se pierden las auditorías más antiguas en cola, no las nuevas ni el request HTTP.
@Configuration
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public TaskExecutor auditExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("audit-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();
        return ex;
    }
}
```

---

## Dependencia requerida en pom.xml

```xml
<!-- AOP — necesario para que @Aspect funcione -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

El resto de dependencias (JPA, Security, Jackson, Lombok) se asumen presentes en cualquier proyecto Spring Boot estándar.

## Habilitar @Async en Application.java

```java
@SpringBootApplication
@EnableAsync   // ← requerido para que @Async("auditExecutor") funcione
public class Application { ... }
```

---

## Endpoints con @SkipAudit en este proyecto

| Controller / método | Razón |
|---|---|
| `AuditoriaController` (clase) | No registra sus propios accesos de lectura |
| `AuthController` — POST /login | Operación de autenticación, no de negocio |
| `AuthController` — POST /cambiar-sucursal/{id}` | Cambio de contexto de sesión |
| `ComandaController` — endpoints de impresión y bloqueo | Operaciones de infraestructura, no de negocio |
| `N8nController` (clase) | Integración externa, ruido en el log |
| `PedidoController` — PATCH /{id}/impresion | Reimpresión de ticket |
| `PedidoController` — PATCH /{id}/marcar-impreso | Cambio de estado interno |

---

## Consideraciones al migrar

| Punto | Acción necesaria |
|---|---|
| Package de entidades | Actualizar `ENTITY_PACKAGE = "com.entity"` en `EntityClassResolver` |
| JOIN a `Usuario` y `Sucursal` en el repo | Adaptar la JPQL de `findConFiltros()` al modelo de usuario destino |
| `AuditoriaResponseDTO` | Quitar o adaptar `idSucursal`/`nombreSucursal` si el destino no tiene sucursales |
| `extraerIdUsuario()` en `AuditAspect` | Cambiar `com.entity.Usuario` al tipo de principal del proyecto destino |
| `extraerIdDeObjeto()` en `AuditAspect` | Añadir los getters de ID de las entidades del proyecto destino |
| `AuditoriaParser` | Reescribir los `switch(tabla)` con las tablas del negocio destino |
| `TipoOperacion` enum | Viene de `DBEnums`; mover al package correcto en el destino |
| Pointcut del `@Around` | Cambiar `within(com.Controller..*)`  al package de controllers del destino |
