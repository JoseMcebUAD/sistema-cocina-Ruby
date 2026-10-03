package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoMedida;
import lombok.*;

import java.math.BigDecimal;

/**
 * Response de los endpoints CRUD de ingredientes de receta. Los campos {@code *Snapshot}
 * son los valores guardados al dar de alta el ingrediente (historial);
 * los endpoints de costeo/faltante calculan con precios actuales de {@code Producto}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRecetaResponseDTO {

    private Integer idProductoReceta;
    private Integer idComida;
    private Integer idProducto;
    private String nombreProducto;
    private TipoMedida tipoMedida;
    private BigDecimal cantidadUsada;
    private String unidadUsada;
    private boolean esAproximado;
    private BigDecimal costoUnitarioSnapshot;
    private BigDecimal costoTotalSnapshot;
}
