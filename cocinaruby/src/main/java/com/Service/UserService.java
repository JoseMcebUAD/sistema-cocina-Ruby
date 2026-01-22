package com.Service;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.DAO.Daos.UsuarioDAO;
import com.Model.ModeloUsuario;

import util.session.SessionUsuario;

public class UserService {
    private UsuarioDAO usuarioDAO= new UsuarioDAO();
    private SessionUsuario instanceSessionUsuario = SessionUsuario.getInstance();

    /**
     * Códigos de retorno:
     * 0 = Éxito, usuario autenticado
     * 1 = Campos vacíos o inválidos
     * 2 = Usuario o contraseña incorrectos
     * 3 = Error de base de datos
     */
    public int authenticate(ModeloUsuario usuario) {
        // Verificar si los campos están llenos
        if(!validateLoginData(usuario)){
            return 1; // Campos vacíos
        }
        // Validar usuario en base de datos
        try {
            if(validateUser(usuario)){
                return 0; // Éxito
            }else {
                return 2; // Usuario o contraseña incorrectos
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 3; // Error de base de datos
        }
    }

    public boolean isUserRegistered(ModeloUsuario usuario) {
        // Verify if this validation belongs here
        if(!validateLoginData(usuario)){
            JOptionPane.showMessageDialog(null, "Llena los campos");
            return false;
        }
        // This part goes
        try {
            return validateUser(usuario);
        } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Usuario no encontrado");
            return false;
        }
        
        
    }
    /**
     * Función para verificar los campos que introduce el usuario en el sistema
     * @param usuario Modelo de usuario
     * @return true si usuario y contraseña no están vacíos
     */
    private boolean validateLoginData(ModeloUsuario usuario){
    boolean result = usuario.getNombreUsuario()!= null && !usuario.getNombreUsuario().isBlank() 
    && usuario.getContrasenaUsuario() != null && !usuario.getContrasenaUsuario().isBlank();
    return result;
    }

    // Petición a la base de datos para verificar si el usuario es correcto
    private boolean validateUser(ModeloUsuario usuario) throws SQLException{
    if(usuarioDAO.autenticar(usuario.getNombreUsuario(), usuario.getContrasenaUsuario()) != null){
        this.instanceSessionUsuario.guardarNombreUsuario(usuario.getNombreUsuario());
        return true;
    }else {
        return false;
    }
    }
}
