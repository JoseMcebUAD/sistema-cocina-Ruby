package com.Model.Enum;

/**
 * Contiene constantes de textos e interfaz para la pantalla de autenticación.
 */
public enum AuthUIConstants {
    // Títulos y textos
    TITLE_LOGIN("Iniciar Sesión"),
    TITLE_REGISTER("Registrarse"),
    LABEL_USER("Usuario"),
    LABEL_PASSWORD("Contraseña"),
    
    // Botones
    BTN_LOGIN("Iniciar Sesión"),
    BTN_REGISTER("Registrarse"),
    BTN_TOGGLE_PASSWORD("Ver Contraseña"),
    
    // Mensajes de error
    MSG_INVALID_CREDENTIALS("Usuario o contraseña incorrecto"),
    MSG_USER_EXISTS("El usuario ya existe"),
    MSG_USER_REQUIRED("El usuario es obligatorio"),
    MSG_PASSWORD_REQUIRED("La contraseña es obligatoria"),
    MSG_ERROR_LOGIN("Error al iniciar sesión"),
    MSG_ERROR_REGISTER("Error al registrarse"),
    
    // Mensajes de éxito
    MSG_LOGIN_SUCCESS("¡Bienvenido!"),
    MSG_REGISTER_SUCCESS("Usuario registrado correctamente");
    
    private final String value;
    
    AuthUIConstants(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
