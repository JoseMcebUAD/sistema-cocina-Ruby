package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * Vista ligera de {@link com.cocinarubi.domain.entity.Comida} para el menú web.
 * Solo expone los campos necesarios para renderizar la tarjeta de producto.
 * Capa: DTO de respuesta.
 */
public class ComidaMenuItemDTO {

    private String uuidComida;
    private String nombreComida;
    private String descripcion;
    private BigDecimal precioMedia;
    private BigDecimal precioEntera;
    private boolean destacado;
    private Integer limiteComplemento;

    public ComidaMenuItemDTO() {}

    public ComidaMenuItemDTO(String uuidComida, String nombreComida, String descripcion,
                              BigDecimal precioMedia, BigDecimal precioEntera,
                              boolean destacado, Integer limiteComplemento) {
        this.uuidComida = uuidComida;
        this.nombreComida = nombreComida;
        this.descripcion = descripcion;
        this.precioMedia = precioMedia;
        this.precioEntera = precioEntera;
        this.destacado = destacado;
        this.limiteComplemento = limiteComplemento;
    }

    public String getUuidComida() { return uuidComida; }
    public void setUuidComida(String uuidComida) { this.uuidComida = uuidComida; }

    public String getNombreComida() { return nombreComida; }
    public void setNombreComida(String nombreComida) { this.nombreComida = nombreComida; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioMedia() { return precioMedia; }
    public void setPrecioMedia(BigDecimal precioMedia) { this.precioMedia = precioMedia; }

    public BigDecimal getPrecioEntera() { return precioEntera; }
    public void setPrecioEntera(BigDecimal precioEntera) { this.precioEntera = precioEntera; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public Integer getLimiteComplemento() { return limiteComplemento; }
    public void setLimiteComplemento(Integer limiteComplemento) { this.limiteComplemento = limiteComplemento; }
}
