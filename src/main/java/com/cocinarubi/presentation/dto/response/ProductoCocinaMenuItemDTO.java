package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * Vista ligera de {@link com.cocinarubi.domain.entity.ProductoCocina} para el menú web.
 * Solo expone los campos necesarios para renderizar la tarjeta dentro de su categoría.
 * Capa: DTO de respuesta.
 */
public class ProductoCocinaMenuItemDTO {

    private String uuidProductoCocina;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precioDomicilio;
    private BigDecimal precioNormal;
    private boolean destacado;

    public ProductoCocinaMenuItemDTO() {}

    public ProductoCocinaMenuItemDTO(String uuidProductoCocina, String nombreProducto,
                                      String descripcion, BigDecimal precioDomicilio,
                                      BigDecimal precioNormal, boolean destacado) {
        this.uuidProductoCocina = uuidProductoCocina;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioDomicilio = precioDomicilio;
        this.precioNormal = precioNormal;
        this.destacado = destacado;
    }

    public String getUuidProductoCocina() { return uuidProductoCocina; }
    public void setUuidProductoCocina(String uuidProductoCocina) { this.uuidProductoCocina = uuidProductoCocina; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioDomicilio() { return precioDomicilio; }
    public void setPrecioDomicilio(BigDecimal precioDomicilio) { this.precioDomicilio = precioDomicilio; }

    public BigDecimal getPrecioNormal() { return precioNormal; }
    public void setPrecioNormal(BigDecimal precioNormal) { this.precioNormal = precioNormal; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }
}
