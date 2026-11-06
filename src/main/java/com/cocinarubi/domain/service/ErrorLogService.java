package com.cocinarubi.domain.service;

import com.cocinarubi.dao.ErrorLogRepository;
import com.cocinarubi.domain.entity.ErrorLog;
import com.cocinarubi.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Persiste excepciones no controladas en la tabla error_log.
 * Capa: Service — logging técnico asíncrono desacoplado del request principal.
 */
@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private static final int MAX_STACK_TRACE_LENGTH = 4_000;

    private final ErrorLogRepository errorLogRepository;

    /**
     * Registra la excepción en base de datos de forma asíncrona.
     * El idUsuario se extrae en el hilo del request (GlobalExceptionHandler) y se pasa como parámetro
     * porque @Async ejecuta en otro hilo donde el SecurityContext no está disponible.
     * Las BusinessException se filtran y no se persisten (son errores de negocio esperados).
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String path, String metodoHttp, Exception ex, Integer idUsuario) {
        try {
            if (ex instanceof BusinessException) return;

            ErrorLog errorLog = ErrorLog.builder()
                    .path(path)
                    .metodoHttp(metodoHttp)
                    .tipoError(ex.getClass().getSimpleName())
                    .mensaje(ex.getMessage())
                    .stackTrace(obtenerStackTrace(ex))
                    .idUsuario(idUsuario)
                    .idSucursal(null)
                    .build();

            errorLogRepository.save(errorLog);
        } catch (Exception ignored) {
            // El logging nunca puede ser causa de una excepción adicional
        }
    }

    private String obtenerStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() > MAX_STACK_TRACE_LENGTH
                ? trace.substring(0, MAX_STACK_TRACE_LENGTH)
                : trace;
    }
}
