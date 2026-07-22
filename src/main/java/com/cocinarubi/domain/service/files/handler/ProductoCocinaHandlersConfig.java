package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.service.ProductoCocinaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductoCocinaHandlersConfig {

    @Bean
    CatalogoProductoHandler snackHandler(ProductoCocinaService service) {
        return new ProductoCocinaHandler(service, TipoCatalogoProducto.SNACK, TipoProducto.SNACK);
    }

    @Bean
    CatalogoProductoHandler charolaHandler(ProductoCocinaService service) {
        return new ProductoCocinaHandler(service, TipoCatalogoProducto.CHAROLA, TipoProducto.CHAROLA);
    }

    @Bean
    CatalogoProductoHandler bebidaHandler(ProductoCocinaService service) {
        return new ProductoCocinaHandler(service, TipoCatalogoProducto.BEBIDA, TipoProducto.BEBIDA);
    }

    @Bean
    CatalogoProductoHandler postreHandler(ProductoCocinaService service) {
        return new ProductoCocinaHandler(service, TipoCatalogoProducto.POSTRE, TipoProducto.POSTRE);
    }
}
