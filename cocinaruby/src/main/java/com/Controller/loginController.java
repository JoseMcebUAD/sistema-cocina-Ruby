package com.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import com.Model.ModeloUsuario;
import com.Service.UsuarioService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

public class loginController implements Initializable{
    private UsuarioService usuarioService = new UsuarioService();

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Button btnIngresar;


    @FXML
    private void irAlMenu() {
            try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/view/bienvenida.fxml")
                );
        Parent root = loader.load();
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            }
            }

    @FXML
    private void eventAction(ActionEvent event){
        //Mejorar nombre de variable
        boolean esUsuarioRegistrado= ValidarUsuario(txtUsuario, txtContrasena);
        if(esUsuarioRegistrado){
            irAlMenu();
        }
        else{
            //Algun print coherente
        }
    }



    private void configurarCampos(){
        txtUsuario.setTextFormatter(new TextFormatter<>(change -> {
        if (change.getText().contains(" ")) {
            return null;
        }
        return change;
    }));

    txtContrasena.setTextFormatter(new TextFormatter<>(change -> {
        if (change.getText().contains(" ")) {
            return null;
        }
        return change;
    }));
    }

    private boolean ValidarUsuario(TextField txtUsuario, PasswordField txtContrasena){
        return usuarioService.EsUsuariorRegistrado(CrearUsuarioDesdeElForm(txtUsuario, txtContrasena));

    }

    private ModeloUsuario CrearUsuarioDesdeElForm(TextField txtUsuario, PasswordField txtContrasena){
        ModeloUsuario usuario = new ModeloUsuario();
        usuario.setNombreUsuario(txtUsuario.getText());
        usuario.setContrasenaUsuario(txtContrasena.getText());
        return usuario;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarCampos();
    }

    
}
