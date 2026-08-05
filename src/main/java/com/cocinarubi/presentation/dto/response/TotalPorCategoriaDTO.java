package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Total agregado de {@code ProductoCocina} pedidos hoy para una categoría.
 * Item del arreglo {@code totalesProductosCocina} en {@link ResumenProduccionResponseDTO}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotalPorCategoriaDTO {

    private int idCategoria;
    private String nombreCategoria;
    private long total;
}
