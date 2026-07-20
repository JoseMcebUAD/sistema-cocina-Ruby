package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;
import org.springframework.stereotype.Component;

/**
 * Handler para productos de cocina de tipo SNACK. Delega la verificación a
 * ProductoCocinaService filtrando también por TipoProducto para evitar colisiones entre subtipos.
 */
@Component
public class ProductoCocinaSnackHandler implements CatalogoProductoHandler {

    private final ProductoCocinaService productoCocinaService;

    public ProductoCocinaSnackHandler(ProductoCocinaService productoCocinaService) {
        this.productoCocinaService = productoCocinaService;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.SNACK;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Valida id + TipoProducto.SNACK para rechazar ids de CHAROLA o BEBIDA
        return productoCocinaService.existsByIdAndTipo(idEntidad, TipoProducto.SNACK);
    }
}
