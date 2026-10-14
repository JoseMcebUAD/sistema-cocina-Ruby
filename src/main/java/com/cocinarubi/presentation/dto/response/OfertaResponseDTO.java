package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class OfertaResponseDTO {

    private int idOferta;
    private String nombre;
    private String descripcion;
    private BigDecimal precioOferta;
    private int prioridad;
    private boolean activo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Set<TipoPedido> tiposPedidoRestriccion;
    private List<OfertaLineaResponseDTO> lineas;

    public OfertaResponseDTO() {}

    public int getIdOferta() { return idOferta; }
    public void setIdOferta(int idOferta) { this.idOferta = idOferta; }

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
    public void setTiposPedidoRestriccion(Set<TipoPedido> tiposPedidoRestriccion) { this.tiposPedidoRestriccion = tiposPedidoRestriccion; }

    public List<OfertaLineaResponseDTO> getLineas() { return lineas; }
    public void setLineas(List<OfertaLineaResponseDTO> lineas) { this.lineas = lineas; }
}
