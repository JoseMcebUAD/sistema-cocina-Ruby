package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Resumen de pedidos agrupados por franjas horarias del día.
 * Los slots se almacenan en el campo heredado {@code datos} (leyenda = "HH:mm-HH:mm", valor = cantidadPedidos).
 * Siempre incluye todos los slots del día; franjas sin pedidos llevan valor en cero.
 * Capa: DTO — respuesta del endpoint /estadisticas/resumen-horario.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResumenHorarioEstadisticaDTO extends EstadisticaResponseDTO {

    private int totalPedidos;
    private String rango;
}
