package com.cocinarubi.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.cocinarubi.Constants;

public class FormatearReciboService {

    protected static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");

    /**
     * Formatea el detalle de una orden genérica (refrescos, etc.),
     * dividiendo en múltiples líneas si es necesario.
     * El precio siempre aparece al final de la última línea.
     *
     * @param especificaciones Descripción del producto
     * @param precio           Precio formateado
     * @return Lista de líneas formateadas
     */
    public List<String> formatearDetalleOrden(String especificaciones, String precio) {
        return formatearDetalleOrden(especificaciones, precio, Constants.ANCHO_TICKET);
    }

    /**
     * Formatea el detalle de una orden con ancho efectivo explícito.
     * Usar cuando el estilo de fuente reduce el ancho útil (ej. FontSize._2 → ANCHO_TICKET/2).
     *
     * @param especificaciones Descripción del producto
     * @param precio           Precio formateado
     * @param anchoEfectivo    Ancho real disponible en caracteres
     * @return Lista de líneas formateadas
     */
    public List<String> formatearDetalleOrden(String especificaciones, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();
        int anchoParaTexto = anchoEfectivo - precio.length() - 1;

        if (especificaciones.length() <= anchoParaTexto) {
            lineas.add(construirLineaConPrecio(especificaciones, precio, anchoEfectivo));
        } else {
            List<String> lineasTexto = dividirTexto(especificaciones, anchoEfectivo);
            for (int i = 0; i < lineasTexto.size() - 1; i++) {
                lineas.add(completarLinea(lineasTexto.get(i), anchoEfectivo));
            }
            String ultimaLinea = lineasTexto.get(lineasTexto.size() - 1);
            lineas.add(construirLineaConPrecio(ultimaLinea, precio, anchoEfectivo));
        }

        return lineas;
    }

    /**
     * Formatea una línea de total con texto y precio alineados a ANCHO_TICKET.
     *
     * @param texto  Texto a la izquierda (ej: "TOTAL")
     * @param precio Precio formateado
     * @return Línea de exactamente ANCHO_TICKET caracteres
     */
    public String formatearLineaTotal(String texto, String precio) {
        return construirLineaConPrecio(texto, precio, Constants.ANCHO_TICKET);
    }

    /**
     * Formatea el texto de guarniciones con el precio al final, garantizando que el
     * precio quede completo en una sola línea sin desbordarse.
     *
     * Reglas:
     * <ul>
     *   <li>Si el texto + precio caben con margen ≥ 2 → una línea con precio alineado a la derecha.</li>
     *   <li>Si no caben → se divide el texto por palabras; en la última línea se vuelve a
     *       verificar el margen: si es ≥ 2 el precio va ahí, en caso contrario el precio
     *       ocupa su propia línea alineada completamente a la derecha.</li>
     * </ul>
     *
     * @param guarniciones Texto de guarniciones (ej: "Arroz, Frijoles")
     * @param precio       Precio ya formateado (ej: "$90.00")
     * @param anchoEfectivo Ancho útil en caracteres (usar ANCHO_TICKET/2 con FontSize._2)
     * @return Lista de líneas listas para imprimir
     */
    public List<String> formatearGuarniciones(String guarniciones, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();

        if (anchoEfectivo - guarniciones.length() - precio.length() >= 2) {
            lineas.add(construirLineaConPrecio(guarniciones, precio, anchoEfectivo));
            return lineas;
        }

        List<String> fragmentos = dividirTexto(guarniciones, anchoEfectivo);
        for (int i = 0; i < fragmentos.size() - 1; i++) {
            lineas.add(completarLinea(fragmentos.get(i), anchoEfectivo));
        }
        String ultima = fragmentos.get(fragmentos.size() - 1);
        if (anchoEfectivo - ultima.length() - precio.length() >= 2) {
            lineas.add(construirLineaConPrecio(ultima, precio, anchoEfectivo));
        } else {
            lineas.add(completarLinea(ultima, anchoEfectivo));
            lineas.add(alinearDerechaCompleto(precio, anchoEfectivo));
        }

        return lineas;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    protected String construirLineaConPrecio(String texto, String precio, int anchoEfectivo) {
        int espacios = anchoEfectivo - texto.length() - precio.length();
        StringBuilder linea = new StringBuilder(texto);
        for (int i = 0; i < Math.max(1, espacios); i++) {
            linea.append(' ');
        }
        linea.append(precio);
        return linea.toString();
    }

    protected String alinearDerechaCompleto(String texto, int anchoEfectivo) {
        int espacios = anchoEfectivo - texto.length();
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < Math.max(0, espacios); i++) {
            linea.append(' ');
        }
        linea.append(texto);
        return linea.toString();
    }

    protected List<String> dividirTexto(String texto, int anchoLinea) {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            int longitudConPalabra = lineaActual.length() + (lineaActual.length() > 0 ? 1 : 0) + palabra.length();
            if (longitudConPalabra > anchoLinea) {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder();
                }
                if (palabra.length() > anchoLinea) {
                    int inicio = 0;
                    while (inicio < palabra.length()) {
                        int fin = Math.min(inicio + anchoLinea, palabra.length());
                        lineas.add(palabra.substring(inicio, fin));
                        inicio = fin;
                    }
                } else {
                    lineaActual.append(palabra);
                }
            } else {
                if (lineaActual.length() > 0) {
                    lineaActual.append(' ');
                }
                lineaActual.append(palabra);
            }
        }

        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }

        return lineas;
    }

    protected String completarLinea(String texto, int ancho) {
        StringBuilder linea = new StringBuilder(texto);
        while (linea.length() < ancho) {
            linea.append(' ');
        }
        return linea.toString();
    }
}
