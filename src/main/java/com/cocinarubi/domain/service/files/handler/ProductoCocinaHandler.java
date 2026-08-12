package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;
import org.springframework.stereotype.Component;

/**
 * Handler universal para todas las categorías dinámicas de ProductoCocina.
 * Comparten la misma tabla, así que una única instancia sirve a cualquier
 * {@code idCategoria}. Se registra en el factory por el slot "universal"
 * ({@link #getEntityType()} devuelve {@code null}).
 */
@Component
public class ProductoCocinaHandler implements CatalogoProductoHandler {

    private final ProductoCocinaService productoCocinaService;

    public ProductoCocinaHandler(ProductoCocinaService productoCocinaService) {
        this.productoCocinaService = productoCocinaService;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        // Universal — no atado a un enum estático.
        return null;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        return productoCocinaService.existsById(idEntidad);
    }
}
