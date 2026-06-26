package com.cocinarubi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class RutaRequestDTO {

    @NotBlank(message = "El nombre de la ruta no puede estar vacío")
    @Size(max = 45, message = "El nombre no puede exceder 45 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El boundary WKT no puede estar vacío")
    @JsonProperty("boundaryWkt")
    private String boundaryWkt;

    @JsonProperty("isActive")
    private boolean active;

    @NotNull(message = "La tarifa de envío no puede ser nula")
    @Positive(message = "La tarifa de envío debe ser mayor a cero")
    @JsonProperty("tarifaEnvio")
    private BigDecimal tarifaEnvio;

    @JsonProperty("tiempoEstimadoMin")
    private Integer tiempoEstimadoMin;

    public RutaRequestDTO() {}

    public RutaRequestDTO(String nombre, String boundaryWkt, boolean active,
                          BigDecimal tarifaEnvio, Integer tiempoEstimadoMin) {
        this.nombre = nombre;
        this.boundaryWkt = boundaryWkt;
        this.active = active;
        this.tarifaEnvio = tarifaEnvio;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

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
