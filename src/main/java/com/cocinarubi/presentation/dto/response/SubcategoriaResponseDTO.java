package com.cocinarubi.presentation.dto.response;

public class SubcategoriaResponseDTO {

    private int idSubcategoria;
    private String nombre;
    private int idCategoria;
    private String nombreCategoria;

    public SubcategoriaResponseDTO() {}

    public SubcategoriaResponseDTO(int idSubcategoria, String nombre,
                                   int idCategoria, String nombreCategoria) {
        this.idSubcategoria = idSubcategoria;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    public int getIdSubcategoria() { return idSubcategoria; }
    public void setIdSubcategoria(int idSubcategoria) { this.idSubcategoria = idSubcategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
}
