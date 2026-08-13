package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(max = 60, message = "El nombre de la categoría no puede exceder 60 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    public CategoriaRequestDTO() {}

    public CategoriaRequestDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
