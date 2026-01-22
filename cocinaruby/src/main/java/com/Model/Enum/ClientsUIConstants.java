package com.Model.Enum;

/**
 * Contiene constantes de validación y mensajes para la interfaz de clientes.
 */
public enum ClientsUIConstants {
    // Mensajes de validación
    MSG_NAME_REQUIRED("El nombre del cliente es obligatorio."),
    MSG_PHONE_INVALID("El teléfono debe contener solo números (máx. 10 dígitos)."),
    MSG_CLIENT_ADDED("El cliente se ha agregado correctamente"),
    MSG_CLIENT_EXISTS("No se pudo agregar: El cliente ya existe o hubo un error de conexión."),
    MSG_CLIENT_UPDATED("El cliente se ha actualizado correctamente"),
    MSG_CLIENT_DELETED("El cliente se ha eliminado correctamente"),
    MSG_CLIENT_DELETE_FAIL("No se pudo eliminar el cliente."),
    
    // Mensajes de confirmación
    CONFIRM_DELETE("¿Está seguro de que desea eliminar este cliente?"),
    
    // Etiquetas
    LABEL_NAME("Nombre"),
    LABEL_PHONE("Teléfono"),
    LABEL_ADDRESS("Dirección");
    
    private final String value;
    
    ClientsUIConstants(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
