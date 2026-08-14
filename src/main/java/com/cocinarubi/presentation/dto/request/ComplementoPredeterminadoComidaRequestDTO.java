package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Datos necesarios para asignar o actualizar un complemento predeterminado en una comida.
 * Capa: DTO de entrada — presentation/dto/request.
 */
public class ComplementoPredeterminadoComidaRequestDTO {

    @NotNull(message = "El id del complemento no puede ser nulo")
    @Positive(message = "El id del complemento debe ser mayor a cero")
    @JsonProperty("idComplemento")
    private Integer idComplemento;

    @NotNull(message = "La cantidad no puede ser nula")
    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public ComplementoPredeterminadoComidaRequestDTO() {}

    public ComplementoPredeterminadoComidaRequestDTO(Integer idComplemento, Integer cantidad) {
        this.idComplemento = idComplemento;
        this.cantidad = cantidad;
    }

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
