package com.Model;

/**
 * Representa la entidad 'usuario' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * de un usuario para ingresar a la sistema */
public class ModeloUsuario {
    private int idUsuario;
    private int idRelTipoUsuario;
    private String nombreUsuario;
    private String contrasenaUsuario;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRelTipoUsuario() {
        return idRelTipoUsuario;
    }

    public void setIdRelTipoUsuario(int idRelTipoUsuario) {
        this.idRelTipoUsuario = idRelTipoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaUsuario() {
        return contrasenaUsuario;
    }

    public void setContrasenaUsuario(String contrasenaUsuario) {
        this.contrasenaUsuario = contrasenaUsuario;
    }
}
