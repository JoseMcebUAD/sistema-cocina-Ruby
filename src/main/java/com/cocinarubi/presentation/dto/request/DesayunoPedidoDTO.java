package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DesayunoPedidoDTO {

    @NotNull(message = "El id del desayuno no puede ser nulo")
    @Positive(message = "El id del desayuno debe ser mayor a cero")
    @JsonProperty("idDesayuno")
    private Integer idDesayuno;

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser mayor a cero")
    @JsonProperty("precio")
    private BigDecimal precio;

    public DesayunoPedidoDTO() {}

    public DesayunoPedidoDTO(Integer idDesayuno, BigDecimal precio) {
        this.idDesayuno = idDesayuno;
        this.precio = precio;
    }

    public Integer getIdDesayuno() { return idDesayuno; }
    public void setIdDesayuno(Integer idDesayuno) { this.idDesayuno = idDesayuno; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
