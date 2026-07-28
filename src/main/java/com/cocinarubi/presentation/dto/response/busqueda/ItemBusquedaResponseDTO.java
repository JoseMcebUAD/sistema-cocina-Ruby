package com.cocinarubi.presentation.dto.response.busqueda;

/**
 * Ítem plano de búsqueda cross-catálogo.
 * Agrupa la categoría a la que pertenece con el objeto resultado correspondiente.
 */
public class ItemBusquedaResponseDTO {

    private String categoria;
    private Object resultado;

    public ItemBusquedaResponseDTO() {}

    public ItemBusquedaResponseDTO(String categoria, Object resultado) {
        this.categoria = categoria;
        this.resultado = resultado;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Object getResultado() { return resultado; }
    public void setResultado(Object resultado) { this.resultado = resultado; }
}
