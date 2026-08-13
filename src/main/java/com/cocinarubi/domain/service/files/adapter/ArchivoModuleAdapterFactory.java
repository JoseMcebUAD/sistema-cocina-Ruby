package com.cocinarubi.domain.service.files.adapter;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.service.files.ArchivoModuloCache;
import com.cocinarubi.domain.service.files.ArchivoUploader;
import com.cocinarubi.domain.service.files.handler.CatalogoProductoHandlerFactory;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Factory que devuelve el {@link ArchivoModuleAdapter} apropiado según el
 * discriminador que traiga la petición. Dos overloads simétricos con
 * {@code CatalogoProductoHandlerFactory} y {@code ArchivoModuloCache}.
 * Capa: Service.
 */
@Component
public class ArchivoModuleAdapterFactory {

    private final CatalogoProductoHandlerFactory handlerFactory;
    private final ArchivoModuloCache archivoModuloCache;
    private final ArchivoRepository archivoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ArchivoUploader archivoUploader;

    public ArchivoModuleAdapterFactory(CatalogoProductoHandlerFactory handlerFactory,
                                       ArchivoModuloCache archivoModuloCache,
                                       ArchivoRepository archivoRepository,
                                       CategoriaRepository categoriaRepository,
                                       ArchivoUploader archivoUploader) {
        this.handlerFactory = handlerFactory;
        this.archivoModuloCache = archivoModuloCache;
        this.archivoRepository = archivoRepository;
        this.categoriaRepository = categoriaRepository;
        this.archivoUploader = archivoUploader;
    }

    /** Adapter para un módulo estático (COMIDA/DESAYUNO/BASICO/EXTRAS). */
    public ArchivoModuleAdapter resolve(TipoCatalogoProducto entityType) {
        if (entityType == null) {
            throw new BusinessException("entityType requerido", HttpStatus.BAD_REQUEST);
        }
        return new EstaticoArchivoModuleAdapter(entityType, handlerFactory,
                archivoModuloCache, archivoRepository, archivoUploader);
    }

    /** Adapter para un módulo dinámico ligado a una Categoria. */
    public ArchivoModuleAdapter resolve(Integer idCategoria) {
        if (idCategoria == null) {
            throw new BusinessException("idCategoria requerido", HttpStatus.BAD_REQUEST);
        }
        // CategoriaRepository: valida existencia + carga la entidad para setear FK en Archivo
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new BusinessException(
                        "Categoría no encontrada con id: " + idCategoria, HttpStatus.NOT_FOUND));
        return new CategoriaArchivoModuleAdapter(categoria, handlerFactory,
                archivoModuloCache, archivoRepository, archivoUploader);
    }
}
