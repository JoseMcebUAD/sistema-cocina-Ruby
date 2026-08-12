package com.cocinarubi.presentation.dto.response.estadisticas;

import java.math.BigDecimal;

public record CatalogoProductoEstadisticaProducto(
    String categoria, 
    String nombreProducto, 
    BigDecimal totalVentas, 
    int totalProductos
) {}
