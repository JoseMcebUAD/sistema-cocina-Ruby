package com.cocinarubi.domain.interfaces;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.service.files.adapter.ArchivoModuleAdapter;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Fachada del subsistema de archivos. Las operaciones que dependen del
 * discriminador (enum estático XOR FK a categoría) se resuelven pidiendo el
 * {@link ArchivoModuleAdapter} vía {@link #forModule(TipoCatalogoProducto, Integer)}.
 * Las operaciones neutrales ({@link #getOne(Integer)}, {@link #delete(Integer)})
 * viven directamente aquí porque solo requieren {@code idArchivo}.
 * Capa: Service.
 */
public interface ArchivoService {

    /**
     * Devuelve el adapter que sabe cómo operar sobre el módulo indicado.
     * Centraliza la validación XOR: exactamente uno de los dos discriminadores
     * debe llegar no-null (BAD_REQUEST en caso contrario).
     */
    ArchivoModuleAdapter forModule(TipoCatalogoProducto entityType, Integer idCategoria);

    /** Atajo para el endpoint POST /files: parsea el DTO y delega en el adapter. */
    List<ArchivoResponseDTO> upload(FileUploadRequestDTO meta, MultipartFile[] files);

    ArchivoResponseDTO getOne(Integer idArchivo);

    void delete(Integer idArchivo);
}
