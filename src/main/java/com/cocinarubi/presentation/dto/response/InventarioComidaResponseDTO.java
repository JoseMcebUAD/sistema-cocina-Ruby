package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoContadorComida;

public class InventarioComidaResponseDTO {

    private int idInventarioComida;
    private int idComida;
    private String nombreComida;
    private Integer cantidad;
    private TipoContadorComida tipoContadorComida;

    public InventarioComidaResponseDTO() {}

    public InventarioComidaResponseDTO(int idInventarioComida, int idComida, String nombreComida,
                                       Integer cantidad, TipoContadorComida tipoContadorComida) {
        this.idInventarioComida = idInventarioComida;
        this.idComida = idComida;
        this.nombreComida = nombreComida;
        this.cantidad = cantidad;
        this.tipoContadorComida = tipoContadorComida;
    }

    public int getIdInventarioComida() { return idInventarioComida; }
    public void setIdInventarioComida(int idInventarioComida) { this.idInventarioComida = idInventarioComida; }

    public int getIdComida() { return idComida; }
    public void setIdComida(int idComida) { this.idComida = idComida; }

    public String getNombreComida() { return nombreComida; }
    public void setNombreComida(String nombreComida) { this.nombreComida = nombreComida; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public TipoContadorComida getTipoContadorComida() { return tipoContadorComida; }
    public void setTipoContadorComida(TipoContadorComida tipoContadorComida) {
        this.tipoContadorComida = tipoContadorComida;
    }
}
