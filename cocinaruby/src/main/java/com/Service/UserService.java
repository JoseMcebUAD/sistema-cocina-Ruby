package com.Service;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.DAO.Daos.UsuarioDAO;
import com.Model.ModeloUsuario;

import util.session.SessionUsuario;

public class UserService {
    private UsuarioDAO usuarioDAO= new UsuarioDAO();
    private SessionUsuario instanceSessionUsuario = SessionUsuario.getInstance();

    public boolean EsUsuariorRegistrado(ModeloUsuario usuario) {
        //Verificar si esta validacion si pertenece aqui
        if(!ValidarDatosLogin(usuario)){
            JOptionPane.showMessageDialog(null, "Llena los campos");
            return false;
        }
        //Esto si va
        try {
            return ValidarUsuario(usuario);
        } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Usuario no encontrado");
            return false;
        }
        
        
    }
    /**
     * funcion para verificar los campos que introduce el usuario al sistema
     * @param usuario
     * @return
     */
    private boolean ValidarDatosLogin(ModeloUsuario usuario){
    boolean resultado = usuario.getNombreUsuario()!= null && !usuario.getNombreUsuario().isBlank() 
    && usuario.getContrasenaUsuario() != null && !usuario.getContrasenaUsuario().isBlank();
    return resultado;
    }

    //petición a la base de datos para verificar si el ususario es correcto
    private boolean ValidarUsuario(ModeloUsuario usuario) throws SQLException{
    if(usuarioDAO.autenticar(usuario.getNombreUsuario(), usuario.getContrasenaUsuario()) != null){
        this.instanceSessionUsuario.guardarNombreUsuario(usuario.getNombreUsuario());
        return true;
    }else {
        return false;
    }
    }
}
