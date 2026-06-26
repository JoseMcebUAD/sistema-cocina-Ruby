package com.cocinarubi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UsuarioRequestDTO {

    @NotNull(message = "El id del rol no puede ser nulo")
    @Positive(message = "El id del rol debe ser mayor a cero")
    @JsonProperty("idRol")
    private Integer idRol;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(max = 20, message = "El nombre de usuario no puede exceder 20 caracteres")
    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 5, max = 5, message = "La contraseña debe ser exactamente 5 caracteres")
    @JsonProperty("contrasena")
    private String contrasena;

    public UsuarioRequestDTO() {}

    public UsuarioRequestDTO(Integer idRol, String nombreUsuario, String contrasena) {
        this.idRol = idRol;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
