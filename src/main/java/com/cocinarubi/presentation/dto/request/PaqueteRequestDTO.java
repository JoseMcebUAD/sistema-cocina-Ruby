package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaqueteRequestDTO {

    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @JsonProperty("precio")
    private BigDecimal precio;

    @Size(max = 90, message = "La descripción no puede exceder 90 caracteres")
    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("destacado")
    private boolean destacado;

    @NotNull(message = "El estatus no puede ser nulo")
    @JsonProperty("estatus")
    private Estatus estatus;

    @Valid
    @NotEmpty(message = "Debe enviar al menos una línea en 'productos'")
    @JsonProperty("productos")
    private List<PaqueteLineaRequestDTO> productos = new ArrayList<>();

    public PaqueteRequestDTO() {}

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public Boolean getDestacado() { return destacado; }
    public void setDestacado(Boolean destacado) { this.destacado = destacado; }

    public List<PaqueteLineaRequestDTO> getProductos() { return productos; }
    public void setProductos(List<PaqueteLineaRequestDTO> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
    }
}
