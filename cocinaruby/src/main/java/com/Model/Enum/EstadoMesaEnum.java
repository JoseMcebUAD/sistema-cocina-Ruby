package com.Model.Enum;

/**
 * Enum de estados posibles para las mesas
 */
public class EstadoMesaEnum {
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String OCUPADO = "OCUPADO";
    public static final String SUSPENDIDO = "SUSPENDIDO";

    /**
     * Valida si el estado proporcionado es válido
     * @param estado Estado a validar
     * @return true si el estado es válido, false en caso contrario
     */
    public static boolean esValido(String estado) {
        return estado != null && (
            estado.equals(DISPONIBLE) ||
            estado.equals(OCUPADO) ||
            estado.equals(SUSPENDIDO)
        );
    }

    /**
     * Obtiene un array con todos los estados válidos
     * @return Array de strings con los estados
     */
    public static String[] getAllEstados() {
        return new String[] { DISPONIBLE, OCUPADO, SUSPENDIDO };
    }
}
