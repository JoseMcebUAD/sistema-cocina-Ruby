package com.Model.Enum;

/**
 * Contiene constantes y mensajes para la interfaz de órdenes.
 */
public enum OrderUIConstants {
    // Mensajes de validación
    MSG_EMPTY_FIELDS("Campos Vacíos"),
    MSG_FILL_FIELDS("Por favor llene todos los campos del producto."),
    MSG_EMPTY_ORDER("Orden Vacía"),
    MSG_NO_PRODUCTS("No puede realizar una orden sin productos."),
    MSG_INCOMPLETE_DATA("Datos Incompletos"),
    MSG_DELIVERY_REQUIRED("Para Domicilio, debe llenar Nombre, Dirección y Teléfono."),
    MSG_TABLE_REQUIRED("Por favor ingrese el número de mesa."),
    
    // Mensajes de éxito
    MSG_ORDER_SAVED("Orden guardada correctamente"),
    MSG_PRODUCT_ADDED("Producto agregado"),
    MSG_PRODUCT_REMOVED("Producto eliminado");
    
    private final String value;
    
    OrderUIConstants(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
