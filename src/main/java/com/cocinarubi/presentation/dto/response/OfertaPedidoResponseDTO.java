package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class OfertaPedidoResponseDTO {

    private int idOfertaPedido;
    private Integer idOferta;
    private String nombreOferta;
    private BigDecimal precioAplicado;
    private BigDecimal ahorro;
    private List<OfertaPedidoLineaResponseDTO> productos;

    public OfertaPedidoResponseDTO() {}

    public int getIdOfertaPedido() { return idOfertaPedido; }
    public void setIdOfertaPedido(int idOfertaPedido) { this.idOfertaPedido = idOfertaPedido; }

    public Integer getIdOferta() { return idOferta; }
    public void setIdOferta(Integer idOferta) { this.idOferta = idOferta; }

    public String getNombreOferta() { return nombreOferta; }
    public void setNombreOferta(String nombreOferta) { this.nombreOferta = nombreOferta; }

    public BigDecimal getPrecioAplicado() { return precioAplicado; }
    public void setPrecioAplicado(BigDecimal precioAplicado) { this.precioAplicado = precioAplicado; }

    public BigDecimal getAhorro() { return ahorro; }
    public void setAhorro(BigDecimal ahorro) { this.ahorro = ahorro; }

    public List<OfertaPedidoLineaResponseDTO> getProductos() { return productos; }
    public void setProductos(List<OfertaPedidoLineaResponseDTO> productos) { this.productos = productos; }
}
