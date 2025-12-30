package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class loginController implements Initializable{
    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnIngresar;


    @FXML
    private void eventKey(KeyEvent event){
        Object evt= event.getSource();

        if(evt.equals(txtUsuario)){
            if(event.getCharacter().contains(" ")){
                event.consume();
            }
        }else if(evt.equals(txtPassword)){
        if(event.getCharacter().contains(" ")){
                event.consume();
            }
        }
    }

    @FXML
    private void eventAction(ActionEvent event){

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    
}
