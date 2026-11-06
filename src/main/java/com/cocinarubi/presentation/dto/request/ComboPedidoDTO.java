package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ComboPedidoDTO {

    @NotNull(message = "El id del combo no puede ser nulo")
    @Positive(message = "El id del combo debe ser mayor a cero")
    @JsonProperty("idCombo")
    private Integer idCombo;

    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public ComboPedidoDTO() {}

    public ComboPedidoDTO(Integer idCombo, BigDecimal precioUnitario, Integer cantidad) {
        this.idCombo = idCombo;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    @JsonProperty("descuento")
    private boolean descuento;

    @Positive(message = "El precio de descuento debe ser mayor a cero")
    @JsonProperty("precioDescuento")
    private BigDecimal precioDescuento;

    public Integer getIdCombo() { return idCombo; }
    public void setIdCombo(Integer idCombo) { this.idCombo = idCombo; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public boolean isDescuento() { return descuento; }
    public void setDescuento(boolean descuento) { this.descuento = descuento; }

    public BigDecimal getPrecioDescuento() { return precioDescuento; }
    public void setPrecioDescuento(BigDecimal precioDescuento) { this.precioDescuento = precioDescuento; }
}
