package com.cocinarubi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class TarifaEspecialRequestDTO {

    @NotBlank(message = "El nombre de la tarifa no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @JsonProperty("nombreTarifa")
    private String nombreTarifa;

    @NotNull(message = "La tarifa no puede ser nula")
    @Positive(message = "La tarifa debe ser mayor a cero")
    @JsonProperty("tarifa")
    private BigDecimal tarifa;

    @JsonProperty("isActive")
    private boolean active;

    public TarifaEspecialRequestDTO() {}

    public TarifaEspecialRequestDTO(String nombreTarifa, BigDecimal tarifa, boolean active) {
        this.nombreTarifa = nombreTarifa;
        this.tarifa = tarifa;
        this.active = active;
    }

    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }

    public BigDecimal getTarifa() { return tarifa; }
    public void setTarifa(BigDecimal tarifa) { this.tarifa = tarifa; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
