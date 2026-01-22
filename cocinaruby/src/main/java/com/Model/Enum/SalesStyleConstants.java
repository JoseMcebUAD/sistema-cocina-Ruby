package com.Model.Enum;

/**
 * Contiene constantes de estilos CSS y configuración visual para la interfaz de ventas.
 */
public enum SalesStyleConstants {
    // Estilos CSS
    TAB_BUTTON("sales-tab-button"),
    TAB_ACTIVE("sales-tab-active"),
    BUTTON_STYLE("-fx-padding: 5; -fx-font-size: 11;"),
    TABLE_ACTION_BTN("table-action-btn");
    
    // Configuración visual
    public static final int FADE_DURATION_MS = 300;
    public static final int BUTTON_SPACING = 5;
    
    private final String value;
    
    SalesStyleConstants(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
