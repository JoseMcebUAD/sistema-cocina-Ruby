package com.cocinarubi.domain.service.files;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Helper de bajo nivel que sube un archivo a Cloudinary y persiste la fila
 * correspondiente en la tabla {@code archivo}. Es agnóstico al discriminador —
 * los dos adapters (estático y por categoría) le pasan {@code entityType} o
 * {@code categoria} según corresponda; el otro va {@code null}.
 * Capa: Infrastructure — pegamento entre Cloudinary y JPA.
 */
@Component
public class ArchivoUploader {

    private final Cloudinary cloudinary;
    private final ArchivoRepository archivoRepository;

    public ArchivoUploader(Cloudinary cloudinary, ArchivoRepository archivoRepository) {
        this.cloudinary = cloudinary;
        this.archivoRepository = archivoRepository;
    }

    /**
     * Valida el MIME type contra los permitidos por el módulo, sube el archivo a
     * Cloudinary dentro de la carpeta {@code modulo.getRuta()} y persiste la fila
     * en la tabla {@code archivo}.
     * <p>
     * Contrato XOR del discriminador: el caller debe pasar exactamente uno de
     * {@code entityType} (módulo estático) o {@code categoria} (módulo dinámico);
     * el otro debe ser {@code null}. Esta clase no valida el XOR — es responsabilidad
     * de los adapters que la consumen.
     * </p>
     */
    public Archivo subirYPersistir(MultipartFile file, ArchivoModulo modulo,
                                   List<String> mimesPermitidos, int orden,
                                   TipoCatalogoProducto entityType, Categoria categoria,
                                   Integer idEntidad) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Archivo vacío en la petición", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !mimesPermitidos.contains(contentType)) {
            throw new BusinessException(
                    "Tipo de archivo no permitido: " + contentType + ". Permitidos: " + mimesPermitidos,
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        Map<?, ?> resultUpload;
        try {
            // Cloudinary: crea la carpeta destino al vuelo si no existe
            resultUpload = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", modulo.getRuta()));
        } catch (Exception e) {
            throw new BusinessException(
                    "Error al subir el archivo a Cloudinary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String secureUrl = String.valueOf(resultUpload.get("secure_url"));
        String publicId = String.valueOf(resultUpload.get("public_id"));

        Archivo archivo = Archivo.builder()
                .archivoModulo(modulo)
                .pathArchivo(secureUrl)
                .mimeType(contentType)
                .nombreArchivo(file.getOriginalFilename() != null
                        ? file.getOriginalFilename() : publicId)
                .orden(orden)
                .entityType(entityType)
                .categoria(categoria)
                .idEntidad(idEntidad)
                .publicId(publicId)
                .creadoEn(LocalDateTime.now(Constants.ZONA_MERIDA))
                .build();

        return archivoRepository.save(archivo);
    }
}
