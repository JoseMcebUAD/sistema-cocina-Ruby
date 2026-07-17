package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PedidoDomicilioCocinaDTO {

    @NotNull(message = "El id del registro de cliente no puede ser nulo")
    @Positive(message = "El id del registro de cliente debe ser mayor a cero")
    @JsonProperty("idRegistroCliente")
    private Integer idRegistroCliente;

    @NotNull(message = "El id de la ruta no puede ser nulo")
    @Positive(message = "El id de la ruta debe ser mayor a cero")
    @JsonProperty("idRuta")
    private Integer idRuta;

    @NotBlank(message = "El domicilio de entrega no puede estar vacío")
    @JsonProperty("domicilio")
    private String domicilio;

    @NotNull(message = "El precio de tarifa no puede ser nulo")
    @JsonProperty("precioTarifa")
    private BigDecimal precioTarifa;

    public PedidoDomicilioCocinaDTO() {}

    public PedidoDomicilioCocinaDTO(Integer idRegistroCliente, Integer idRuta,
                                    String domicilio, BigDecimal precioTarifa) {
        this.idRegistroCliente = idRegistroCliente;
        this.idRuta = idRuta;
        this.domicilio = domicilio;
        this.precioTarifa = precioTarifa;
    }

    public Integer getIdRegistroCliente() { return idRegistroCliente; }
    public void setIdRegistroCliente(Integer idRegistroCliente) { this.idRegistroCliente = idRegistroCliente; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public BigDecimal getPrecioTarifa() { return precioTarifa; }
    public void setPrecioTarifa(BigDecimal precioTarifa) { this.precioTarifa = precioTarifa; }
}
