package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Discriminador dual: exactamente uno de {@code entityType} o {@code idCategoria}
 * debe estar presente. Validado en el controlador (400 si vienen ambos o ninguno).
 */
public class CambiarOrdenRequestDTO {

    private TipoCatalogoProducto entityType;

    private Integer idCategoria;

    @NotNull(message = "El id del archivo no puede ser nulo")
    @Positive(message = "El id del archivo debe ser mayor a cero")
    private Integer idArchivo;

    @NotNull(message = "El nuevo orden no puede ser nulo")
    @Positive(message = "El nuevo orden debe ser mayor a cero")
    private Integer nuevoOrden;

    public CambiarOrdenRequestDTO() {}

    public TipoCatalogoProducto getEntityType() { return entityType; }
    public void setEntityType(TipoCatalogoProducto entityType) { this.entityType = entityType; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public Integer getIdArchivo() { return idArchivo; }
    public void setIdArchivo(Integer idArchivo) { this.idArchivo = idArchivo; }

    public Integer getNuevoOrden() { return nuevoOrden; }
    public void setNuevoOrden(Integer nuevoOrden) { this.nuevoOrden = nuevoOrden; }
}
