package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class RutaSimpleResponseDTO {

    private int idRuta;
    private String uuidRuta;
    private String nombre;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer idOrdenRuta;

    public RutaSimpleResponseDTO() {}

    public RutaSimpleResponseDTO(int idRuta, String uuidRuta, String nombre, boolean active,
                                 BigDecimal tarifaEnvio, Integer idOrdenRuta) {
        this.idRuta = idRuta;
        this.uuidRuta = uuidRuta;
        this.nombre = nombre;
        this.active = active;
        this.tarifaEnvio = tarifaEnvio;
        this.idOrdenRuta = idOrdenRuta;
    }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getUuidRuta() { return uuidRuta; }
    public void setUuidRuta(String uuidRuta) { this.uuidRuta = uuidRuta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getTarifaEnvio() { return tarifaEnvio; }
    public void setTarifaEnvio(BigDecimal tarifaEnvio) { this.tarifaEnvio = tarifaEnvio; }

    public Integer getIdOrdenRuta() { return idOrdenRuta; }
    public void setIdOrdenRuta(Integer idOrdenRuta) { this.idOrdenRuta = idOrdenRuta; }
}
