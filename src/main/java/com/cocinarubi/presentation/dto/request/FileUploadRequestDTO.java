package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para el endpoint de subida de archivos. Llega como parte JSON
 * en el @RequestPart "meta" de la petición multipart/form-data.
 *
 * <p>Discriminador dual: exactamente uno de {@code entityType} o {@code idCategoria}
 * debe estar presente. {@code entityType} identifica módulos estáticos
 * (BASICO / COMIDA / DESAYUNO); {@code idCategoria} identifica módulos dinámicos
 * generados al crear una {@code Categoria}. Validado en el service (400 si vienen
 * ambos o ninguno).</p>
 */
public class FileUploadRequestDTO {

    @JsonProperty("entityType")
    private TipoCatalogoProducto entityType;

    @JsonProperty("idCategoria")
    private Integer idCategoria;

    @NotNull(message = "El id de la entidad no puede ser nulo")
    @Positive(message = "El id de la entidad debe ser mayor a cero")
    @JsonProperty("idEntidad")
    private Integer idEntidad;

    public FileUploadRequestDTO() {}

    public FileUploadRequestDTO(TipoCatalogoProducto entityType, Integer idCategoria, Integer idEntidad) {
        this.entityType = entityType;
        this.idCategoria = idCategoria;
        this.idEntidad = idEntidad;
    }

    public TipoCatalogoProducto getEntityType() { return entityType; }
    public void setEntityType(TipoCatalogoProducto entityType) { this.entityType = entityType; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public Integer getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Integer idEntidad) { this.idEntidad = idEntidad; }
}
