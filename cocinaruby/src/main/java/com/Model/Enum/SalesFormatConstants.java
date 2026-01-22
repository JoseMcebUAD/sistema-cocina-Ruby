package com.Model.Enum;

/**
 * Contiene constantes de formato, textos y valores por defecto para la interfaz de ventas.
 */
public enum SalesFormatConstants {
    // Formatos
    CURRENCY_FORMAT("$%.2f"),
    
    // Estados de impresión
    STATUS_PRINTED("Impreso"),
    STATUS_NOT_PRINTED("No Impreso"),
    
    // Valores por defecto
    DEFAULT_CLIENT_NAME("Cliente General"),
    DEFAULT_PAYMENT_TYPE("Desconocido");
    
    private final String value;
    
    SalesFormatConstants(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
