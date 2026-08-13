package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Conteos totales de cada categoría de alimento para el día en curso.
 * Capa: DTO de respuesta — no extiende EstadisticaResponseDTO (es un KPI escalar sin gráfica).
 *
 * <p>El total de {@code ProductoCocina} ya no está desglosado en campos fijos
 * (totalSnacks / totalBebidas / ...) desde Fase 2, sino en el arreglo
 * {@code totalesProductosCocina} — una entrada por cada {@code Categoria} con al
 * menos un producto pedido en el día.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenProduccionResponseDTO {

    private LocalDate fecha;
    private long totalComidas;
    private long totalDesayunos;
    private long totalBasicos;
    private List<TotalPorCategoriaDTO> totalesProductosCocina;
}
