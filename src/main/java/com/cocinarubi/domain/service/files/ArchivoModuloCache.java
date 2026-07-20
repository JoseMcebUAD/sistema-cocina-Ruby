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
import java.util.List;
import java.util.Map;

/**
 * Caché en memoria de los registros de archivo_modulo. Al arrancar la aplicación
 * carga todos los módulos catalogados y pre-parsea sus MIME types aceptados para
 * evitar consultas repetidas a BD en cada solicitud de subida de archivo.
 */
@Component
public class ArchivoModuloCache {

    private final ArchivoModuloRepository archivoModuloRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<TipoCatalogoProducto, ArchivoModulo> modulosPorTipo =
            new EnumMap<>(TipoCatalogoProducto.class);
    private final Map<TipoCatalogoProducto, List<String>> mimesPorTipo =
            new EnumMap<>(TipoCatalogoProducto.class);

    public ArchivoModuloCache(ArchivoModuloRepository archivoModuloRepository) {
        this.archivoModuloRepository = archivoModuloRepository;
    }

    @PostConstruct
    void init() {
        // Carga todos los módulos con tipo asignado; uno por cada TipoCatalogoProducto catalogado
        List<ArchivoModulo> modulos = archivoModuloRepository.findByTipoCatalogoProductoIsNotNull();
        for (ArchivoModulo modulo : modulos) {
            TipoCatalogoProducto tipo = modulo.getTipoCatalogoProducto();
            modulosPorTipo.put(tipo, modulo);
            // Pre-parsea el JSON de MIME types una vez al arrancar para no repetirlo en cada petición
            mimesPorTipo.put(tipo, parseMimes(modulo.getArchivosAceptados()));
        }
    }

    public ArchivoModulo get(TipoCatalogoProducto tipo) {
        ArchivoModulo modulo = modulosPorTipo.get(tipo);
        if (modulo == null) {
            // Indica que falta la fila en archivo_modulo para ese tipo; error de configuración
            throw new BusinessException(
                    "No existe configuración de módulo para el tipo: " + tipo,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return modulo;
    }

    public List<String> allowedMimeTypes(TipoCatalogoProducto tipo) {
        return mimesPorTipo.getOrDefault(tipo, Collections.emptyList());
    }

    private List<String> parseMimes(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            // Deserializa el arreglo JSON de strings (ej. ["image/jpeg","image/png","image/webp"])
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(
                    "JSON inválido en archivo_modulo.archivos_aceptados: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
