package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoPedido;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OfertaRequestDTO {

    @NotBlank(message = "El nombre de la oferta no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @Size(max = 400, message = "La descripción no puede superar 400 caracteres")
    @JsonProperty("descripcion")
    private String descripcion;

    @NotNull(message = "El precio de la oferta no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @JsonProperty("precioOferta")
    private BigDecimal precioOferta;

    @JsonProperty("prioridad")
    private int prioridad = 0;

    @JsonProperty("activo")
    private boolean activo = true;

    @JsonProperty("fechaInicio")
    private LocalDate fechaInicio;

    @JsonProperty("fechaFin")
    private LocalDate fechaFin;

    /** Tipos de pedido en los que aplica la oferta. Vacío = sin restricción (todos). */
    @JsonProperty("tiposPedidoRestriccion")
    private Set<TipoPedido> tiposPedidoRestriccion = new HashSet<>();

    @Valid
    @NotEmpty(message = "La oferta debe tener al menos una línea de matching")
    @JsonProperty("lineas")
    private List<OfertaLineaRequestDTO> lineas = new ArrayList<>();

    public OfertaRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Set<TipoPedido> getTiposPedidoRestriccion() { return tiposPedidoRestriccion; }
    public void setTiposPedidoRestriccion(Set<TipoPedido> tiposPedidoRestriccion) {
        this.tiposPedidoRestriccion = tiposPedidoRestriccion != null ? tiposPedidoRestriccion : new HashSet<>();
    }

    public List<OfertaLineaRequestDTO> getLineas() { return lineas; }
    public void setLineas(List<OfertaLineaRequestDTO> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }
}
