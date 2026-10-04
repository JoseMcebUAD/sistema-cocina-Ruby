package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ComboPedidoResponseDTO {

    private int idComboPedido;
    private int idCombo;
    private String descripcion;
    private BigDecimal precioUnitario;
    private int cantidad;
    // Nombres de los productos incluidos en el combo al momento de imprimir el ticket
    private List<String> nombresProductos;

    public ComboPedidoResponseDTO() {}

    public ComboPedidoResponseDTO(int idComboPedido, int idCombo, String descripcion,
                                   BigDecimal precioUnitario, int cantidad,
                                   List<String> nombresProductos) {
        this.idComboPedido = idComboPedido;
        this.idCombo = idCombo;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.nombresProductos = nombresProductos;
    }

    public int getIdComboPedido() { return idComboPedido; }
    public void setIdComboPedido(int idComboPedido) { this.idComboPedido = idComboPedido; }

    public int getIdCombo() { return idCombo; }
    public void setIdCombo(int idCombo) { this.idCombo = idCombo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public List<String> getNombresProductos() { return nombresProductos; }
    public void setNombresProductos(List<String> nombresProductos) { this.nombresProductos = nombresProductos; }
}
