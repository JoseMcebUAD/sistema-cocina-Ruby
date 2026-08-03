package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Desglose de una categoría de producción: qué productos concretos se pidieron hoy y en qué cantidad.
 * Capa: DTO de respuesta.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetalleProduccionResponseDTO {

    private String categoria;
    private List<ItemProduccionDTO> items;
}
