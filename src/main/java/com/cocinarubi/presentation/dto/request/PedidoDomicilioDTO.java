package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PedidoDomicilioDTO {

    @NotNull(message = "El id de la ruta no puede ser nulo cuando el pedido es a domicilio")
    @Positive(message = "El id de la ruta debe ser mayor a cero")
    @JsonProperty("idRuta")
    private Integer idRuta;

    @NotBlank(message = "La dirección de entrega no puede estar vacía")
    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("latitud")
    private BigDecimal latitud;

    @JsonProperty("longitud")
    private BigDecimal longitud;

    @NotNull(message = "La tarifa no puede ser nula")
    @Positive(message = "La tarifa debe ser mayor a cero")
    @JsonProperty("tarifa")
    private BigDecimal tarifa;

    @JsonProperty("tarifasEspeciales")
    private BigDecimal tarifasEspeciales;

    public PedidoDomicilioDTO() {}

    public PedidoDomicilioDTO(Integer idRuta, String direccion, String codigo,
                               BigDecimal latitud, BigDecimal longitud,
                               BigDecimal tarifa, BigDecimal tarifasEspeciales) {
        this.idRuta = idRuta;
        this.direccion = direccion;
        this.codigo = codigo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tarifa = tarifa;
        this.tarifasEspeciales = tarifasEspeciales;
    }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }

    public BigDecimal getTarifa() { return tarifa; }
    public void setTarifa(BigDecimal tarifa) { this.tarifa = tarifa; }

    public BigDecimal getTarifasEspeciales() { return tarifasEspeciales; }
    public void setTarifasEspeciales(BigDecimal tarifasEspeciales) { this.tarifasEspeciales = tarifasEspeciales; }
}
