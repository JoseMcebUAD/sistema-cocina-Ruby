package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * DTO de respuesta para un complemento extra de un paquete básico en el pedido.
 *
 * <p>El {@code precio} refleja el valor histórico capturado al momento de la orden.</p>
 */
public class BasicoPedidoExtraResponseDTO {

    private int idBasicoPedidoExtra;
    private Integer idComplemento;
    private String nombreComplemento;
    private int cantidad;
    private BigDecimal precio;

    public BasicoPedidoExtraResponseDTO() {}

    public BasicoPedidoExtraResponseDTO(int idBasicoPedidoExtra, Integer idComplemento,
                                         String nombreComplemento, int cantidad, BigDecimal precio) {
        this.idBasicoPedidoExtra = idBasicoPedidoExtra;
        this.idComplemento = idComplemento;
        this.nombreComplemento = nombreComplemento;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public int getIdBasicoPedidoExtra() { return idBasicoPedidoExtra; }
    public void setIdBasicoPedidoExtra(int idBasicoPedidoExtra) { this.idBasicoPedidoExtra = idBasicoPedidoExtra; }

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public String getNombreComplemento() { return nombreComplemento; }
    public void setNombreComplemento(String nombreComplemento) { this.nombreComplemento = nombreComplemento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
