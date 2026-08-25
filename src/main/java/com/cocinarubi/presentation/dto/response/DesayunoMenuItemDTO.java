package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * Vista ligera de {@link com.cocinarubi.domain.entity.Desayuno} para el menú web.
 * Solo expone los campos necesarios para renderizar la tarjeta de producto.
 * Capa: DTO de respuesta.
 */
public class DesayunoMenuItemDTO {

    private String uuidDesayuno;
    private String nombreDesayuno;
    private String descripcion;
    private BigDecimal precioMedia;
    private BigDecimal precioEntera;
    private boolean destacado;

    public DesayunoMenuItemDTO() {}

    public DesayunoMenuItemDTO(String uuidDesayuno, String nombreDesayuno, String descripcion,
                                BigDecimal precioMedia, BigDecimal precioEntera, boolean destacado) {
        this.uuidDesayuno = uuidDesayuno;
        this.nombreDesayuno = nombreDesayuno;
        this.descripcion = descripcion;
        this.precioMedia = precioMedia;
        this.precioEntera = precioEntera;
        this.destacado = destacado;
    }

    public String getUuidDesayuno() { return uuidDesayuno; }
    public void setUuidDesayuno(String uuidDesayuno) { this.uuidDesayuno = uuidDesayuno; }

    public String getNombreDesayuno() { return nombreDesayuno; }
    public void setNombreDesayuno(String nombreDesayuno) { this.nombreDesayuno = nombreDesayuno; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioMedia() { return precioMedia; }
    public void setPrecioMedia(BigDecimal precioMedia) { this.precioMedia = precioMedia; }

    public BigDecimal getPrecioEntera() { return precioEntera; }
    public void setPrecioEntera(BigDecimal precioEntera) { this.precioEntera = precioEntera; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }
}
