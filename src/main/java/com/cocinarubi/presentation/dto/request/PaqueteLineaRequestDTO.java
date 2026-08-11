package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaqueteLineaRequestDTO {

    @NotNull(message = "El tipo de producto no puede ser nulo")
    @JsonProperty("tipoProducto")
    private TipoLineaPaquete tipoProducto;

    @NotNull(message = "El id del producto no puede ser nulo")
    @Positive(message = "El id del producto debe ser mayor a cero")
    @JsonProperty("idProducto")
    private Integer idProducto;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public PaqueteLineaRequestDTO() {}

    public PaqueteLineaRequestDTO(TipoLineaPaquete tipoProducto, Integer idProducto, Integer cantidad) {
        this.tipoProducto = tipoProducto;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public TipoLineaPaquete getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoLineaPaquete tipoProducto) { this.tipoProducto = tipoProducto; }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
