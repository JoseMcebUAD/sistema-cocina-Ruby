package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ProductoCocinaPedidoDTO {

    @NotNull(message = "El id del producto de cocina no puede ser nulo")
    @Positive(message = "El id del producto de cocina debe ser mayor a cero")
    @JsonProperty("idProductoCocina")
    private Integer idProductoCocina;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public ProductoCocinaPedidoDTO() {}

    public ProductoCocinaPedidoDTO(Integer idProductoCocina, BigDecimal precioUnitario, Integer cantidad) {
        this.idProductoCocina = idProductoCocina;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public Integer getIdProductoCocina() { return idProductoCocina; }
    public void setIdProductoCocina(Integer idProductoCocina) { this.idProductoCocina = idProductoCocina; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
