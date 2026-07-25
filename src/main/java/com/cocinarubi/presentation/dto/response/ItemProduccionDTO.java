package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un punto de datos en el detalle de producción: nombre del producto y cantidad total pedida hoy.
 * Capa: DTO de respuesta.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemProduccionDTO {

    private String nombre;
    private int cantidad;
}
