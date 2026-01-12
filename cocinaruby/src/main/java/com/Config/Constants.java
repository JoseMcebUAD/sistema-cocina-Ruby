package com.Config;

public final class Constants {

    // Configuración de impresora
    public static final String NOMBRE_IMPRESORA = "EPSON";

    // Configuración de formato de factura térmica
    public static final int ANCHO_TICKET = 40; // Ancho en caracteres del ticket térmico
    public static final String SEPARADOR_TICKET = "----------------------------------------"; // 40 caracteres

    public static final String AUTH_URL = "auth.fxml";
    public static final String MENU_URL = "menu.fxml";


    //numero de veces que se hashea un string
    public static final int EXP_COS = 5;

    /**
     * Constructor privado para evitar instanciación.
     */
    private Constants() {
        throw new UnsupportedOperationException("Esta clase no puede ser instanciada");
    }

}
