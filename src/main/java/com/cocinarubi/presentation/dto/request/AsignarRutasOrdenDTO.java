package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AsignarRutasOrdenDTO {

    @NotNull(message = "El id de la orden no puede ser nulo")
    @JsonProperty("idOrdenRuta")
    private Integer idOrdenRuta;

    @NotEmpty(message = "La lista de rutas no puede estar vacía")
    @JsonProperty("rutaIds")
    private List<Integer> rutaIds;

    public AsignarRutasOrdenDTO() {}

    public AsignarRutasOrdenDTO(Integer idOrdenRuta, List<Integer> rutaIds) {
        this.idOrdenRuta = idOrdenRuta;
        this.rutaIds = rutaIds;
    }

    public Integer getIdOrdenRuta() { return idOrdenRuta; }
    public void setIdOrdenRuta(Integer idOrdenRuta) { this.idOrdenRuta = idOrdenRuta; }

    public List<Integer> getRutaIds() { return rutaIds; }
    public void setRutaIds(List<Integer> rutaIds) { this.rutaIds = rutaIds; }
}
