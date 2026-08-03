package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Rango de fechas disponibles para las consultas de estadísticas.
 * Contiene la fecha del primer y último pedido registrado en el sistema.
 * Capa: DTO — respuesta del endpoint /estadisticas/rango-fechas.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RangoFechasEstadisticaDTO {

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
