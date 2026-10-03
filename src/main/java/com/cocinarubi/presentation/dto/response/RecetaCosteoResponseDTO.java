package com.cocinarubi.presentation.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del endpoint {@code GET /comida/{id}/receta/costear}: receta escalada
 * a {@code porcionesDeseadas} con costos y paquetes a comprar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaCosteoResponseDTO {

    private Integer idComida;
    private String nombreComida;
    private Integer porcionesBase;
    private Integer porcionesDeseadas;
    private BigDecimal factorEscala;
    private List<IngredienteCosteadoResponseDTO> ingredientes;
    private BigDecimal costoTotalReceta;
    private BigDecimal costoPorPorcion;
}
