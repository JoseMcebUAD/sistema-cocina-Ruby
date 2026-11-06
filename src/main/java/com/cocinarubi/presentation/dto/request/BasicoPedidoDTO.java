package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BasicoPedidoDTO {

    @NotNull(message = "El id del básico no puede ser nulo")
    @Positive(message = "El id del básico debe ser mayor a cero")
    @JsonProperty("idBasico")
    private Integer idBasico;

    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    @Valid
    @JsonProperty("extras")
    private List<BasicoPedidoExtraDTO> extras = new ArrayList<>();

    @JsonProperty("descuento")
    private boolean descuento;

    @Positive(message = "El precio de descuento debe ser mayor a cero")
    @JsonProperty("precioDescuento")
    private BigDecimal precioDescuento;

    public BasicoPedidoDTO() {}

    public BasicoPedidoDTO(Integer idBasico, BigDecimal precioUnitario) {
        this.idBasico = idBasico;
        this.precioUnitario = precioUnitario;
    }

    public Integer getIdBasico() { return idBasico; }
    public void setIdBasico(Integer idBasico) { this.idBasico = idBasico; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public List<BasicoPedidoExtraDTO> getExtras() { return extras; }
    public void setExtras(List<BasicoPedidoExtraDTO> extras) { this.extras = extras != null ? extras : new ArrayList<>(); }

    public boolean isDescuento() { return descuento; }
    public void setDescuento(boolean descuento) { this.descuento = descuento; }

    public BigDecimal getPrecioDescuento() { return precioDescuento; }
    public void setPrecioDescuento(BigDecimal precioDescuento) { this.precioDescuento = precioDescuento; }
}
