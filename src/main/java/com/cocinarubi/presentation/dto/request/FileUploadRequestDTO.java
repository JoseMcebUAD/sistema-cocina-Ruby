package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para el endpoint de subida de archivos. Llega como parte JSON
 * en el @RequestPart "meta" de la petición multipart/form-data.
 */
public class FileUploadRequestDTO {

    @NotNull(message = "El tipo de entidad no puede ser nulo")
    @JsonProperty("entityType")
    private TipoCatalogoProducto entityType;

    @NotNull(message = "El id de la entidad no puede ser nulo")
    @Positive(message = "El id de la entidad debe ser mayor a cero")
    @JsonProperty("idEntidad")
    private Integer idEntidad;

    public FileUploadRequestDTO() {}

    public FileUploadRequestDTO(TipoCatalogoProducto entityType, Integer idEntidad) {
        this.entityType = entityType;
        this.idEntidad = idEntidad;
    }

    public TipoCatalogoProducto getEntityType() { return entityType; }
    public void setEntityType(TipoCatalogoProducto entityType) { this.entityType = entityType; }

    public Integer getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Integer idEntidad) { this.idEntidad = idEntidad; }
}
