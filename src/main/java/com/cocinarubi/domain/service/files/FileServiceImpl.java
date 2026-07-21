package com.cocinarubi.domain.service.files;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.interfaces.FileUploadService;
import com.cocinarubi.domain.service.files.handler.CatalogoProductoHandler;
import com.cocinarubi.domain.service.files.handler.CatalogoProductoHandlerFactory;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio principal de gestión de archivos en Cloudinary. Orquesta validación,
 * subida al proveedor externo y persistencia en la tabla archivo para cualquier
 * entidad del catálogo de productos.
 */
@Service
public class FileServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;
    private final CatalogoProductoHandlerFactory handlerFactory;
    private final ArchivoModuloCache archivoModuloCache;
    private final ArchivoRepository archivoRepository;

    public FileServiceImpl(Cloudinary cloudinary,
                           CatalogoProductoHandlerFactory handlerFactory,
                           ArchivoModuloCache archivoModuloCache,
                           ArchivoRepository archivoRepository) {
        this.cloudinary = cloudinary;
        this.handlerFactory = handlerFactory;
        this.archivoModuloCache = archivoModuloCache;
        this.archivoRepository = archivoRepository;
    }

    @Override
    @Transactional
    public List<ArchivoResponseDTO> upload(FileUploadRequestDTO meta, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("Debe enviar al menos un archivo", HttpStatus.BAD_REQUEST);
        }

        TipoCatalogoProducto entityType = meta.getEntityType();
        Integer idEntidad = meta.getIdEntidad();

        // Resuelve el handler del tipo de entidad; lanza 400 si no está registrado
        CatalogoProductoHandler handler = handlerFactory.resolve(entityType);
        // Verifica que la entidad padre exista antes de subir archivos huérfanos
        if (!handler.exists(idEntidad)) {
            throw new BusinessException(
                    "No existe " + entityType + " con id: " + idEntidad,
                    HttpStatus.NOT_FOUND);
        }

        // Obtiene carpeta destino y MIME types permitidos desde el caché en memoria
        ArchivoModulo modulo = archivoModuloCache.get(entityType);
        List<String> mimesPermitidos = archivoModuloCache.allowedMimeTypes(entityType);

        // Calcula el siguiente valor de orden para no pisar registros previos de la entidad
        int siguienteOrden = archivoRepository.findMaxOrdenForEntity(entityType, idEntidad) + 1;

        List<ArchivoResponseDTO> resultado = new ArrayList<>(files.length);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("Archivo vacío en la petición", HttpStatus.BAD_REQUEST);
            }

            String contentType = file.getContentType();
            // Rechaza MIME types no permitidos por el módulo antes de consumir red
            if (contentType == null || !mimesPermitidos.contains(contentType)) {
                throw new BusinessException(
                        "Tipo de archivo no permitido para " + entityType + ": "
                                + contentType + ". Permitidos: " + mimesPermitidos,
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }

            Map<?, ?> resultUpload;
            try {
                // Sube el archivo a Cloudinary dentro de la carpeta configurada en archivo_modulo
                resultUpload = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("folder", modulo.getRuta()));
            } catch (Exception e) {
                throw new BusinessException(
                        "Error al subir el archivo a Cloudinary: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Extrae URL pública y public_id de la respuesta de Cloudinary
            String secureUrl = String.valueOf(resultUpload.get("secure_url"));
            String publicId = String.valueOf(resultUpload.get("public_id"));

            Archivo archivo = Archivo.builder()
                    .archivoModulo(modulo)
                    .pathArchivo(secureUrl)
                    .mimeType(contentType)
                    .nombreArchivo(file.getOriginalFilename() != null
                            ? file.getOriginalFilename() : publicId)
                    .orden(siguienteOrden++)
                    .entityType(entityType)
                    .idEntidad(idEntidad)
                    .publicId(publicId)
                    .creadoEn(LocalDateTime.now())
                    .build();

            // Persiste los metadatos del archivo recién subido en la tabla archivo
            archivo = archivoRepository.save(archivo);
            resultado.add(ArchivoResponseDTO.from(archivo));
        }

        return resultado;
    }

    @Override
    public List<ArchivoResponseDTO> getAll(TipoCatalogoProducto entityType, Integer idEntidad) {
        return archivoRepository
                .findByEntityTypeAndIdEntidadOrderByOrdenAsc(entityType, idEntidad)
                .stream()
                .map(ArchivoResponseDTO::from)
                .toList();
    }

    @Override
    public ArchivoResponseDTO getOne(Integer idArchivo) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));
        return ArchivoResponseDTO.from(archivo);
    }

    @Override
    public Map<Integer, List<ArchivoResponseDTO>> getAllBatch(TipoCatalogoProducto entityType, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return archivoRepository.findByEntityTypeAndIdEntidadIn(entityType, ids)
                .stream()
                .map(ArchivoResponseDTO::from)
                .collect(Collectors.groupingBy(ArchivoResponseDTO::getIdEntidad));
    }

    @Override
    public Map<Integer, ArchivoResponseDTO> getPortadaBatch(TipoCatalogoProducto entityType, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        // La query ya ordena por idEntidad ASC, orden ASC; el merge keeper descarta duplicados posteriores
        return archivoRepository.findByEntityTypeAndIdEntidadIn(entityType, ids)
                .stream()
                .map(ArchivoResponseDTO::from)
                .collect(Collectors.toMap(
                        ArchivoResponseDTO::getIdEntidad,
                        dto -> dto,
                        (primero, siguiente) -> primero
                ));
    }

    @Override
    @Transactional
    public ArchivoResponseDTO actualizarOrden(TipoCatalogoProducto entityType, Integer idArchivo, Integer nuevoOrden) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));

        if (!archivo.getEntityType().equals(entityType)) {
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
            // El archivo sube: todos los que estaban en [nuevoOrden, origenOrden-1] ceden su lugar
            archivoRepository.incrementOrdenBetween(entityType, idEntidad, nuevoOrden, origenOrden - 1);
        } else {
            // El archivo baja: todos los que estaban en [origenOrden+1, nuevoOrden] llenan el hueco
            archivoRepository.decrementOrdenBetween(entityType, idEntidad, origenOrden + 1, nuevoOrden);
        }

        archivo.setOrden(nuevoOrden);
        return ArchivoResponseDTO.from(archivoRepository.save(archivo));
    }

    @Override
    @Transactional
    public void delete(Integer idArchivo) {
        // Lanza 404 si el archivo no existe antes de intentar eliminarlo en Cloudinary
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));

        try {
            // Elimina el recurso en Cloudinary usando el public_id almacenado en BD
            cloudinary.uploader().destroy(archivo.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new BusinessException(
                    "Error al eliminar el archivo en Cloudinary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Elimina el registro de BD solo después de confirmar la eliminación en Cloudinary
        archivoRepository.delete(archivo);
    }
}
