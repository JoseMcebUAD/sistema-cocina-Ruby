package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para un complemento extra de un paquete básico en el pedido.
 *
 * <p>El precio no se envía desde el cliente: siempre se toma del catálogo
 * ({@code complemento.precioExtra}) en el momento de la orden.</p>
 */
public class BasicoPedidoExtraDTO {

    @NotNull(message = "El id del complemento no puede ser nulo")
    @Positive(message = "El id del complemento debe ser mayor a cero")
    @JsonProperty("idComplemento")
    private Integer idComplemento;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Max(value = 127, message = "La cantidad no puede superar 127")
    @JsonProperty("cantidad")
    private Integer cantidad;

    public BasicoPedidoExtraDTO() {}

    public BasicoPedidoExtraDTO(Integer idComplemento, Integer cantidad) {
        this.idComplemento = idComplemento;
        this.cantidad = cantidad;
    }

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
