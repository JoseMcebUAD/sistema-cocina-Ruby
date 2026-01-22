package com.Model.Enum;

/**
 * Contiene constantes de configuración visual para el menú principal.
 */
public enum MenuStyleConstants {
    // Estilos CSS para botones
    BUTTON_ACTIVE("-fx-text-fill: #1CAC78; -fx-font-weight: bold;"),
    BUTTON_INACTIVE("-fx-text-fill: #666;"),
    
    // Configuración de animaciones
    SLIDER_ANIMATION_DURATION(300),  // milliseconds
    FONT_SIZE_DIVISOR(45);           // Divisor para calcular tamaño dinámico de fuente
    
    private final Object value;
    
    MenuStyleConstants(Object value) {
        this.value = value;
    }
    
    public String getStringValue() {
        return value.toString();
    }
    
    public int getIntValue() {
        return (Integer) value;
    }
}
