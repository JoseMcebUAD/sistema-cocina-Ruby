package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * Complemento predefinido asociado a una Comida, incluyendo la cantidad sugerida
 * para pre-poblar el carrito en el menú web.
 * Capa: DTO de respuesta.
 */
public class ComplementoPredeterminadoResponseDTO {

    private Integer idComplemento;
    private String nombreComplemento;
    private BigDecimal precioExtra;
    private boolean cobrarSiempre;
    private int cantidad;

    public ComplementoPredeterminadoResponseDTO() {}

    public ComplementoPredeterminadoResponseDTO(Integer idComplemento, String nombreComplemento,
                                                 BigDecimal precioExtra, boolean cobrarSiempre,
                                                 int cantidad) {
        this.idComplemento = idComplemento;
        this.nombreComplemento = nombreComplemento;
        this.precioExtra = precioExtra;
        this.cobrarSiempre = cobrarSiempre;
        this.cantidad = cantidad;
    }

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public String getNombreComplemento() { return nombreComplemento; }
    public void setNombreComplemento(String nombreComplemento) { this.nombreComplemento = nombreComplemento; }

    public BigDecimal getPrecioExtra() { return precioExtra; }
    public void setPrecioExtra(BigDecimal precioExtra) { this.precioExtra = precioExtra; }

    public boolean isCobrarSiempre() { return cobrarSiempre; }
    public void setCobrarSiempre(boolean cobrarSiempre) { this.cobrarSiempre = cobrarSiempre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
