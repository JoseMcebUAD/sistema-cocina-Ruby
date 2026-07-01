package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class BasicoPedidoResponseDTO {

    private int idBasicoPedido;
    private int idBasico;
    private String nombreBasico;
    private BigDecimal precioUnitario;

    public BasicoPedidoResponseDTO() {}

    public BasicoPedidoResponseDTO(int idBasicoPedido, int idBasico, String nombreBasico, BigDecimal precioUnitario) {
        this.idBasicoPedido = idBasicoPedido;
        this.idBasico = idBasico;
        this.nombreBasico = nombreBasico;
        this.precioUnitario = precioUnitario;
    }

    public int getIdBasicoPedido() { return idBasicoPedido; }
    public void setIdBasicoPedido(int idBasicoPedido) { this.idBasicoPedido = idBasicoPedido; }

    public int getIdBasico() { return idBasico; }
    public void setIdBasico(int idBasico) { this.idBasico = idBasico; }

    public String getNombreBasico() { return nombreBasico; }
    public void setNombreBasico(String nombreBasico) { this.nombreBasico = nombreBasico; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
