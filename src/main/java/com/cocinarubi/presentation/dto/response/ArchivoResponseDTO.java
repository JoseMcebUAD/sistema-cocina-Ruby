package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.Archivo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO de salida que representa un archivo almacenado en Cloudinary. Incluye
 * la URL pública, el public_id para eliminación futura y los metadatos de la entidad dueña.
 */
public class ArchivoResponseDTO {

    private Integer idArchivo;
    private TipoCatalogoProducto entityType;
    private Integer idEntidad;
    private String pathArchivo;
    private String publicId;
    private String mimeType;
    private String nombreArchivo;
    private Integer orden;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creadoEn;

    public ArchivoResponseDTO() {}

    // Mapeo manual desde la entidad para desacoplar la capa de presentación del dominio
    public static ArchivoResponseDTO from(Archivo a) {
        ArchivoResponseDTO dto = new ArchivoResponseDTO();
        dto.idArchivo = a.getIdArchivo();
        dto.entityType = a.getEntityType();
        dto.idEntidad = a.getIdEntidad();
        dto.pathArchivo = a.getPathArchivo();
        dto.publicId = a.getPublicId();
        dto.mimeType = a.getMimeType();
        dto.nombreArchivo = a.getNombreArchivo();
        dto.orden = a.getOrden();
        dto.creadoEn = a.getCreadoEn();
        return dto;
    }

    public Integer getIdArchivo() { return idArchivo; }
    public void setIdArchivo(Integer idArchivo) { this.idArchivo = idArchivo; }

    public TipoCatalogoProducto getEntityType() { return entityType; }
    public void setEntityType(TipoCatalogoProducto entityType) { this.entityType = entityType; }

    public Integer getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Integer idEntidad) { this.idEntidad = idEntidad; }

    public String getPathArchivo() { return pathArchivo; }
    public void setPathArchivo(String pathArchivo) { this.pathArchivo = pathArchivo; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
