package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RegistroClienteRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size (max = 12,min = 10, message = "El número telefónico debe de tener 10 dígitos en el (máximo 12 si tiene extensión de otro país)")
    @JsonProperty("telefono")
    private String telefono;

    @Positive(message = "El id de ruta debe ser mayor a cero")
    @JsonProperty("idRuta")
    private Integer idRuta;

    @JsonProperty("direcciones")
    private List<String> direcciones;

    public RegistroClienteRequestDTO() {}

    public RegistroClienteRequestDTO(String nombre, String telefono, Integer idRuta, List<String> direcciones) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.idRuta = idRuta;
        this.direcciones = direcciones;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public List<String> getDirecciones() { return direcciones; }
    public void setDirecciones(List<String> direcciones) { this.direcciones = direcciones; }
}
