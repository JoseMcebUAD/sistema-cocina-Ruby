package com.cocinarubi.exception;

/**
 * Excepción lanzada cuando no hay inventario suficiente para completar una operación.
 * No extiende BusinessException para que el GlobalExceptionHandler la persista en error_log.
 */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
