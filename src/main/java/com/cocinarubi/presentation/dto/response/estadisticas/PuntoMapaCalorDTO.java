package com.cocinarubi.presentation.dto.response.estadisticas;

import java.math.BigDecimal;

/**
 * Punto geográfico agregado para el mapa de calor de entregas a domicilio.
 * {@code cantidad} representa el número de pedidos en esa coordenada exacta
 * y se usa como intensidad (weight) en Leaflet.heat.
 */
public record PuntoMapaCalorDTO(BigDecimal latitud, BigDecimal longitud, long cantidad) {}
