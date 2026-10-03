package com.cocinarubi.presentation.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Diferencia entre lo necesario y lo disponible para un ingrediente concreto,
 * con el costo y los paquetes a comprar para cubrirla.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaltanteIngredienteDTO {

    private Integer idProducto;
    private String nombreProducto;
    private BigDecimal cantidadNecesaria;
    private BigDecimal cantidadDisponible;
    private BigDecimal cantidadFaltante;
    private BigDecimal costoFaltante;
    private Integer paquetesACompar;
}
