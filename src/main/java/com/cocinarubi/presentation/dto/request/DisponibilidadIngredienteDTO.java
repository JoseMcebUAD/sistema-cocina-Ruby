package com.cocinarubi.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entrada del endpoint de cálculo de faltante: cuánto tiene el usuario disponible
 * de un ingrediente concreto. La cantidad admite 0 ("no tengo nada de este").
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadIngredienteDTO {

    @NotNull
    @Positive
    private Integer idProducto;

    @NotNull
    @DecimalMin(value = "0.000")
    private BigDecimal cantidad;
}
