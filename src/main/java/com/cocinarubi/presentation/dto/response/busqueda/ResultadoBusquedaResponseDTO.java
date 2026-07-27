package com.cocinarubi.presentation.dto.response.busqueda;

import java.util.List;

/**
 * Respuesta global de búsqueda cross-catálogo con metadatos de paginación.
 */
public class ResultadoBusquedaResponseDTO {

    private List<CategoriaResultadoResponseDTO> categorias;
    private long totalElementos;
    private int totalPaginas;
    private int paginaActual;
    private int tamanioPagina;

    public ResultadoBusquedaResponseDTO() {}

    public ResultadoBusquedaResponseDTO(List<CategoriaResultadoResponseDTO> categorias,
                                        long totalElementos, int totalPaginas,
                                        int paginaActual, int tamanioPagina) {
        this.categorias = categorias;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
        this.paginaActual = paginaActual;
        this.tamanioPagina = tamanioPagina;
    }

    public List<CategoriaResultadoResponseDTO> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaResultadoResponseDTO> categorias) { this.categorias = categorias; }

    public long getTotalElementos() { return totalElementos; }
    public void setTotalElementos(long totalElementos) { this.totalElementos = totalElementos; }

    public int getTotalPaginas() { return totalPaginas; }
    public void setTotalPaginas(int totalPaginas) { this.totalPaginas = totalPaginas; }

    public int getPaginaActual() { return paginaActual; }
    public void setPaginaActual(int paginaActual) { this.paginaActual = paginaActual; }

    public int getTamanioPagina() { return tamanioPagina; }
    public void setTamanioPagina(int tamanioPagina) { this.tamanioPagina = tamanioPagina; }
}
