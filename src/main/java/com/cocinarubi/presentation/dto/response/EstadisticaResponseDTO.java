package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.presentation.dto.response.graficas.DatoGrafica;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Base para respuestas de estadísticas con gráfica.
 * Las subclases extienden esta clase para añadir campos específicos del concepto.
 * Capa: DTO — respuesta del módulo de estadísticas.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaResponseDTO {

    private String nombreEstadistica;
    private TipoEstadistica tipoEstadistica;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DatoGrafica> datos;
}
