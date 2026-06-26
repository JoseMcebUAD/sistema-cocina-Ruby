package com.cocinarubi.dto.response;

import java.math.BigDecimal;

public class RutaResponseDTO {

    private int idRuta;
    private String nombre;
    private String boundaryWkt;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer tiempoEstimadoMin;

    public RutaResponseDTO() {}

    public RutaResponseDTO(int idRuta, String nombre, String boundaryWkt, boolean active,
                           BigDecimal tarifaEnvio, Integer tiempoEstimadoMin) {
        this.idRuta = idRuta;
        this.nombre = nombre;
        this.boundaryWkt = boundaryWkt;
        this.active = active;
        this.tarifaEnvio = tarifaEnvio;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getBoundaryWkt() { return boundaryWkt; }
    public void setBoundaryWkt(String boundaryWkt) { this.boundaryWkt = boundaryWkt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getTarifaEnvio() { return tarifaEnvio; }
    public void setTarifaEnvio(BigDecimal tarifaEnvio) { this.tarifaEnvio = tarifaEnvio; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }
}
