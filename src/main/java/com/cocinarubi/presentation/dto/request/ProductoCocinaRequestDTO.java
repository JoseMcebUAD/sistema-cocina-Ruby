package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoProducto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ProductoCocinaRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @JsonProperty("nombreProducto")
    private String nombreProducto;

    @JsonProperty("descripcion")
    private String descripcion;

    @NotNull(message = "El precio a domicilio no puede ser nulo")
    @Positive(message = "El precio a domicilio debe ser mayor a cero")
    @JsonProperty("precioDomicilio")
    private BigDecimal precioDomicilio;

    @NotNull(message = "El precio normal no puede ser nulo")
    @Positive(message = "El precio normal debe ser mayor a cero")
    @JsonProperty("precioNormal")
    private BigDecimal precioNormal;

    @NotNull(message = "El estatus no puede ser nulo")
    @JsonProperty("estatus")
    private Estatus estatus;

    @JsonProperty("destacado")
    private boolean destacado;

    @NotNull(message = "El tipo de producto no puede ser nulo")
    @JsonProperty("tipoProducto")
    private TipoProducto tipoProducto;

    @JsonProperty("saltarConfirmacion")
    private boolean saltarConfirmacion = false;

    public ProductoCocinaRequestDTO() {}

    public ProductoCocinaRequestDTO(String nombreProducto, String descripcion,
                                    BigDecimal precioDomicilio, BigDecimal precioNormal,
                                    Estatus estatus, boolean destacado, TipoProducto tipoProducto) {
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioDomicilio = precioDomicilio;
        this.precioNormal = precioNormal;
        this.estatus = estatus;
        this.destacado = destacado;
        this.tipoProducto = tipoProducto;
    }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioDomicilio() { return precioDomicilio; }
    public void setPrecioDomicilio(BigDecimal precioDomicilio) { this.precioDomicilio = precioDomicilio; }

    public BigDecimal getPrecioNormal() { return precioNormal; }
    public void setPrecioNormal(BigDecimal precioNormal) { this.precioNormal = precioNormal; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public TipoProducto getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoProducto tipoProducto) { this.tipoProducto = tipoProducto; }

    public boolean isSaltarConfirmacion() { return saltarConfirmacion; }
    public void setSaltarConfirmacion(boolean saltarConfirmacion) { this.saltarConfirmacion = saltarConfirmacion; }
}
