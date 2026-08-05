package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

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

    @NotNull(message = "El id de la categoría no puede ser nulo")
    @JsonProperty("idCategoria")
    private Integer idCategoria;

    // Subcategorías asignadas al producto (0..N). Todas deben pertenecer a la
    // categoría indicada en idCategoria; el service valida con 409 si mezclan.
    @JsonProperty("idSubcategorias")
    private List<Integer> idSubcategorias;

    @JsonProperty("saltarConfirmacion")
    private boolean saltarConfirmacion = false;

    public ProductoCocinaRequestDTO() {}

    public ProductoCocinaRequestDTO(String nombreProducto, String descripcion,
                                    BigDecimal precioDomicilio, BigDecimal precioNormal,
                                    Estatus estatus, boolean destacado,
                                    Integer idCategoria, List<Integer> idSubcategorias) {
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioDomicilio = precioDomicilio;
        this.precioNormal = precioNormal;
        this.estatus = estatus;
        this.destacado = destacado;
        this.idCategoria = idCategoria;
        this.idSubcategorias = idSubcategorias;
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

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public List<Integer> getIdSubcategorias() { return idSubcategorias; }
    public void setIdSubcategorias(List<Integer> idSubcategorias) { this.idSubcategorias = idSubcategorias; }

    public boolean isSaltarConfirmacion() { return saltarConfirmacion; }
    public void setSaltarConfirmacion(boolean saltarConfirmacion) { this.saltarConfirmacion = saltarConfirmacion; }
}
