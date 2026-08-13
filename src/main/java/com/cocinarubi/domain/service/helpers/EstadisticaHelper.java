package com.cocinarubi.domain.service.helpers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import com.cocinarubi.exception.BusinessException;

public class EstadisticaHelper {

    /** Tipo de categoría de catálogo para el endpoint /estadisticas/catalogo/productos. */
    public enum TipoCategoriaCatalogo {
        COMIDA, BASICO, COMPLEMENTO, PAQUETE, DESAYUNO, PRODUCTO_COCINA
    }

    /**
     * Convierte el string de rango ("1H", "30M") a minutos.
     * Lanza BusinessException si el formato no es válido.
     */
    public int parsearRango(String rango) {
        if (rango == null || rango.isBlank()) {
            throw new BusinessException("El parámetro 'rango' es requerido", HttpStatus.BAD_REQUEST);
        }
        Matcher m = Pattern.compile("^(\\d+)(H|M)$", Pattern.CASE_INSENSITIVE).matcher(rango.trim());
        if (!m.matches()) {
            throw new BusinessException(
                    "Formato de rango inválido. Use NHH o NM (ej. 1H, 2H, 30M)",
                    HttpStatus.BAD_REQUEST);
        }
        int valor = Integer.parseInt(m.group(1));
        return m.group(2).equalsIgnoreCase("H") ? valor * 60 : valor;
    }

    /** Convierte índice de slot a etiqueta "HH:mm-HH:mm". */
    public String formatearSlot(int slotIndex, int rangoMinutos) {
        int inicioMin = slotIndex * rangoMinutos;
        int finMin = Math.min(inicioMin + rangoMinutos, 24 * 60);
        return String.format("%02d:%02d-%02d:%02d",
                inicioMin / 60, inicioMin % 60,
                finMin / 60, finMin % 60);
    }

    public void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        return new BigDecimal(obj.toString());
    }

    public BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

}
