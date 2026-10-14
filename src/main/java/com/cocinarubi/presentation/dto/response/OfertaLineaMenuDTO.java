package com.cocinarubi.presentation.dto.response;

/**
 * Representa una línea de una oferta con descripción ya resuelta para el menú web.
 * El backend traduce los enums técnicos (ModoMatchOferta, idReferencia) a texto legible.
 * Capa: DTO de respuesta — capa de presentación web.
 */
public class OfertaLineaMenuDTO {

    private int cantidad;
    private String descripcion;

    public OfertaLineaMenuDTO() {}

    public OfertaLineaMenuDTO(int cantidad, String descripcion) {
        this.cantidad = cantidad;
        this.descripcion = descripcion;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
