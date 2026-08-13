package com.cocinarubi.presentation.dto.response.estadisticas;

import java.math.BigDecimal;
import java.util.List;

public record CatalogoProductoEstadisticaDTO(
    int totalProductosVendidos, 
    BigDecimal totalVentas, 
    BigDecimal totalTransferencia, 
    BigDecimal totalTarjeta, 
    BigDecimal totalEfectivo,
    List<CatalogoProductoEstadisticaProducto> productos
) {}
