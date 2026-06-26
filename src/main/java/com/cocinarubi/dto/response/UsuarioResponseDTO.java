package com.cocinarubi.dto.response;

import java.time.LocalDateTime;

public class UsuarioResponseDTO {

    private int idUsuario;
    private String nombreUsuario;
    private String nombreRol;
    private LocalDateTime creadoEn;
    private LocalDateTime ultimoLogin;

    public UsuarioResponseDTO() {}

    public UsuarioResponseDTO(int idUsuario, String nombreUsuario, String nombreRol,
                              LocalDateTime creadoEn, LocalDateTime ultimoLogin) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombreRol = nombreRol;
        this.creadoEn = creadoEn;
        this.ultimoLogin = ultimoLogin;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
}
