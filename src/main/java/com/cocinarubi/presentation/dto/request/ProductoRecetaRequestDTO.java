package com.cocinarubi.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payload para agregar o actualizar un ingrediente de la receta de una comida.
 * El {@code idComida} llega por path parameter, no en el body.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRecetaRequestDTO {

    @NotNull
    @Positive
    private Integer idProducto;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal cantidadUsada;

    @NotBlank
    @Size(max = 10)
    private String unidadUsada;

    private Boolean esAproximado;
}
