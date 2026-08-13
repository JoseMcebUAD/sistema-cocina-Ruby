package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaquetePedidoDTO {

    @NotNull(message = "El id del paquete no puede ser nulo")
    @Positive(message = "El id del paquete debe ser mayor a cero")
    @JsonProperty("idPaquete")
    private Integer idPaquete;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public PaquetePedidoDTO() {}

    public PaquetePedidoDTO(Integer idPaquete, BigDecimal precioUnitario, Integer cantidad) {
        this.idPaquete = idPaquete;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public Integer getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Integer idPaquete) { this.idPaquete = idPaquete; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
