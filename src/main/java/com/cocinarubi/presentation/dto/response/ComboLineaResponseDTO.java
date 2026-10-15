package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoLineaCombo;

public class ComboLineaResponseDTO {

    private int idComboProducto;
    private TipoLineaCombo tipoProducto;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;

    public ComboLineaResponseDTO() {}

    public ComboLineaResponseDTO(int idComboProducto, TipoLineaCombo tipoProducto,
                                 int idProducto, String nombreProducto, int cantidad) {
        this.idComboProducto = idComboProducto;
        this.tipoProducto = tipoProducto;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
    }

    public int getIdComboProducto() { return idComboProducto; }
    public void setIdComboProducto(int idComboProducto) { this.idComboProducto = idComboProducto; }

    public TipoLineaCombo getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoLineaCombo tipoProducto) { this.tipoProducto = tipoProducto; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
