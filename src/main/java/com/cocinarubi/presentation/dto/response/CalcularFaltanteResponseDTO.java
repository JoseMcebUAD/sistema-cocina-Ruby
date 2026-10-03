package com.cocinarubi.presentation.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del endpoint {@code POST /comida/{id}/receta/calcular-faltante}:
 * trae la receta escalada + el detalle de faltantes dado el stock del usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcularFaltanteResponseDTO {

    private RecetaCosteoResponseDTO receta;
    private List<FaltanteIngredienteDTO> faltantes;
    private BigDecimal costoTotalFaltante;
    private boolean sePuedeProducir;
}
