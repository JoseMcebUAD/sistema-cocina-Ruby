package com.cocinarubi.exception;

import org.springframework.http.HttpStatus;

/**
 * Advertencia lanzada cuando una entidad catálogo tiene pedidos relacionados y
 * el cliente no ha confirmado la eliminación (saltarConfirmacion=false).
 *
 * <p>Retorna 409 Conflict con {@code errorCode: "ADVERTENCIA_ELIMINACION"} para que
 * el front detecte la condición en su interceptor de errores y muestre el diálogo
 * de confirmación antes de reenviar con saltarConfirmacion=true.</p>
 */
public class AdvertenciaEliminacionException extends BusinessException {

    public AdvertenciaEliminacionException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, ErrorCode.ADVERTENCIA_ELIMINACION);
    }
}
