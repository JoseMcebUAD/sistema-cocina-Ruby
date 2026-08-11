package com.cocinarubi.domain.service.files;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.interfaces.ArchivoService;
import com.cocinarubi.domain.service.files.adapter.ArchivoModuleAdapter;
import com.cocinarubi.domain.service.files.adapter.ArchivoModuleAdapterFactory;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementación de {@link ArchivoService}. Delega toda la lógica dependiente
 * del discriminador a un {@link ArchivoModuleAdapter} obtenido del factory;
 * conserva únicamente las operaciones neutrales ({@code getOne}, {@code delete})
 * y el atajo de {@code upload} que consume el DTO del controller.
 * Capa: Service — fachada delgada sobre el subsistema de archivos.
 */
@Service
public class ArchivoServiceImpl implements ArchivoService {

    private final ArchivoModuleAdapterFactory adapterFactory;
    private final ArchivoRepository archivoRepository;
    private final Cloudinary cloudinary;

    public ArchivoServiceImpl(ArchivoModuleAdapterFactory adapterFactory,
                              ArchivoRepository archivoRepository,
                              Cloudinary cloudinary) {
        this.adapterFactory = adapterFactory;
        this.archivoRepository = archivoRepository;
        this.cloudinary = cloudinary;
    }

    /**
     * Punto único de validación XOR: exactamente uno de los dos discriminadores debe
     * llegar no-null. Dos nulls o dos valores simultáneos producen 400.
     * Delega la construcción del adapter concreto a {@link ArchivoModuleAdapterFactory}.
     */
    @Override
    public ArchivoModuleAdapter forModule(TipoCatalogoProducto entityType, Integer idCategoria) {
        boolean tieneTipo = entityType != null;
        boolean tieneCategoria = idCategoria != null;
        if (tieneTipo == tieneCategoria) {
            throw new BusinessException(
                    "Debe indicarse exactamente uno de: 'entityType' o 'idCategoria'",
                    HttpStatus.BAD_REQUEST);
        }
        return tieneTipo
                ? adapterFactory.resolve(entityType)
                : adapterFactory.resolve(idCategoria);
    }

    @Override
    public List<ArchivoResponseDTO> upload(FileUploadRequestDTO meta, MultipartFile[] files) {
        return forModule(meta.getEntityType(), meta.getIdCategoria())
                .upload(meta.getIdEntidad(), files);
    }

    @Override
    public ArchivoResponseDTO getOne(Integer idArchivo) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));
        return ArchivoResponseDTO.from(archivo);
    }

    /**
     * Elimina el asset de Cloudinary antes de borrar la fila en BD para evitar
     * huérfanos en el CDN. Si la llamada remota falla se lanza 500 y se aborta
     * sin tocar la base de datos.
     */
    @Override
    @Transactional
    public void delete(Integer idArchivo) {
        Archivo archivo = archivoRepository.findById(idArchivo)
                .orElseThrow(() -> new BusinessException(
                        "Archivo no encontrado con id: " + idArchivo, HttpStatus.NOT_FOUND));

        try {
            // Cloudinary: elimina el asset remoto; solo entonces liberamos la fila en BD
            cloudinary.uploader().destroy(archivo.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new BusinessException(
                    "Error al eliminar el archivo en Cloudinary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        archivoRepository.delete(archivo);
    }
}
