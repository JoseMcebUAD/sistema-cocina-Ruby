package com.cocinarubi.domain.service.files.adapter;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.service.files.ArchivoModuloCache;
import com.cocinarubi.domain.service.files.ArchivoUploader;
import com.cocinarubi.domain.service.files.handler.CatalogoProductoHandler;
import com.cocinarubi.domain.service.files.handler.CatalogoProductoHandlerFactory;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter que resuelve todas las operaciones de archivos contra un módulo estático
 * ({@link TipoCatalogoProducto}). Es package-private: solo se instancia desde
 * {@link ArchivoModuleAdapterFactory}.
 */
class EstaticoArchivoModuleAdapter implements ArchivoModuleAdapter {

    private final TipoCatalogoProducto entityType;
    private final CatalogoProductoHandlerFactory handlerFactory;
    private final ArchivoModuloCache archivoModuloCache;
    private final ArchivoRepository archivoRepository;
    private final ArchivoUploader archivoUploader;

    EstaticoArchivoModuleAdapter(TipoCatalogoProducto entityType,
                                 CatalogoProductoHandlerFactory handlerFactory,
                                 ArchivoModuloCache archivoModuloCache,
                                 ArchivoRepository archivoRepository,
                                 ArchivoUploader archivoUploader) {
        this.entityType = entityType;
        this.handlerFactory = handlerFactory;
        this.archivoModuloCache = archivoModuloCache;
        this.archivoRepository = archivoRepository;
        this.archivoUploader = archivoUploader;
    }

    /**
     * Valida que la entidad exista, calcula el siguiente orden correlativo y sube
     * cada archivo a Cloudinary a través de {@link ArchivoUploader}, persistiendo
     * la fila en {@code archivo} con {@code entity_type = entityType}.
     */
    @Override
    @Transactional
    public List<ArchivoResponseDTO> upload(Integer idEntidad, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("Debe enviar al menos un archivo", HttpStatus.BAD_REQUEST);
        }

        // Handler estático: valida que la entidad destino exista en su tabla propia
        CatalogoProductoHandler handler = handlerFactory.resolve(entityType);
        if (!handler.exists(idEntidad)) {
            throw new BusinessException(
                    "No existe " + entityType + " con id: " + idEntidad,
                    HttpStatus.NOT_FOUND);
        }

        ArchivoModulo modulo = archivoModuloCache.get(entityType);
        List<String> mimesPermitidos = archivoModuloCache.allowedMimeTypes(entityType);
        int siguienteOrden = archivoRepository.findMaxOrdenForEntity(entityType, idEntidad) + 1;

        List<ArchivoResponseDTO> resultado = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            // ArchivoUploader: sube a Cloudinary y persiste la fila en tabla archivo
            Archivo archivo = archivoUploader.subirYPersistir(file, modulo, mimesPermitidos,
                    siguienteOrden++, entityType, null, idEntidad);
            resultado.add(ArchivoResponseDTO.from(archivo));
        }
        return resultado;
    }

    @Override
    public List<ArchivoResponseDTO> getAll(Integer idEntidad) {
        return archivoRepository
                .findByEntityTypeAndIdEntidadOrderByOrdenAsc(entityType, idEntidad)
                .stream()
                .map(ArchivoResponseDTO::from)
                .toList();
    }

    @Override
    public Map<Integer, List<ArchivoResponseDTO>> getAllBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return archivoRepository.findByEntityTypeAndIdEntidadIn(entityType, ids)
                .stream()
                .map(ArchivoResponseDTO::from)
                .collect(Collectors.groupingBy(ArchivoResponseDTO::getIdEntidad));
    }

    @Override
    public Map<Integer, ArchivoResponseDTO> getPortadaBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return archivoRepository.findByEntityTypeAndIdEntidadIn(entityType, ids)
                .stream()
                .map(ArchivoResponseDTO::from)
                .collect(Collectors.toMap(
                        ArchivoResponseDTO::getIdEntidad,
                        dto -> dto,
                        (primero, siguiente) -> primero // colisión: mantener el de menor orden (primero en la consulta)
                ));
    }

    /**
     * Reposiciona el archivo al {@code nuevoOrden} usando el algoritmo shift:
     * si sube (origenOrden > nuevoOrden) incrementa en 1 los archivos del rango
     * [nuevoOrden, origenOrden-1]; si baja, los decrementa en [origenOrden+1, nuevoOrden].
     * Garantiza que el orden siempre sea contiguo sin huecos.
     */
    @Override
    @Transactional
    public ArchivoResponseDTO actualizarOrden(Integer idArchivo, Integer nuevoOrden) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));

        if (archivo.getEntityType() == null || !archivo.getEntityType().equals(entityType)) {
            throw new BusinessException(
                    "El archivo no pertenece al tipo de entidad indicado", HttpStatus.BAD_REQUEST);
        }

        Integer origenOrden = archivo.getOrden();
        if (origenOrden.equals(nuevoOrden)) {
            return ArchivoResponseDTO.from(archivo);
        }

        Integer idEntidad = archivo.getIdEntidad();
        Integer maxOrden = archivoRepository.findMaxOrdenForEntity(entityType, idEntidad);
        if (nuevoOrden > maxOrden) {
            throw new BusinessException(
                    "El nuevo orden debe estar entre 1 y " + maxOrden, HttpStatus.BAD_REQUEST);
        }

        if (origenOrden > nuevoOrden) {
            // El archivo sube: los que estaban entre nuevoOrden y origenOrden-1 bajan un lugar
            archivoRepository.incrementOrdenBetween(entityType, idEntidad, nuevoOrden, origenOrden - 1);
        } else {
            // El archivo baja: los que estaban entre origenOrden+1 y nuevoOrden suben un lugar
            archivoRepository.decrementOrdenBetween(entityType, idEntidad, origenOrden + 1, nuevoOrden);
        }

        archivo.setOrden(nuevoOrden);
        return ArchivoResponseDTO.from(archivoRepository.save(archivo));
    }
}
