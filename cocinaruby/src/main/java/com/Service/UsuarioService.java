package com.Service;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.DAO.Daos.UsuarioDAO;
import com.Model.ModeloUsuario;

public class UsuarioService {
    private UsuarioDAO usuarioDAO= new UsuarioDAO();

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
    private boolean ValidarDatosLogin(ModeloUsuario usuario){
    boolean resultado = usuario.getNombreUsuario()!= null && !usuario.getNombreUsuario().isBlank() 
    && usuario.getContrasenaUsuario() != null && !usuario.getContrasenaUsuario().isBlank();
    return resultado;
    }

    private boolean ValidarUsuario(ModeloUsuario usuario) throws SQLException{
    if(usuarioDAO.autenticar(usuario.getNombreUsuario(), usuario.getContrasenaUsuario()) != null){
        return true;
    }else {
        return false;
    }
    }
}
