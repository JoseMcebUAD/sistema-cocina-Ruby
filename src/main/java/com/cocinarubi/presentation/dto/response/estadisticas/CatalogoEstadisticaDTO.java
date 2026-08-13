package com.cocinarubi.presentation.dto.response.estadisticas;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumen de ventas agrupadas por categoría de catálogo para el endpoint /estadisticas/catalogo.
 * Capa: Response DTO.
 */
public record CatalogoEstadisticaDTO(
    long totalProductosVendidos,
    BigDecimal totalVentas,
    BigDecimal totalTransferencias,
    BigDecimal totalEfectivo,
    BigDecimal totalTarjeta,
    List<CatalogoEstadisticaCategoria> categorias
) {}
