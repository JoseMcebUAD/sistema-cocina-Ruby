package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Conteos totales de cada categoría de alimento para el día en curso.
 * Capa: DTO de respuesta — no extiende EstadisticaResponseDTO (es un KPI escalar sin gráfica).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenProduccionResponseDTO {

    private LocalDate fecha;
    private long totalComidas;
    private long totalDesayunos;
    private long totalSnacks;
    private long totalBebidas;
    private long totalCharolas;
    private long totalPostres;
    private long totalBasicos;
}
