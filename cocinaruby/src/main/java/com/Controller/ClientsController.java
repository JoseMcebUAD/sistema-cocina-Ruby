package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class ClientsController implements Initializable {
    @FXML
    private Button addButton, deleteButton, editButton, confirmButton ;
    @FXML
    private TextField nameField, phoneField, addressField;
    @FXML
    //Cambiar el tipo segun lo que se utilizara
    private TableView<?> clienteTable;
    @FXML
    private HBox confirmBar;

    private OrderController parentController;

    public void setParentController(OrderController parent) {
        this.parentController = parent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        insertData();
        setButtonConfig();
    }

    private void searchClients(){

    }

    private void insertData(){

    }

    private void getTextFieldInfo(){

    }

    private void setButtonConfig(){
        setConfirmButton();
    }

    public void showConfirmBar() {
        if (confirmBar != null) {
            confirmBar.setVisible(true);
            confirmBar.setManaged(true);
        }
    }
    //Activo solamente cuando se aprieta el boton seleccionar en ordenes
    private void setConfirmButton(){
        confirmButton.setOnAction(event -> {
            if (parentController != null) {
                // Enviamos lo que esté escrito en los campos o texto fijo de prueba
                String nombreTest = nameField.getText().isEmpty() ? "Cliente de Prueba" : nameField.getText();
                String dirTest = addressField.getText().isEmpty() ? "Calle Falsa 123" : addressField.getText();
                String telTest = phoneField.getText().isEmpty() ? "555-0123" : phoneField.getText();
                parentController.setClientData(nombreTest, dirTest, telTest);
            }
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }

        

    }

