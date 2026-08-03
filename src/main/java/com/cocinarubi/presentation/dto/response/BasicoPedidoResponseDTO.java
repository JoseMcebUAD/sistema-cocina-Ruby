package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class BasicoPedidoResponseDTO {

    private int idBasicoPedido;
    private BasicoResponseDTO basico;
    private BigDecimal precioUnitario;
    private List<BasicoPedidoExtraResponseDTO> extras;

    public BasicoPedidoResponseDTO() {}

    public BasicoPedidoResponseDTO(int idBasicoPedido, BasicoResponseDTO basico,
                                    BigDecimal precioUnitario, List<BasicoPedidoExtraResponseDTO> extras) {
        this.idBasicoPedido = idBasicoPedido;
        this.basico = basico;
        this.precioUnitario = precioUnitario;
        this.extras = extras;
    }

    public int getIdBasicoPedido() { return idBasicoPedido; }
    public void setIdBasicoPedido(int idBasicoPedido) { this.idBasicoPedido = idBasicoPedido; }

    public BasicoResponseDTO getBasico() { return basico; }
    public void setBasico(BasicoResponseDTO basico) { this.basico = basico; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public List<BasicoPedidoExtraResponseDTO> getExtras() { return extras; }
    public void setExtras(List<BasicoPedidoExtraResponseDTO> extras) { this.extras = extras; }
}
