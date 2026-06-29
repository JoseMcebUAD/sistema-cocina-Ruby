package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;
import org.springframework.stereotype.Component;

/**
 * Handler para productos de cocina de tipo CHAROLA. Delega la verificación a
 * ProductoCocinaService filtrando también por TipoProducto para evitar colisiones entre subtipos.
 */
@Component
public class ProductoCocinaCharolaHandler implements CatalogoProductoHandler {

    private final ProductoCocinaService productoCocinaService;

    public ProductoCocinaCharolaHandler(ProductoCocinaService productoCocinaService) {
        this.productoCocinaService = productoCocinaService;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.CHAROLA;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Valida id + TipoProducto.CHAROLA para rechazar ids de SNACK o BEBIDA
        return productoCocinaService.existsByIdAndTipo(idEntidad, TipoProducto.CHAROLA);
    }
}
