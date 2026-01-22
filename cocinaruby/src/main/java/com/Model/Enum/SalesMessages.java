package com.Model.Enum;

/**
 * Contiene mensajes de usuario para la interfaz de ventas.
 */
public enum SalesMessages {
    // Mensajes informativos
    MSG_TOTAL_SALES("Total de Ventas: "),
    MSG_NO_ORDERS("No se encontraron órdenes para hoy"),
    
    // Mensajes de confirmación
    ALERT_CONFIRM_DELETE("¿Está seguro de que desea eliminar la orden #"),
    ALERT_DELETE_WARNING("Esta acción no se puede deshacer."),
    ALERT_SUCCESS_DELETE("Orden eliminada correctamente");
    
    private final String value;
    
    SalesMessages(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
