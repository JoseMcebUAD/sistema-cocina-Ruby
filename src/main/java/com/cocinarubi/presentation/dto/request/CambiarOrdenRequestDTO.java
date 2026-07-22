package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CambiarOrdenRequestDTO {

    @NotNull(message = "El tipo de entidad no puede ser nulo")
    private TipoCatalogoProducto entityType;

    @NotNull(message = "El id del archivo no puede ser nulo")
    @Positive(message = "El id del archivo debe ser mayor a cero")
    private Integer idArchivo;

    @NotNull(message = "El nuevo orden no puede ser nulo")
    @Positive(message = "El nuevo orden debe ser mayor a cero")
    private Integer nuevoOrden;

    public CambiarOrdenRequestDTO() {}

    public TipoCatalogoProducto getEntityType() { return entityType; }
    public void setEntityType(TipoCatalogoProducto entityType) { this.entityType = entityType; }

    public Integer getIdArchivo() { return idArchivo; }
    public void setIdArchivo(Integer idArchivo) { this.idArchivo = idArchivo; }

    public Integer getNuevoOrden() { return nuevoOrden; }
    public void setNuevoOrden(Integer nuevoOrden) { this.nuevoOrden = nuevoOrden; }
}
