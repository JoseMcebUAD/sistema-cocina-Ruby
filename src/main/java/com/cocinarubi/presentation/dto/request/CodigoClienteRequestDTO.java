package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CodigoClienteRequestDTO {

    @NotBlank(message = "El identificador no puede estar vacío")
    @JsonProperty("identificador")
    private String identificador;

    @NotBlank(message = "El código del cliente no puede estar vacío")
    @Size(min = 5, max = 20, message = "El código debe tener un mínimo de 5 y máximo de caracteres")
    @JsonProperty("codigoCliente")
    private String codigoCliente;

    @NotNull(message = "La tarifa especial no puede ser nula")
    @Positive(message = "La tarifa especial debe ser mayor a cero")
    @JsonProperty("tarifaEspecial")
    private BigDecimal tarifaEspecial;

    @NotNull(message = "El estatus no puede ser nulo")
    @JsonProperty("estatus")
    private Estatus estatus;

    public CodigoClienteRequestDTO() {}

    public CodigoClienteRequestDTO(String identificador, String codigoCliente,
                                   BigDecimal tarifaEspecial, Estatus estatus) {
        this.identificador = identificador;
        this.codigoCliente = codigoCliente;
        this.tarifaEspecial = tarifaEspecial;
        this.estatus = estatus;
    }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public BigDecimal getTarifaEspecial() { return tarifaEspecial; }
    public void setTarifaEspecial(BigDecimal tarifaEspecial) { this.tarifaEspecial = tarifaEspecial; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }
}
