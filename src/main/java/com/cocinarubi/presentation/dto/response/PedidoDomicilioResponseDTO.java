package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class PedidoDomicilioResponseDTO {

    private int idRuta;
    private String nombreRuta;
    private String direccion;
    private String codigo;
    private BigDecimal tarifasEspeciales;

    public PedidoDomicilioResponseDTO() {}

    public PedidoDomicilioResponseDTO(int idRuta, String nombreRuta, String direccion, String codigo) {
        this.idRuta = idRuta;
        this.nombreRuta = nombreRuta;
        this.direccion = direccion;
        this.codigo = codigo;
    }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public BigDecimal getTarifasEspeciales() { return tarifasEspeciales; }
    public void setTarifasEspeciales(BigDecimal tarifasEspeciales) { this.tarifasEspeciales = tarifasEspeciales; }
}
