package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BasicoPedidoDTO {

    @NotNull(message = "El id del básico no puede ser nulo")
    @Positive(message = "El id del básico debe ser mayor a cero")
    @JsonProperty("idBasico")
    private Integer idBasico;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    public BasicoPedidoDTO() {}

    public BasicoPedidoDTO(Integer idBasico, BigDecimal precioUnitario) {
        this.idBasico = idBasico;
        this.precioUnitario = precioUnitario;
    }

    public Integer getIdBasico() { return idBasico; }
    public void setIdBasico(Integer idBasico) { this.idBasico = idBasico; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
