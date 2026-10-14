package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OfertaLineaRequestDTO {

    @NotNull(message = "El modo de match no puede ser nulo")
    @JsonProperty("modoMatch")
    private ModoMatchOferta modoMatch;

    /** Null para modos _GENERAL y COMIDA_POR_TIPO. */
    @JsonProperty("idReferencia")
    private Integer idReferencia;

    /** Solo requerido cuando modoMatch = COMIDA_POR_TIPO. */
    @JsonProperty("tipoComidaFiltro")
    private TipoComida tipoComidaFiltro;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @JsonProperty("cantidad")
    private Integer cantidad = 1;

    public OfertaLineaRequestDTO() {}

    public ModoMatchOferta getModoMatch() { return modoMatch; }
    public void setModoMatch(ModoMatchOferta modoMatch) { this.modoMatch = modoMatch; }

    public Integer getIdReferencia() { return idReferencia; }
    public void setIdReferencia(Integer idReferencia) { this.idReferencia = idReferencia; }

    public TipoComida getTipoComidaFiltro() { return tipoComidaFiltro; }
    public void setTipoComidaFiltro(TipoComida tipoComidaFiltro) { this.tipoComidaFiltro = tipoComidaFiltro; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
