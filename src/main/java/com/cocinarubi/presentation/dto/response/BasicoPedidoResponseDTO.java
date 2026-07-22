package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class BasicoPedidoResponseDTO {

    private int idBasicoPedido;
    private BasicoResponseDTO basico;
    private BigDecimal precioUnitario;

    public BasicoPedidoResponseDTO() {}

    public BasicoPedidoResponseDTO(int idBasicoPedido, BasicoResponseDTO basico, BigDecimal precioUnitario) {
        this.idBasicoPedido = idBasicoPedido;
        this.basico = basico;
        this.precioUnitario = precioUnitario;
    }

    public int getIdBasicoPedido() { return idBasicoPedido; }
    public void setIdBasicoPedido(int idBasicoPedido) { this.idBasicoPedido = idBasicoPedido; }

    public BasicoResponseDTO getBasico() { return basico; }
    public void setBasico(BasicoResponseDTO basico) { this.basico = basico; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
