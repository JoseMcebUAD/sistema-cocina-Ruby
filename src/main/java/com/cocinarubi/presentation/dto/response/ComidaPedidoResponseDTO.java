package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TamanoPorcion;

import java.math.BigDecimal;
import java.util.List;

public class ComidaPedidoResponseDTO {

    private int idComidaPedido;
    private int idComida;
    private String nombreComida;
    private BigDecimal precioUnitario;
    private TamanoPorcion tamanoPorcion;
    private List<ComplementoResponseDTO> complementos;

    public ComidaPedidoResponseDTO() {}

    public ComidaPedidoResponseDTO(int idComidaPedido, int idComida, String nombreComida,
                                   BigDecimal precioUnitario, TamanoPorcion tamanoPorcion,
                                   List<ComplementoResponseDTO> complementos) {
        this.idComidaPedido = idComidaPedido;
        this.idComida = idComida;
        this.nombreComida = nombreComida;
        this.precioUnitario = precioUnitario;
        this.tamanoPorcion = tamanoPorcion;
        this.complementos = complementos;
    }

    public int getIdComidaPedido() { return idComidaPedido; }
    public void setIdComidaPedido(int idComidaPedido) { this.idComidaPedido = idComidaPedido; }

    public int getIdComida() { return idComida; }
    public void setIdComida(int idComida) { this.idComida = idComida; }

    public String getNombreComida() { return nombreComida; }
    public void setNombreComida(String nombreComida) { this.nombreComida = nombreComida; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public TamanoPorcion getTamanoPorcion() { return tamanoPorcion; }
    public void setTamanoPorcion(TamanoPorcion tamanoPorcion) { this.tamanoPorcion = tamanoPorcion; }

    public List<ComplementoResponseDTO> getComplementos() { return complementos; }
    public void setComplementos(List<ComplementoResponseDTO> complementos) { this.complementos = complementos; }
}
