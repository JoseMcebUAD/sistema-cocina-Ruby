package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
//DTO para @Basico
public class ComplementoResponseDTO {

    private Integer idComplemento;
    private String nombreComplemento;
    private BigDecimal precioExtra;
    // Indica que el complemento se cobra siempre, aun cuando forma parte de un básico
    private boolean cobrarSiempre;

    public ComplementoResponseDTO() {}

    public ComplementoResponseDTO(Integer idComplemento, String nombreComplemento,
                                  BigDecimal precioExtra, boolean cobrarSiempre) {
        this.idComplemento = idComplemento;
        this.nombreComplemento = nombreComplemento;
        this.precioExtra = precioExtra;
        this.cobrarSiempre = cobrarSiempre;
    }

    public Integer getIdComplemento() { return idComplemento; }
    public void setIdComplemento(Integer idComplemento) { this.idComplemento = idComplemento; }

    public String getNombreComplemento() { return nombreComplemento; }
    public void setNombreComplemento(String nombreComplemento) { this.nombreComplemento = nombreComplemento; }

    public BigDecimal getPrecioExtra() { return precioExtra; }
    public void setPrecioExtra(BigDecimal precioExtra) { this.precioExtra = precioExtra; }

    public boolean isCobrarSiempre() { return cobrarSiempre; }
    public void setCobrarSiempre(boolean cobrarSiempre) { this.cobrarSiempre = cobrarSiempre; }
}
