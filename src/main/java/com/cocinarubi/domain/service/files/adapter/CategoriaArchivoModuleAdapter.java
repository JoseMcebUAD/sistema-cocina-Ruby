package com.cocinarubi.domain.service.files.adapter;

import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.entity.Categoria;
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
 * Adapter que resuelve todas las operaciones de archivos contra un módulo dinámico
 * ligado a una {@link Categoria}. Es package-private: solo se instancia desde
 * {@link ArchivoModuleAdapterFactory}, que además garantiza que la Categoria exista.
 */
class CategoriaArchivoModuleAdapter implements ArchivoModuleAdapter {

    private final Integer idCategoria;
    private final Categoria categoria;
    private final CatalogoProductoHandlerFactory handlerFactory;
    private final ArchivoModuloCache archivoModuloCache;
    private final ArchivoRepository archivoRepository;
    private final ArchivoUploader archivoUploader;

    CategoriaArchivoModuleAdapter(Categoria categoria,
                                  CatalogoProductoHandlerFactory handlerFactory,
                                  ArchivoModuloCache archivoModuloCache,
                                  ArchivoRepository archivoRepository,
                                  ArchivoUploader archivoUploader) {
        this.idCategoria = categoria.getIdCategoria();
        this.categoria = categoria;
        this.handlerFactory = handlerFactory;
        this.archivoModuloCache = archivoModuloCache;
        this.archivoRepository = archivoRepository;
        this.archivoUploader = archivoUploader;
    }

    /**
     * Valida que la entidad exista en la tabla de productos de la categoría, calcula
     * el siguiente orden correlativo y sube cada archivo a Cloudinary a través de
     * {@link ArchivoUploader}, persistiendo la fila en {@code archivo} con la FK
     * {@code id_categoria} en lugar del enum estático.
     */
    @Override
    @Transactional
    public List<ArchivoResponseDTO> upload(Integer idEntidad, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("Debe enviar al menos un archivo", HttpStatus.BAD_REQUEST);
        }

        // Handler universal: valida que la entidad exista en la tabla producto_cocina
        CatalogoProductoHandler handler = handlerFactory.resolve(idCategoria);
        if (!handler.exists(idEntidad)) {
            throw new BusinessException(
                    "No existe ProductoCocina con id: " + idEntidad,
                    HttpStatus.NOT_FOUND);
        }

        ArchivoModulo modulo = archivoModuloCache.get(idCategoria);
        List<String> mimesPermitidos = archivoModuloCache.allowedMimeTypes(idCategoria);
        int siguienteOrden = archivoRepository.findMaxOrdenForEntityCategoria(idCategoria, idEntidad) + 1;

        List<ArchivoResponseDTO> resultado = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            // ArchivoUploader: sube a Cloudinary y persiste la fila; entityType=null porque usa FK a categoría
            Archivo archivo = archivoUploader.subirYPersistir(file, modulo, mimesPermitidos,
                    siguienteOrden++, null, categoria, idEntidad);
            resultado.add(ArchivoResponseDTO.from(archivo));
        }
        return resultado;
    }

    @Override
    public List<ArchivoResponseDTO> getAll(Integer idEntidad) {
        return archivoRepository
                .findByCategoriaAndIdEntidadOrderByOrdenAsc(idCategoria, idEntidad)
                .stream()
                .map(ArchivoResponseDTO::from)
                .toList();
    }

    @Override
    public Map<Integer, List<ArchivoResponseDTO>> getAllBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return archivoRepository.findByCategoriaAndIdEntidadIn(idCategoria, ids)
                .stream()
                .map(ArchivoResponseDTO::from)
                .collect(Collectors.groupingBy(ArchivoResponseDTO::getIdEntidad));
    }

    @Override
    public Map<Integer, ArchivoResponseDTO> getPortadaBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return archivoRepository.findByCategoriaAndIdEntidadIn(idCategoria, ids)
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
     * Usa las variantes {@code *Categoria} del repositorio que filtran por FK en lugar de enum.
     */
    @Override
    @Transactional
    public ArchivoResponseDTO actualizarOrden(Integer idArchivo, Integer nuevoOrden) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));

        if (archivo.getCategoria() == null
                || !archivo.getCategoria().getIdCategoria().equals(idCategoria)) {
            throw new BusinessException(
                    "El archivo no pertenece a la categoría indicada", HttpStatus.BAD_REQUEST);
        }

        Integer origenOrden = archivo.getOrden();
        if (origenOrden.equals(nuevoOrden)) {
            return ArchivoResponseDTO.from(archivo);
        }

        Integer idEntidad = archivo.getIdEntidad();
        Integer maxOrden = archivoRepository.findMaxOrdenForEntityCategoria(idCategoria, idEntidad);
        if (nuevoOrden > maxOrden) {
            throw new BusinessException(
                    "El nuevo orden debe estar entre 1 y " + maxOrden, HttpStatus.BAD_REQUEST);
        }

        if (origenOrden > nuevoOrden) {
            // El archivo sube: los que estaban entre nuevoOrden y origenOrden-1 bajan un lugar
            archivoRepository.incrementOrdenBetweenCategoria(idCategoria, idEntidad, nuevoOrden, origenOrden - 1);
        } else {
            // El archivo baja: los que estaban entre origenOrden+1 y nuevoOrden suben un lugar
            archivoRepository.decrementOrdenBetweenCategoria(idCategoria, idEntidad, origenOrden + 1, nuevoOrden);
        }

        archivo.setOrden(nuevoOrden);
        return ArchivoResponseDTO.from(archivoRepository.save(archivo));
    }
}
