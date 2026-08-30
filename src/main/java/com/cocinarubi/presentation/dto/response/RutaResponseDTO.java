package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class RutaResponseDTO {

    private int idRuta;
    private String uuidRuta;
    private String nombre;
    private List<CoordinateDTO> coordinates;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer idOrdenRuta;

    public static class CoordinateDTO {
        private double latitude;
        private double longitude;

        public CoordinateDTO(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    public RutaResponseDTO() {}

    public RutaResponseDTO(int idRuta, String uuidRuta, String nombre, List<CoordinateDTO> coordinates,
                           boolean active, BigDecimal tarifaEnvio, Integer idOrdenRuta) {
        this.idRuta = idRuta;
        this.uuidRuta = uuidRuta;
        this.nombre = nombre;
        this.coordinates = coordinates;
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

    public List<CoordinateDTO> getCoordinates() { return coordinates; }
    public void setCoordinates(List<CoordinateDTO> coordinates) { this.coordinates = coordinates; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getTarifaEnvio() { return tarifaEnvio; }
    public void setTarifaEnvio(BigDecimal tarifaEnvio) { this.tarifaEnvio = tarifaEnvio; }

    public Integer getIdOrdenRuta() { return idOrdenRuta; }
    public void setIdOrdenRuta(Integer idOrdenRuta) { this.idOrdenRuta = idOrdenRuta; }
}
