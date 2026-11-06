package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.OrigenSuscripcion;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SuscripcionSemanalRequestDTO {

    @NotBlank(message = "El identificador del cliente no puede estar vacío")
    @JsonProperty("identificadorCliente")
    private String identificadorCliente;

    @JsonProperty("uuidCliente")
    private String uuidCliente;

    @NotNull(message = "La fecha de inicio de semana no puede ser nula")
    @JsonProperty("semanaInicio")
    private LocalDate semanaInicio;

    @NotNull(message = "Las comidas por día no pueden ser nulas")
    @Min(value = 1, message = "Debe contratar al menos 1 comida por día")
    @Max(value = 10, message = "No se pueden contratar más de 10 comidas por día")
    @JsonProperty("comidasPorDia")
    private Integer comidasPorDia;

    @NotNull(message = "El precio de la suscripción no puede ser nulo")
    @Positive(message = "El precio debe ser mayor a cero")
    @JsonProperty("precioSuscripcion")
    private BigDecimal precioSuscripcion;

    @NotNull(message = "El método de pago no puede ser nulo")
    @JsonProperty("metodoPago")
    private MetodoPago metodoPago;

    @JsonProperty("codigoRecuperacion")
    private String codigoRecuperacion;

    @JsonProperty("activo")
    private boolean activo = true;

    @NotNull(message = "El origen no puede ser nulo")
    @JsonProperty("origen")
    private OrigenSuscripcion origen;

    public SuscripcionSemanalRequestDTO() {}

    public SuscripcionSemanalRequestDTO(String identificadorCliente, String uuidCliente,
                                        LocalDate semanaInicio, Integer comidasPorDia,
                                        BigDecimal precioSuscripcion, MetodoPago metodoPago,
                                        String codigoRecuperacion, boolean activo,
                                        OrigenSuscripcion origen) {
        this.identificadorCliente = identificadorCliente;
        this.uuidCliente = uuidCliente;
        this.semanaInicio = semanaInicio;
        this.comidasPorDia = comidasPorDia;
        this.precioSuscripcion = precioSuscripcion;
        this.metodoPago = metodoPago;
        this.codigoRecuperacion = codigoRecuperacion;
        this.activo = activo;
        this.origen = origen;
    }

    public String getIdentificadorCliente() { return identificadorCliente; }
    public void setIdentificadorCliente(String identificadorCliente) { this.identificadorCliente = identificadorCliente; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public LocalDate getSemanaInicio() { return semanaInicio; }
    public void setSemanaInicio(LocalDate semanaInicio) { this.semanaInicio = semanaInicio; }

    public Integer getComidasPorDia() { return comidasPorDia; }
    public void setComidasPorDia(Integer comidasPorDia) { this.comidasPorDia = comidasPorDia; }

    public BigDecimal getPrecioSuscripcion() { return precioSuscripcion; }
    public void setPrecioSuscripcion(BigDecimal precioSuscripcion) { this.precioSuscripcion = precioSuscripcion; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getCodigoRecuperacion() { return codigoRecuperacion; }
    public void setCodigoRecuperacion(String codigoRecuperacion) { this.codigoRecuperacion = codigoRecuperacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public OrigenSuscripcion getOrigen() { return origen; }
    public void setOrigen(OrigenSuscripcion origen) { this.origen = origen; }
}
