package com.cocinarubi.presentation.dto.response.busqueda;

import java.util.List;

/**
 * Agrupa los ítems de una categoría que aparecen en la página actual de la búsqueda.
 */
public class CategoriaResultadoResponseDTO {

    private String categoria;
    private List<?> resultados;

    public CategoriaResultadoResponseDTO() {}

    public CategoriaResultadoResponseDTO(String categoria, List<?> resultados) {
        this.categoria = categoria;
        this.resultados = resultados;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<?> getResultados() { return resultados; }
    public void setResultados(List<?> resultados) { this.resultados = resultados; }
}
