package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PedidoCocinaDTO {

    @JsonProperty("nombreCliente")
    private String nombreCliente;

    public PedidoCocinaDTO() {}

    public PedidoCocinaDTO(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
