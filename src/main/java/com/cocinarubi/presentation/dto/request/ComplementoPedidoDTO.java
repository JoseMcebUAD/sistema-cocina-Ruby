package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ComplementoPedidoDTO {

    @NotNull(message = "El id del complemento no puede ser nulo")
    @Positive(message = "El id del complemento debe ser mayor a cero")
    @JsonProperty("idComplemento")
    private Integer idComplemento;

    @JsonProperty("precio_unitario")
    private BigDecimal precioUnitario;

    public ComplementoPedidoDTO() {}

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
