package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoContadorComida;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InventarioComidaRequestDTO {

    @NotNull(message = "El id de la comida no puede ser nulo")
    @Positive(message = "El id de la comida debe ser mayor a cero")
    @JsonProperty("idComida")
    private Integer idComida;

    @NotNull(message = "La cantidad no puede ser nula (CHECK constraint en BD)")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @JsonProperty("cantidad")
    private Integer cantidad;

    @NotNull(message = "El tipo de contador no puede ser nulo")
    @JsonProperty("tipoContadorComida")
    private TipoContadorComida tipoContadorComida;

    public InventarioComidaRequestDTO() {}

    public InventarioComidaRequestDTO(Integer idComida, Integer cantidad,
                                      TipoContadorComida tipoContadorComida) {
        this.idComida = idComida;
        this.cantidad = cantidad;
        this.tipoContadorComida = tipoContadorComida;
    }

    public Integer getIdComida() { return idComida; }
    public void setIdComida(Integer idComida) { this.idComida = idComida; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public TipoContadorComida getTipoContadorComida() { return tipoContadorComida; }
    public void setTipoContadorComida(TipoContadorComida tipoContadorComida) {
        this.tipoContadorComida = tipoContadorComida;
    }
}
