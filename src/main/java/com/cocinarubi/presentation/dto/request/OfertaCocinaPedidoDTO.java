package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

/**
 * Permite al operador de cocina indicar explícitamente qué oferta se aplica
 * y qué productos del pedido entran en ella.
 *
 * <p>Para el canal WEB esta información se omite: el backend auto-detecta
 * las ofertas aplicables. Para COCINA se envía en {@code PedidoRequestDTO.ofertas}.</p>
 */
public class OfertaCocinaPedidoDTO {

    @NotNull(message = "El id de oferta no puede ser nulo")
    @Positive(message = "El id de oferta debe ser positivo")
    @JsonProperty("idOferta")
    private Integer idOferta;

    @Valid
    @NotEmpty(message = "La oferta debe especificar al menos una línea de producto")
    @JsonProperty("lineas")
    private List<LineaCocinaPedidoDTO> lineas = new ArrayList<>();

    public OfertaCocinaPedidoDTO() {}

    public Integer getIdOferta() { return idOferta; }
    public void setIdOferta(Integer idOferta) { this.idOferta = idOferta; }

    public List<LineaCocinaPedidoDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaCocinaPedidoDTO> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }
}
