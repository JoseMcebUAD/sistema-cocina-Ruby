package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class TarjetaTransferenciaEstatusRequestDTO {

    @NotNull(message = "El estatus no puede ser nulo")
    @JsonProperty("estatus")
    private Estatus estatus;

    public TarjetaTransferenciaEstatusRequestDTO() {}

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }
}
