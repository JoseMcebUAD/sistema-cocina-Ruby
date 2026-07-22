package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;

import java.math.BigDecimal;

public class CodigoClienteResponseDTO {

    private int idCodigoCliente;
    private String identificador;
    private String codigoCliente;
    private BigDecimal tarifaEspecial;
    private Estatus estatus;

    public CodigoClienteResponseDTO() {}

    public CodigoClienteResponseDTO(int idCodigoCliente, String identificador, String codigoCliente,
                                    BigDecimal tarifaEspecial, Estatus estatus) {
        this.idCodigoCliente = idCodigoCliente;
        this.identificador = identificador;
        this.codigoCliente = codigoCliente;
        this.tarifaEspecial = tarifaEspecial;
        this.estatus = estatus;
    }

    public int getIdCodigoCliente() { return idCodigoCliente; }
    public void setIdCodigoCliente(int idCodigoCliente) { this.idCodigoCliente = idCodigoCliente; }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public BigDecimal getTarifaEspecial() { return tarifaEspecial; }
    public void setTarifaEspecial(BigDecimal tarifaEspecial) { this.tarifaEspecial = tarifaEspecial; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }
}
