package com.Model.Enum;

/**
 * Contiene constantes de animaciones y configuración visual compartidas.
 */
public enum AnimationConstants {
    // Duraciones de animaciones
    FADE_DURATION_SHORT(150),      // Animaciones rápidas
    FADE_DURATION_MEDIUM(300),     // Animaciones estándar
    FADE_DURATION_LONG(500);       // Animaciones lentas
    
    private final int milliseconds;
    
    AnimationConstants(int milliseconds) {
        this.milliseconds = milliseconds;
    }
    
    public int getMillis() {
        return milliseconds;
    }
    
    public javafx.util.Duration getDuration() {
        return javafx.util.Duration.millis(milliseconds);
    }
}
