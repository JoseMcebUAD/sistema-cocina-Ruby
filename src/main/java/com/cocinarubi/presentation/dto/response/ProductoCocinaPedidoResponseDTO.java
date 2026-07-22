package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class ProductoCocinaPedidoResponseDTO {

    private int idProductoCocinaPedido;
    private int idProductoCocina;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private int cantidad;

    public ProductoCocinaPedidoResponseDTO() {}

    public ProductoCocinaPedidoResponseDTO(int idProductoCocinaPedido, int idProductoCocina,
                                           String nombreProducto, BigDecimal precioUnitario, int cantidad) {
        this.idProductoCocinaPedido = idProductoCocinaPedido;
        this.idProductoCocina = idProductoCocina;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public int getIdProductoCocinaPedido() { return idProductoCocinaPedido; }
    public void setIdProductoCocinaPedido(int idProductoCocinaPedido) { this.idProductoCocinaPedido = idProductoCocinaPedido; }

    public int getIdProductoCocina() { return idProductoCocina; }
    public void setIdProductoCocina(int idProductoCocina) { this.idProductoCocina = idProductoCocina; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
