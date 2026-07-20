package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
//DTO para @Basico
public class ComplementoResponseDTO {

    private int idComplemento;
    private String nombreComplemento;
    private BigDecimal precioExtra;

    public ComplementoResponseDTO() {}

    public ComplementoResponseDTO(int idComplemento, String nombreComplemento, BigDecimal precioExtra) {
        this.idComplemento = idComplemento;
        this.nombreComplemento = nombreComplemento;
        this.precioExtra = precioExtra;
    }

    public int getIdComplemento() { return idComplemento; }
    public void setIdComplemento(int idComplemento) { this.idComplemento = idComplemento; }

    public String getNombreComplemento() { return nombreComplemento; }
    public void setNombreComplemento(String nombreComplemento) { this.nombreComplemento = nombreComplemento; }

    public BigDecimal getPrecioExtra() { return precioExtra; }
    public void setPrecioExtra(BigDecimal precioExtra) { this.precioExtra = precioExtra; }
}
