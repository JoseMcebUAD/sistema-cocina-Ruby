package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoMedida;
import lombok.*;

import java.math.BigDecimal;

/**
 * Fila del costeo escalado: cuánto se necesita de un ingrediente para N porciones
 * y cuánto cuesta usando el precio actual del {@link com.cocinarubi.domain.entity.Producto}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteCosteadoResponseDTO {

    private Integer idProducto;
    private String nombreProducto;
    private TipoMedida tipoMedida;
    private BigDecimal cantidadBase;
    private BigDecimal cantidadEscalada;
    private String unidadUsada;
    private BigDecimal costoUnitarioActual;
    private BigDecimal costoTotal;
    private Integer paquetesACompar;
    private boolean esAproximado;
}
