package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PaquetePedidoResponseDTO {

    private int idPaquetePedido;
    private int idPaquete;
    private String descripcion;
    private BigDecimal precioUnitario;
    private int cantidad;
    // Nombres de los productos incluidos en el paquete al momento de imprimir el ticket
    private List<String> nombresProductos;

    public PaquetePedidoResponseDTO() {}

    public PaquetePedidoResponseDTO(int idPaquetePedido, int idPaquete, String descripcion,
                                    BigDecimal precioUnitario, int cantidad,
                                    List<String> nombresProductos) {
        this.idPaquetePedido = idPaquetePedido;
        this.idPaquete = idPaquete;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.nombresProductos = nombresProductos;
    }

    public int getIdPaquetePedido() { return idPaquetePedido; }
    public void setIdPaquetePedido(int idPaquetePedido) { this.idPaquetePedido = idPaquetePedido; }

    public int getIdPaquete() { return idPaquete; }
    public void setIdPaquete(int idPaquete) { this.idPaquete = idPaquete; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public List<String> getNombresProductos() { return nombresProductos; }
    public void setNombresProductos(List<String> nombresProductos) { this.nombresProductos = nombresProductos; }
}
