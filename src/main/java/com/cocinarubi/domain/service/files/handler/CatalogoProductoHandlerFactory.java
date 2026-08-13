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
 * Factory que despacha handlers de archivos. Soporta dos vías de resolución:
 * <ul>
 *   <li>Por {@link TipoCatalogoProducto} — módulos estáticos (COMIDA, DESAYUNO, BASICO).</li>
 *   <li>Por {@code idCategoria} — módulos dinámicos generados al crear una
 *       {@link com.cocinarubi.domain.entity.Categoria}. Un único handler universal
 *       sirve a todos ellos porque comparten la tabla {@code producto_cocina}.</li>
 * </ul>
 *
 * <p>Spring inyecta todos los {@code @Component} que implementan
 * {@link CatalogoProductoHandler}. El {@code @PostConstruct} los clasifica: los que
 * devuelven un enum no-nulo en {@link CatalogoProductoHandler#getEntityType()} van al
 * EnumMap; el que devuelve {@code null} queda como universal.</p>
 */
@Component
public class CatalogoProductoHandlerFactory {

    private final List<CatalogoProductoHandler> handlers;
    private final Map<TipoCatalogoProducto, CatalogoProductoHandler> registryEstatico =
            new EnumMap<>(TipoCatalogoProducto.class);
    private CatalogoProductoHandler universal;

    public CatalogoProductoHandlerFactory(List<CatalogoProductoHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    void init() {
        for (CatalogoProductoHandler handler : handlers) {
            TipoCatalogoProducto tipo = handler.getEntityType();
            if (tipo == null) {
                // Handler universal (ProductoCocinaHandler) — sirve a cualquier idCategoria
                universal = handler;
            } else {
                registryEstatico.put(tipo, handler);
            }
        }
    }

    /** Resuelve el handler para un módulo estático (COMIDA/DESAYUNO/BASICO). */
    public CatalogoProductoHandler resolve(TipoCatalogoProducto entityType) {
        CatalogoProductoHandler handler = registryEstatico.get(entityType);
        if (handler == null) {
            throw new BusinessException(
                    "Tipo de entidad no soportado para gestión de archivos: " + entityType,
                    HttpStatus.BAD_REQUEST);
        }
        return handler;
    }

    /** Resuelve el handler universal para un módulo dinámico por categoría. */
    public CatalogoProductoHandler resolve(Integer idCategoria) {
        if (universal == null) {
            // Configuración incorrecta: falta el ProductoCocinaHandler
            throw new BusinessException(
                    "No hay handler universal registrado para categorías dinámicas",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return universal;
    }
}
