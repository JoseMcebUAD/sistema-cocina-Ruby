package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoMedida;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payload para dar de alta o actualizar un {@code Producto} del catálogo de ingredientes.
 * El {@code costoUnitarioBase} NO se recibe: lo calcula el service como
 * {@code costoPresentacion / contenidoPresentacion}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @NotNull
    private TipoMedida tipoMedida;

    @NotBlank
    @Size(max = 10)
    private String unidadBase;

    // Obligatorio solo si tipoMedida = PIEZA_VARIABLE (lo valida la strategy).
    @DecimalMin(value = "0.00", inclusive = false, message = "pesoPromedioPieza debe ser > 0 cuando se envía")
    private BigDecimal pesoPromedioPieza;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal contenidoPresentacion;

    @NotBlank
    @Size(max = 10)
    private String unidadPresentacion;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal costoPresentacion;
}
