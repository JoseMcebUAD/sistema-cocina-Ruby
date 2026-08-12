package com.cocinarubi.presentation.dto.response;

import java.util.List;

/**
 * Categoría con sus productos disponibles para el menú web.
 * Solo aparecen categorías que tienen al menos un producto con estatus DISPONIBLE.
 * Capa: DTO de respuesta.
 */
public class CategoriaMenuDTO {

    private int idCategoria;
    private String nombre;
    private List<ProductoCocinaMenuItemDTO> productos;

    public CategoriaMenuDTO() {}

    public CategoriaMenuDTO(int idCategoria, String nombre, List<ProductoCocinaMenuItemDTO> productos) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.productos = productos;
    }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<ProductoCocinaMenuItemDTO> getProductos() { return productos; }
    public void setProductos(List<ProductoCocinaMenuItemDTO> productos) { this.productos = productos; }
}
