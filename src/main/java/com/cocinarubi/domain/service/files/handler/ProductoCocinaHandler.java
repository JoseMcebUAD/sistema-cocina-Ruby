package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;

public class ProductoCocinaHandler implements CatalogoProductoHandler {

    private final ProductoCocinaService productoCocinaService;
    private final TipoCatalogoProducto entityType;
    private final TipoProducto tipoProducto;

    public ProductoCocinaHandler(ProductoCocinaService productoCocinaService,
                                 TipoCatalogoProducto entityType,
                                 TipoProducto tipoProducto) {
        this.productoCocinaService = productoCocinaService;
        this.entityType = entityType;
        this.tipoProducto = tipoProducto;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return entityType;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        return productoCocinaService.existsByIdAndTipo(idEntidad, tipoProducto);
    }
}
