package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;

import java.math.BigDecimal;
import java.util.List;


public class ComboResponseDTO {

    private int idCombo;
    private BigDecimal precio;
    private String descripcion;
    private Estatus estatus;
    private boolean destacado;
    private List<ComboLineaResponseDTO> productos;
    private boolean descuentoActivo;
    private String descripcionDescuento;
    private BigDecimal precioDescuento;

    public ComboResponseDTO() {}

    public ComboResponseDTO(int idCombo, BigDecimal precio, String descripcion,
                            Estatus estatus, boolean destacado, List<ComboLineaResponseDTO> productos) {
        this.idCombo = idCombo;
        this.precio = precio;
        this.descripcion = descripcion;
        this.estatus = estatus;
        this.destacado = destacado;
        this.productos = productos;
    }

    public int getIdCombo() { return idCombo; }
    public void setIdCombo(int idCombo) { this.idCombo = idCombo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean getDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public List<ComboLineaResponseDTO> getProductos() { return productos; }
    public void setProductos(List<ComboLineaResponseDTO> productos) { this.productos = productos; }

    public boolean isDescuentoActivo() { return descuentoActivo; }
    public void setDescuentoActivo(boolean descuentoActivo) { this.descuentoActivo = descuentoActivo; }

    public String getDescripcionDescuento() { return descripcionDescuento; }
    public void setDescripcionDescuento(String descripcionDescuento) { this.descripcionDescuento = descripcionDescuento; }

    public BigDecimal getPrecioDescuento() { return precioDescuento; }
    public void setPrecioDescuento(BigDecimal precioDescuento) { this.precioDescuento = precioDescuento; }
}
