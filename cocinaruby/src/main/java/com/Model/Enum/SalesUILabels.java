package com.Model.Enum;

/**
 * Contiene etiquetas y texto de interfaz para botones y elementos de UI en ventas.
 */
public enum SalesUILabels {
    // Etiquetas de botones
    BTN_EDIT("Editar"),
    BTN_DELETE("Borrar"),
    BTN_REPRINT("Reimprimir");
    
    private final String value;
    
    SalesUILabels(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
