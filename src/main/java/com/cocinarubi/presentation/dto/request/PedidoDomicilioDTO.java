package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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

    public PedidoDomicilioDTO() {}

    public PedidoDomicilioDTO(Integer idRuta, String direccion, String codigo) {
        this.idRuta = idRuta;
        this.direccion = direccion;
        this.codigo = codigo;
    }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
