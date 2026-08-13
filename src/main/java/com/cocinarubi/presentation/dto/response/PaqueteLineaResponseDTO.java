package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoLineaPaquete;

public class PaqueteLineaResponseDTO {

    private int idPaqueteProducto;
    private TipoLineaPaquete tipoProducto;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;

    public PaqueteLineaResponseDTO() {}

    public PaqueteLineaResponseDTO(int idPaqueteProducto, TipoLineaPaquete tipoProducto,
                                   int idProducto, String nombreProducto, int cantidad) {
        this.idPaqueteProducto = idPaqueteProducto;
        this.tipoProducto = tipoProducto;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
    }

    public int getIdPaqueteProducto() { return idPaqueteProducto; }
    public void setIdPaqueteProducto(int idPaqueteProducto) { this.idPaqueteProducto = idPaqueteProducto; }

    public TipoLineaPaquete getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoLineaPaquete tipoProducto) { this.tipoProducto = tipoProducto; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
