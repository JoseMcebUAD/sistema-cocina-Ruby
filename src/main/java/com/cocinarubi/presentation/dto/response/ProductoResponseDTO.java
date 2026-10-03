package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoMedida;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Integer idProducto;
    private String nombre;
    private TipoMedida tipoMedida;
    private String unidadBase;
    private BigDecimal pesoPromedioPieza;
    private BigDecimal contenidoPresentacion;
    private String unidadPresentacion;
    private BigDecimal costoPresentacion;
    private BigDecimal costoUnitarioBase;
}
