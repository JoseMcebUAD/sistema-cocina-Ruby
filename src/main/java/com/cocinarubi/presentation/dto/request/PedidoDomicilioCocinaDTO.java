package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PedidoDomicilioCocinaDTO {

    @NotNull(message = "El id del registro de cliente no puede ser nulo")
    @Positive(message = "El id del registro de cliente debe ser mayor a cero")
    @JsonProperty("idRegistroCliente")
    private Integer idRegistroCliente;

    @NotNull(message = "La tarifa no puede ser nula")
    @DecimalMin(value = "0.0", inclusive = true, message = "La tarifa no puede ser negativa")
    @JsonProperty("tarifa")
    private BigDecimal tarifa;

    @NotBlank(message = "El domicilio de entrega no puede estar vacío")
    @JsonProperty("domicilio")
    private String domicilio;

    @NotNull(message = "El id de ruta no puede ser nulo")
    @Positive(message = "El id de ruta debe ser mayor a cero")
    @JsonProperty("idRuta")
    private Integer idRuta;

    public PedidoDomicilioCocinaDTO() {}

    public PedidoDomicilioCocinaDTO(Integer idRegistroCliente, BigDecimal tarifa, String domicilio, Integer idRuta) {
        this.idRegistroCliente = idRegistroCliente;
        this.tarifa = tarifa;
        this.domicilio = domicilio;
        this.idRuta = idRuta;
    }

    public Integer getIdRegistroCliente() { return idRegistroCliente; }
    public void setIdRegistroCliente(Integer idRegistroCliente) { this.idRegistroCliente = idRegistroCliente; }

    public BigDecimal getTarifa() { return tarifa; }
    public void setTarifa(BigDecimal tarifa) { this.tarifa = tarifa; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }
}
