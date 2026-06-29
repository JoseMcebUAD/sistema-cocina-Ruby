package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;
import org.springframework.stereotype.Component;

/**
 * Handler para productos de cocina de tipo BEBIDA. Delega la verificación a
 * ProductoCocinaService filtrando también por TipoProducto para evitar colisiones entre subtipos.
 */
@Component
public class ProductoCocinaBebidaHandler implements CatalogoProductoHandler {

    private final ProductoCocinaService productoCocinaService;

    public ProductoCocinaBebidaHandler(ProductoCocinaService productoCocinaService) {
        this.productoCocinaService = productoCocinaService;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.BEBIDA;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Valida id + TipoProducto.BEBIDA para rechazar ids de SNACK o CHAROLA
        return productoCocinaService.existsByIdAndTipo(idEntidad, TipoProducto.BEBIDA);
    }
}
