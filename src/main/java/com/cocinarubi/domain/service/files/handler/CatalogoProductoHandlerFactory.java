package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory que centraliza el despacho de handlers por TipoCatalogoProducto.
 * Spring inyecta todos los @Component que implementen CatalogoProductoHandler
 * y los registra en un EnumMap en @PostConstruct.
 */
@Component
public class CatalogoProductoHandlerFactory {

    private final List<CatalogoProductoHandler> handlers;
    private final Map<TipoCatalogoProducto, CatalogoProductoHandler> registry =
            new EnumMap<>(TipoCatalogoProducto.class);

    public CatalogoProductoHandlerFactory(List<CatalogoProductoHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    void init() {
        // Registra cada handler usando su propio tipo como clave del mapa
        for (CatalogoProductoHandler handler : handlers) {
            registry.put(handler.getEntityType(), handler);
        }
    }

    public CatalogoProductoHandler resolve(TipoCatalogoProducto entityType) {
        CatalogoProductoHandler handler = registry.get(entityType);
        if (handler == null) {
            // entityType no tiene handler registrado; rechaza la petición con 400
            throw new BusinessException(
                    "Tipo de entidad no soportado para gestión de archivos: " + entityType,
                    HttpStatus.BAD_REQUEST);
        }
        return handler;
    }
}
