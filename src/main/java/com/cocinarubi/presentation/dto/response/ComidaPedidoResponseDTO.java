package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TamanoPorcion;

import java.math.BigDecimal;
import java.util.List;

public class ComidaPedidoResponseDTO {

    private int idComidaPedido;
    private Integer idComida;
    private String nombreComida;
    private BigDecimal precioUnitario;
    private boolean descuento;
    private BigDecimal precioDescuento;
    private TamanoPorcion tamanoPorcion;
    private List<ComplementoResponseDTO> complementos;
    private List<ComplementoPredeterminadoComidaResponseDTO> complementosPredeterminados;

    public ComidaPedidoResponseDTO() {}

    public ComidaPedidoResponseDTO(int idComidaPedido, Integer idComida, String nombreComida,
                                   BigDecimal precioUnitario, boolean descuento, BigDecimal precioDescuento,
                                   TamanoPorcion tamanoPorcion,
                                   List<ComplementoResponseDTO> complementos,
                                   List<ComplementoPredeterminadoComidaResponseDTO> complementosPredeterminados) {
        this.idComidaPedido = idComidaPedido;
        this.idComida = idComida;
        this.nombreComida = nombreComida;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
        this.precioDescuento = precioDescuento;
        this.tamanoPorcion = tamanoPorcion;
        this.complementos = complementos;
        this.complementosPredeterminados = complementosPredeterminados;
    }

    public int getIdComidaPedido() { return idComidaPedido; }
    public void setIdComidaPedido(int idComidaPedido) { this.idComidaPedido = idComidaPedido; }

    public Integer getIdComida() { return idComida; }
    public void setIdComida(Integer idComida) { this.idComida = idComida; }

    public String getNombreComida() { return nombreComida; }
    public void setNombreComida(String nombreComida) { this.nombreComida = nombreComida; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public boolean isDescuento() { return descuento; }
    public void setDescuento(boolean descuento) { this.descuento = descuento; }

    public BigDecimal getPrecioDescuento() { return precioDescuento; }
    public void setPrecioDescuento(BigDecimal precioDescuento) { this.precioDescuento = precioDescuento; }

    public TamanoPorcion getTamanoPorcion() { return tamanoPorcion; }
    public void setTamanoPorcion(TamanoPorcion tamanoPorcion) { this.tamanoPorcion = tamanoPorcion; }

    public List<ComplementoResponseDTO> getComplementos() { return complementos; }
    public void setComplementos(List<ComplementoResponseDTO> complementos) { this.complementos = complementos; }

    public List<ComplementoPredeterminadoComidaResponseDTO> getComplementosPredeterminados() { return complementosPredeterminados; }
    public void setComplementosPredeterminados(List<ComplementoPredeterminadoComidaResponseDTO> complementosPredeterminados) { this.complementosPredeterminados = complementosPredeterminados; }
}
