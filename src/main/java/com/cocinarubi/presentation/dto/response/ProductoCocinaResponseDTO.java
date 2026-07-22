package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoProducto;

import java.math.BigDecimal;

public class ProductoCocinaResponseDTO {

    private int idProductoCocina;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precioDomicilio;
    private BigDecimal precioNormal;
    private Estatus estatus;
    private boolean destacado;
    private TipoProducto tipoProducto;

    public ProductoCocinaResponseDTO() {}

    public ProductoCocinaResponseDTO(int idProductoCocina, String nombreProducto, String descripcion,
                                     BigDecimal precioDomicilio, BigDecimal precioNormal,
                                     Estatus estatus, boolean destacado, TipoProducto tipoProducto) {
        this.idProductoCocina = idProductoCocina;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioDomicilio = precioDomicilio;
        this.precioNormal = precioNormal;
        this.estatus = estatus;
        this.destacado = destacado;
        this.tipoProducto = tipoProducto;
    }

    public int getIdProductoCocina() { return idProductoCocina; }
    public void setIdProductoCocina(int idProductoCocina) { this.idProductoCocina = idProductoCocina; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioDomicilio() { return precioDomicilio; }
    public void setPrecioDomicilio(BigDecimal precioDomicilio) { this.precioDomicilio = precioDomicilio; }

    public BigDecimal getPrecioNormal() { return precioNormal; }
    public void setPrecioNormal(BigDecimal precioNormal) { this.precioNormal = precioNormal; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public TipoProducto getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoProducto tipoProducto) { this.tipoProducto = tipoProducto; }
}
