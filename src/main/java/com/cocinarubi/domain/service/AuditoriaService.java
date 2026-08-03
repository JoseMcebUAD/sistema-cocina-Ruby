package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.DBConstants.TipoOperacion;
import com.cocinarubi.dao.AuditoriaRepository;
import com.cocinarubi.domain.entity.Auditoria;
import com.cocinarubi.domain.service.auditoria.AuditoriaParser;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.AuditoriaResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
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

/**
 * Servicio que persiste registros de auditoría (POST/PUT/DELETE) en la tabla auditoria.
 * Se ejecuta de forma asíncrona con transacción independiente (REQUIRES_NEW) para que
 * un fallo en la auditoría no haga rollback de la operación de negocio principal.
 */
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
                                                     DBConstants.TipoOperacion tipoOperacion,
                                                     Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'",
                    HttpStatus.BAD_REQUEST);
        }
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;
        Page<AuditoriaResponseDTO> pagina = auditoriaRepository.findConFiltros(
                desdeTs, hastaTs, idUsuario, tipoOperacion, pageable);
        return auditoriaParser.parsear(pagina);
    }

    private String serializarJson(Object objeto) {
        if (objeto == null) return null;
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"No se pudo serializar el objeto\"}";
        }
    }
}
