package com.cocinarubi.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * DTO de respuesta para {@link com.cocinarubi.domain.entity.Categoria}.
 *
 * <p>El campo {@code subcategorias} es opcional: se puebla únicamente en el
 * endpoint {@code GET /categoria/con-subcategorias} y queda {@code null} (omitido
 * del JSON gracias a {@link JsonInclude}) en el resto de respuestas.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoriaResponseDTO {

    private int idCategoria;
    private String nombre;
    private List<SubcategoriaResponseDTO> subcategorias;

    public CategoriaResponseDTO() {}

    public CategoriaResponseDTO(int idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    public CategoriaResponseDTO(int idCategoria, String nombre,
                                List<SubcategoriaResponseDTO> subcategorias) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.subcategorias = subcategorias;
    }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<SubcategoriaResponseDTO> getSubcategorias() { return subcategorias; }
    public void setSubcategorias(List<SubcategoriaResponseDTO> subcategorias) {
        this.subcategorias = subcategorias;
    }
}
