package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoPedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Vista simplificada de una oferta para el menú web del carrito de compras.
 * Expone solo los campos relevantes para el cliente; las líneas incluyen
 * descripciones ya resueltas por el backend (sin enums técnicos).
 * Capa: DTO de respuesta — capa de presentación web.
 */
public class OfertaMenuDTO {

    private int idOferta;
    private String nombre;
    private String descripcion;
    private BigDecimal precioOferta;
    private Set<TipoPedido> tiposPedidoRestriccion;
    private List<OfertaLineaMenuDTO> lineas;

    public OfertaMenuDTO() {}

    public OfertaMenuDTO(int idOferta, String nombre, String descripcion,
                         BigDecimal precioOferta, Set<TipoPedido> tiposPedidoRestriccion,
                         List<OfertaLineaMenuDTO> lineas) {
        this.idOferta = idOferta;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioOferta = precioOferta;
        this.tiposPedidoRestriccion = tiposPedidoRestriccion;
        this.lineas = lineas;
    }

    public int getIdOferta() { return idOferta; }
    public void setIdOferta(int idOferta) { this.idOferta = idOferta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public Set<TipoPedido> getTiposPedidoRestriccion() { return tiposPedidoRestriccion; }
    public void setTiposPedidoRestriccion(Set<TipoPedido> tiposPedidoRestriccion) { this.tiposPedidoRestriccion = tiposPedidoRestriccion; }

    public List<OfertaLineaMenuDTO> getLineas() { return lineas; }
    public void setLineas(List<OfertaLineaMenuDTO> lineas) { this.lineas = lineas; }
}
