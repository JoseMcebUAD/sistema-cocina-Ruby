package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;

/**
 * Contrato que debe implementar cada handler de entidad del catálogo de productos.
 * El factory lo usa para registrar y despachar al handler correcto según entityType.
 */
public interface CatalogoProductoHandler {

    // Identifica qué TipoCatalogoProducto gestiona esta implementación
    TipoCatalogoProducto getEntityType();

    // Devuelve true si la entidad con el id dado existe en su tabla correspondiente
    boolean exists(Integer idEntidad);
}
