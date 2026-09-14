package com.cocinarubi;

import java.time.ZoneId;

public class Constants {
    
    public static final ZoneId ZONA_MERIDA = ZoneId.of("America/Merida");

    // Configuración de impresora
    public static final String NOMBRE_IMPRESORA = "EPSON_TM-T88Vl";

    // Configuración de formato de factura térmica
    public static final int ANCHO_TICKET = 35; // Ancho en caracteres del ticket térmico
    public static final String SEPARADOR_TICKET = "----------------------------------------"; // 40 caracteres
    public static final String NUMERO_COCINA = "+52 9995-42-80-65"; // Número de celular de la cocina


    /**
     * Constructor privado para evitar instanciación.
     */
    private Constants() {
        throw new UnsupportedOperationException("Esta clase no puede ser instanciada");
    }
}
