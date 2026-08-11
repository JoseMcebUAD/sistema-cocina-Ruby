package com.cocinarubi.presentation.dto.response.estadisticas;

public record CatalogoEstadisticaCategoria(
    String nombreCategoria,
    Long totalVendido,
    int totalProductos
) {}
