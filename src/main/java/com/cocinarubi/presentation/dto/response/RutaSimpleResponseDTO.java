package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class RutaSimpleResponseDTO {

    private int idRuta;
    private String nombre;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer tiempoEstimadoMin;
    private Integer orden;

    public RutaSimpleResponseDTO() {}

    public RutaSimpleResponseDTO(int idRuta, String nombre, boolean active,
                                 BigDecimal tarifaEnvio, Integer tiempoEstimadoMin, Integer orden) {
        this.idRuta = idRuta;
        this.nombre = nombre;
        this.active = active;
        this.tarifaEnvio = tarifaEnvio;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
        this.orden = orden;
    }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getTarifaEnvio() { return tarifaEnvio; }
    public void setTarifaEnvio(BigDecimal tarifaEnvio) { this.tarifaEnvio = tarifaEnvio; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
