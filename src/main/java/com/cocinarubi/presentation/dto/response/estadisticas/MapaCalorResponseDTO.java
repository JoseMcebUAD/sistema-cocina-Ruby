package com.cocinarubi.presentation.dto.response.estadisticas;

import java.util.List;

/**
 * Respuesta del endpoint {@code GET /estadisticas/mapa-calor}.
 * {@code totalPuntos} = coordenadas únicas; {@code totalPedidos} = suma de todas las entregas.
 */
public record MapaCalorResponseDTO(int totalPuntos, long totalPedidos, List<PuntoMapaCalorDTO> puntos) {}
