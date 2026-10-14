package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;

public class OfertaLineaResponseDTO {

    private int idOfertaLinea;
    private ModoMatchOferta modoMatch;
    private Integer idReferencia;
    private TipoComida tipoComidaFiltro;
    private int cantidad;

    public OfertaLineaResponseDTO() {}

    public OfertaLineaResponseDTO(int idOfertaLinea, ModoMatchOferta modoMatch, Integer idReferencia,
                                   TipoComida tipoComidaFiltro, int cantidad) {
        this.idOfertaLinea = idOfertaLinea;
        this.modoMatch = modoMatch;
        this.idReferencia = idReferencia;
        this.tipoComidaFiltro = tipoComidaFiltro;
        this.cantidad = cantidad;
    }

    public int getIdOfertaLinea() { return idOfertaLinea; }
    public void setIdOfertaLinea(int idOfertaLinea) { this.idOfertaLinea = idOfertaLinea; }

    public ModoMatchOferta getModoMatch() { return modoMatch; }
    public void setModoMatch(ModoMatchOferta modoMatch) { this.modoMatch = modoMatch; }

    public Integer getIdReferencia() { return idReferencia; }
    public void setIdReferencia(Integer idReferencia) { this.idReferencia = idReferencia; }

    public TipoComida getTipoComidaFiltro() { return tipoComidaFiltro; }
    public void setTipoComidaFiltro(TipoComida tipoComidaFiltro) { this.tipoComidaFiltro = tipoComidaFiltro; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
