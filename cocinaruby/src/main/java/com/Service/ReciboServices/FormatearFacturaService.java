package com.Service.ReciboServices;

import java.util.ArrayList;
import java.util.List;

import com.Config.Constants;

/**
 * Clase utilitaria para formatear líneas de factura térmica.
 * Maneja el formato de texto con precios alineados a 40 caracteres.
 */
public class FormatearFacturaService {

    /**
     * Formatea el detalle de una orden, dividiendo en múltiples líneas si es necesario.
     * El precio siempre aparece al final de la última línea.
     * Cada línea ocupa exactamente 40 caracteres.
     *
     * @param especificaciones Descripción del producto
     * @param precio Precio formateado
     * @return Lista de líneas formateadas (1 o más líneas)
     */
    public List<String> formatearDetalleOrden(String especificaciones, String precio) {
        List<String> lineas = new ArrayList<>();
        int espacioParaPrecio = precio.length() + 1; // +1 para al menos un espacio antes del precio
        int anchoParaTexto = Constants.ANCHO_TICKET - espacioParaPrecio;

        // Si el texto cabe en una sola línea con el precio
        if (especificaciones.length() <= anchoParaTexto) {
            // Formatear en una línea: texto + espacios + precio
            int espaciosNecesarios = Constants.ANCHO_TICKET - especificaciones.length() - precio.length();
            StringBuilder linea = new StringBuilder(especificaciones);
            for (int i = 0; i < espaciosNecesarios; i++) {
                linea.append(" ");
            }
            linea.append(precio);
            lineas.add(linea.toString());
        } else {
            // Dividir el texto en múltiples líneas
            List<String> lineasTexto = dividirTexto(especificaciones, Constants.ANCHO_TICKET);

            // Agregar todas las líneas excepto la última
            for (int i = 0; i < lineasTexto.size() - 1; i++) {
                lineas.add(completarLinea(lineasTexto.get(i), Constants.ANCHO_TICKET));
            }

            // La última línea lleva el precio
            String ultimaLinea = lineasTexto.get(lineasTexto.size() - 1);
            int espaciosNecesarios = Constants.ANCHO_TICKET - ultimaLinea.length() - precio.length();
            StringBuilder lineaConPrecio = new StringBuilder(ultimaLinea);
            for (int i = 0; i < espaciosNecesarios; i++) {
                lineaConPrecio.append(" ");
            }
            lineaConPrecio.append(precio);
            lineas.add(lineaConPrecio.toString());
        }

        return lineas;
    }

    /**
     * Formatea una línea de total con texto y precio alineados.
     * Útil para la línea "TOTAL" de la factura.
     *
     * @param texto Texto a la izquierda (ej: "TOTAL")
     * @param precio Precio formateado
     * @return Línea de exactamente 40 caracteres
     */
    public String formatearLineaTotal(String texto, String precio) {
        int espaciosNecesarios = Constants.ANCHO_TICKET - texto.length() - precio.length();
        StringBuilder linea = new StringBuilder(texto);
        for (int i = 0; i < espaciosNecesarios; i++) {
            linea.append(" ");
        }
        linea.append(precio);
        return linea.toString();
    }

    /**
     * Divide un texto largo en líneas de longitud específica,
     * intentando cortar por palabras completas cuando sea posible.
     *
     * @param texto Texto a dividir
     * @param anchoLinea Ancho máximo de cada línea
     * @return Lista de líneas
     */
    private List<String> dividirTexto(String texto, int anchoLinea) {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            // Si agregar esta palabra excede el ancho
            if (lineaActual.length() + palabra.length() + 1 > anchoLinea) {
                // Si la línea actual tiene contenido, la guardamos
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder();
                }

                // Si la palabra sola es más larga que el ancho, la cortamos
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
                // Agregar la palabra a la línea actual
                if (lineaActual.length() > 0) {
                    lineaActual.append(" ");
                }
                lineaActual.append(palabra);
            }
        }

        // Agregar la última línea si tiene contenido
        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }

        return lineas;
    }

    /**
     * Completa una línea con espacios hasta alcanzar el ancho especificado.
     *
     * @param texto Texto de la línea
     * @param ancho Ancho total deseado
     * @return Línea completada con espacios
     */
    private String completarLinea(String texto, int ancho) {
        StringBuilder linea = new StringBuilder(texto);
        while (linea.length() < ancho) {
            linea.append(" ");
        }
        return linea.toString();
    }
}
