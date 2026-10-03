package com.cocinarubi.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

/**
 * Request del endpoint {@code POST /comida/{idComida}/receta/calcular-faltante}.
 * Lleva el número de porciones a producir y el stock que el usuario ya tiene.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcularFaltanteRequestDTO {

    @NotNull
    @Positive
    private Integer porcionesDeseadas;

    @NotNull
    @Valid
    private List<DisponibilidadIngredienteDTO> disponibles;
}
