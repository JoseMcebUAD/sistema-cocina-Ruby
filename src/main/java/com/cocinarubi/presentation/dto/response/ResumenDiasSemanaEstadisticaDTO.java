package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

import com.cocinarubi.presentation.dto.response.graficas.DatoGraficaDia;

/**
 * Resumen de pedidos e ingresos agrupados por día de semana (Lun–Dom).
 * Siempre incluye los 7 días; días sin pedidos llevan valores en cero.
 * Capa: DTO — respuesta del endpoint /estadisticas/resumen-dias-semana.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDiasSemanaEstadisticaDTO extends EstadisticaResponseDTO {

    private int totalPedidos;
    private BigDecimal ingresoTotal;
    private List<DatoGraficaDia> dias;
}
