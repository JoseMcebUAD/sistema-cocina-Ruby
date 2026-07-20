package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class RegistroClienteRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @JsonProperty("telefono")
    private String telefono;

    @Positive(message = "El id de ruta debe ser mayor a cero")
    @JsonProperty("idRuta")
    private Integer idRuta;

    @JsonProperty("direccion")
    private String direccion;

    public RegistroClienteRequestDTO() {}

    public RegistroClienteRequestDTO(String nombre, String telefono, Integer idRuta, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.idRuta = idRuta;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
