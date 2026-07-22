package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RutaOrdenItemDTO {

    @NotNull(message = "El id de la ruta no puede ser nulo")
    @JsonProperty("idRuta")
    private Integer idRuta;

    @NotNull(message = "El orden no puede ser nulo")
    @Min(value = 1, message = "El orden debe ser mayor a cero")
    @JsonProperty("orden")
    private Integer orden;

    public RutaOrdenItemDTO() {}

    public RutaOrdenItemDTO(Integer idRuta, Integer orden) {
        this.idRuta = idRuta;
        this.orden = orden;
    }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
