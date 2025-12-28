package com.Model;

/**
 * Representa la entidad 'tipo_usuario' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * de un tipo de usuario (administrador,empleado...) (ID, nombre, permisos) a través de las diferentes capas de la aplicación
 */
public class ModeloTipoUsuario {
    private int idTipoUsuario;
    private String nombreTipoUsuario;
    private String permisosUsuario;

    public int getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }

    public String getNombreTipoUsuario() {
        return nombreTipoUsuario;
    }

    public void setNombreTipoUsuario(String nombreTipoUsuario) {
        this.nombreTipoUsuario = nombreTipoUsuario;
    }

    public String getPermisosUsuario() {
        return permisosUsuario;
    }

    public void setPermisosUsuario(String permisosUsuario) {
        this.permisosUsuario = permisosUsuario;
    }
}
