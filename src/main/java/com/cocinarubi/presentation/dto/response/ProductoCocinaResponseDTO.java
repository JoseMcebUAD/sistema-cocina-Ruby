package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;

import java.math.BigDecimal;
import java.util.List;

public class ProductoCocinaResponseDTO {

    private int idProductoCocina;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precioDomicilio;
    private BigDecimal precioNormal;
    private Estatus estatus;
    private boolean destacado;
    private int idCategoria;
    private String nombreCategoria;
    private List<SubcategoriaResponseDTO> subcategorias;

    public ProductoCocinaResponseDTO() {}

    public ProductoCocinaResponseDTO(int idProductoCocina, String nombreProducto, String descripcion,
                                     BigDecimal precioDomicilio, BigDecimal precioNormal,
                                     Estatus estatus, boolean destacado,
                                     int idCategoria, String nombreCategoria,
                                     List<SubcategoriaResponseDTO> subcategorias) {
        this.idProductoCocina = idProductoCocina;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioDomicilio = precioDomicilio;
        this.precioNormal = precioNormal;
        this.estatus = estatus;
        this.destacado = destacado;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.subcategorias = subcategorias;
    }

    public int getIdProductoCocina() { return idProductoCocina; }
    public void setIdProductoCocina(int idProductoCocina) { this.idProductoCocina = idProductoCocina; }

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

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public List<SubcategoriaResponseDTO> getSubcategorias() { return subcategorias; }
    public void setSubcategorias(List<SubcategoriaResponseDTO> subcategorias) { this.subcategorias = subcategorias; }
}
