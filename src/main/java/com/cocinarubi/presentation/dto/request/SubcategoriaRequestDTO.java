package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubcategoriaRequestDTO {

    @NotBlank(message = "El nombre de la subcategoría no puede estar vacío")
    @Size(max = 60, message = "El nombre de la subcategoría no puede exceder 60 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @NotNull(message = "El id de la categoría no puede ser nulo")
    @JsonProperty("idCategoria")
    private Integer idCategoria;

    public SubcategoriaRequestDTO() {}

    public SubcategoriaRequestDTO(String nombre, Integer idCategoria) {
        this.nombre = nombre;
        this.idCategoria = idCategoria;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
}
