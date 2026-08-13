package com.cocinarubi.domain.service.files;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoModuloRepository;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caché en memoria de los registros de archivo_modulo. Al arrancar la aplicación
 * carga tanto los módulos estáticos (COMIDA / DESAYUNO / BASICO) como los dinámicos
 * (uno por Categoria) y pre-parsea sus MIME types aceptados para evitar consultas
 * repetidas a BD en cada subida de archivo.
 *
 * <p>El caché es mutable a través de {@link #refresh()} — el service de creación
 * de categorías lo invoca tras insertar/eliminar módulos dinámicos.</p>
 */
@Component
public class ArchivoModuloCache {

    private final ArchivoModuloRepository archivoModuloRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<TipoCatalogoProducto, ArchivoModulo> modulosPorTipo =
            new EnumMap<>(TipoCatalogoProducto.class);
    private final Map<TipoCatalogoProducto, List<String>> mimesPorTipo =
            new EnumMap<>(TipoCatalogoProducto.class);

    private final Map<Integer, ArchivoModulo> modulosPorCategoria = new HashMap<>();
    private final Map<Integer, List<String>> mimesPorCategoria = new HashMap<>();

    public ArchivoModuloCache(ArchivoModuloRepository archivoModuloRepository) {
        this.archivoModuloRepository = archivoModuloRepository;
    }

    @PostConstruct
    void init() {
        refresh();
    }

    /**
     * Recarga completamente los dos mapas desde BD. Invocado por
     * {@code ArchivoModuloService} cuando se crea/elimina un módulo dinámico
     * para que el nuevo estado sea visible sin reiniciar la app.
     */
    public synchronized void refresh() {
        modulosPorTipo.clear();
        mimesPorTipo.clear();
        modulosPorCategoria.clear();
        mimesPorCategoria.clear();

        for (ArchivoModulo modulo : archivoModuloRepository.findByTipoCatalogoProductoIsNotNull()) {
            TipoCatalogoProducto tipo = modulo.getTipoCatalogoProducto();
            modulosPorTipo.put(tipo, modulo);
            mimesPorTipo.put(tipo, parseMimes(modulo.getArchivosAceptados()));
        }
        for (ArchivoModulo modulo : archivoModuloRepository.findByCategoriaIsNotNull()) {
            Integer idCategoria = modulo.getCategoria().getIdCategoria();
            modulosPorCategoria.put(idCategoria, modulo);
            mimesPorCategoria.put(idCategoria, parseMimes(modulo.getArchivosAceptados()));
        }
    }

    public ArchivoModulo get(TipoCatalogoProducto tipo) {
        ArchivoModulo modulo = modulosPorTipo.get(tipo);
        if (modulo == null) {
            throw new BusinessException(
                    "No existe configuración de módulo para el tipo: " + tipo,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return modulo;
    }

    public ArchivoModulo get(Integer idCategoria) {
        ArchivoModulo modulo = modulosPorCategoria.get(idCategoria);
        if (modulo == null) {
            throw new BusinessException(
                    "No existe configuración de módulo para la categoría con id: " + idCategoria,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return modulo;
    }

    public List<String> allowedMimeTypes(TipoCatalogoProducto tipo) {
        return mimesPorTipo.getOrDefault(tipo, Collections.emptyList());
    }

    public List<String> allowedMimeTypes(Integer idCategoria) {
        return mimesPorCategoria.getOrDefault(idCategoria, Collections.emptyList());
    }

    private List<String> parseMimes(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(
                    "JSON inválido en archivo_modulo.archivos_aceptados: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
