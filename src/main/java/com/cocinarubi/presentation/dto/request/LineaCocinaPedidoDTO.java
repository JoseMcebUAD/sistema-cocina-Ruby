package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Referencia a un producto del pedido que el operador de cocina asigna explícitamente
 * a una aplicación de {@link OfertaCocinaPedidoDTO}.
 */
public class LineaCocinaPedidoDTO {

    @NotNull(message = "El tipo de entidad no puede ser nulo")
    @JsonProperty("tipoEntidad")
    private TipoEntidadOferta tipoEntidad;

    @NotNull(message = "El id de producto no puede ser nulo")
    @Positive(message = "El id de producto debe ser positivo")
    @JsonProperty("idProducto")
    private Integer idProducto;

    public LineaCocinaPedidoDTO() {}

    public TipoEntidadOferta getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(TipoEntidadOferta tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
}
