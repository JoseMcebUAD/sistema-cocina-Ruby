package com.cocinarubi.domain.interfaces;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Contrato del servicio de gestión de archivos. Define las operaciones CRUD
 * disponibles para cualquier entidad del catálogo de productos sobre Cloudinary.
 */
public interface FileUploadService {

    // Sube uno o más archivos a Cloudinary y los vincula a la entidad indicada en meta
    List<ArchivoResponseDTO> upload(FileUploadRequestDTO meta, MultipartFile[] files);

    // Retorna todos los archivos de una entidad ordenados por campo 'orden'
    List<ArchivoResponseDTO> getAll(TipoCatalogoProducto entityType, Integer idEntidad);

    // Retorna un archivo específico por su id en la tabla archivo
    ArchivoResponseDTO getOne(Integer idArchivo);

    // Elimina el archivo en Cloudinary y su registro en BD
    void delete(Integer idArchivo);

    // Retorna archivos de varias entidades del mismo tipo, agrupados por idEntidad
    Map<Integer, List<ArchivoResponseDTO>> getAllBatch(TipoCatalogoProducto entityType, List<Integer> ids);

    // Retorna solo la imagen de menor orden por entidad (portada para listados)
    Map<Integer, ArchivoResponseDTO> getPortadaBatch(TipoCatalogoProducto entityType, List<Integer> ids);
}
