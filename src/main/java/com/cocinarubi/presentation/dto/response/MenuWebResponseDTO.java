package com.cocinarubi.presentation.dto.response;

import java.util.List;

/**
 * Agrega todas las secciones del menú web en una sola respuesta.
 * El frontend hace una única petición HTTP para cargar la página completa.
 * Capa: DTO de respuesta.
 */
public class MenuWebResponseDTO {

    private List<ComidaMenuItemDTO> comidas;
    private List<BasicoResponseDTO> basicos;
    private List<DesayunoMenuItemDTO> desayunos;
    private List<PaqueteResponseDTO> paquetes;
    private List<CategoriaMenuDTO> categorias;

    public MenuWebResponseDTO() {}

    public MenuWebResponseDTO(List<ComidaMenuItemDTO> comidas,
                               List<BasicoResponseDTO> basicos,
                               List<DesayunoMenuItemDTO> desayunos,
                               List<PaqueteResponseDTO> paquetes,
                               List<CategoriaMenuDTO> categorias) {
        this.comidas = comidas;
        this.basicos = basicos;
        this.desayunos = desayunos;
        this.paquetes = paquetes;
        this.categorias = categorias;
    }

    public List<ComidaMenuItemDTO> getComidas() { return comidas; }
    public void setComidas(List<ComidaMenuItemDTO> comidas) { this.comidas = comidas; }

    public List<BasicoResponseDTO> getBasicos() { return basicos; }
    public void setBasicos(List<BasicoResponseDTO> basicos) { this.basicos = basicos; }

    public List<DesayunoMenuItemDTO> getDesayunos() { return desayunos; }
    public void setDesayunos(List<DesayunoMenuItemDTO> desayunos) { this.desayunos = desayunos; }

    public List<PaqueteResponseDTO> getPaquetes() { return paquetes; }
    public void setPaquetes(List<PaqueteResponseDTO> paquetes) { this.paquetes = paquetes; }

    public List<CategoriaMenuDTO> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaMenuDTO> categorias) { this.categorias = categorias; }
}
