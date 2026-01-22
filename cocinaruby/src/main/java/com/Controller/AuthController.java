package com.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.Model.ModeloUsuario;
import com.Service.UserService;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.Model.Enum.AuthUIConstants;
import com.Model.Enum.AnimationConstants;

public class AuthController implements Initializable{
    //Verificar Metodos
    @FXML
    private Hyperlink footerLink;
    @FXML
    private Label footerText;
    @FXML
    private Button formButton;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUser;
    @FXML
    private Label subtitlelabel;
    @FXML
    private Label titlelabel;
    @FXML
    private ImageView exit;
    @FXML
    private StackPane authView;
    @FXML
    private BorderPane card;
    @FXML
    private Label errorPasswordLabel;
    @FXML
    private Label errorUserLabel;

    private boolean registerMode= false;
    private UserService userService = new UserService();
    
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (registerMode) {
            register();
        } else {
            login();
        }
    }
    

    @FXML
    private void openMenu(){
            try {
            FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/view/menu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setResizable(false);
            stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTextFieldConfig();
        setExitButtonConfig();
    }

    private void setTextFieldConfig(){
        txtUser.setTextFormatter(new TextFormatter<>(change -> {
        if (change.getText().contains(" ")) {
            return null;
        }
        return change;
    }));

    txtPassword.setTextFormatter(new TextFormatter<>(change -> {
        if (change.getText().contains(" ")) {
            return null;
        }
        return change;
    }));
    }

    private boolean userValidation(TextField txtUser, PasswordField txtPassword){
        return userService.isUserRegistered(createUser(txtUser, txtPassword));
    }

    private ModeloUsuario createUser(TextField txtUser, PasswordField txtPassword){
        ModeloUsuario usuario = new ModeloUsuario();
        usuario.setNombreUsuario(txtUser.getText());
        usuario.setContrasenaUsuario(txtPassword.getText());
        return usuario;
    }

    private void login() {
        // Clear previous errors
        clearErrorMessages();
        
        ModeloUsuario usuario = createUser(txtUser, txtPassword);
        int resultado = userService.authenticate(usuario);
        
        if (resultado == 0) {
            // Éxito
            openMenu();
        } else if (resultado == 1) {
            // Campos vacíos
            showErrorMessage(errorUserLabel, "Por favor completa todos los campos");
        } else if (resultado == 2) {
            // Usuario o contraseña incorrectos - mostrar en ambos
            showErrorMessage(errorUserLabel, "Usuario o contraseña incorrecto");
            showErrorMessage(errorPasswordLabel, "Usuario o contraseña incorrecto");
        } else {
            // Error de base de datos
            showErrorMessage(errorUserLabel, "Error en la autenticación, intenta más tarde");
            showErrorMessage(errorPasswordLabel, "Error en la autenticación, intenta más tarde");
        }
    }

    private void register() {
        //Aun nada
        System.out.println("REGISTER");
    }

    public void setExitButtonConfig(){
        exit.fitHeightProperty().bind(exit.fitWidthProperty());
        exit.setOnMouseEntered(e->{exit.setScaleX(1.15); exit.setScaleY(1.15);});
        exit.setOnMouseExited(e ->{exit.setScaleX(1); exit.setScaleY(1);});
        exit.setOnMouseClicked(event -> {System.exit(0);});
    }

    private void animateText(Labeled node, String newText) {
        FadeTransition fadeOut = new FadeTransition(
            AnimationConstants.FADE_DURATION_SHORT.getDuration(), node);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(
            AnimationConstants.FADE_DURATION_SHORT.getDuration(), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut.setOnFinished(e -> {
            node.setText(newText);
            fadeIn.play();
        });

        fadeOut.play();
    }
    private void showErrorMessage(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrorMessages() {
        errorUserLabel.setVisible(false);
        errorUserLabel.setManaged(false);
        errorPasswordLabel.setVisible(false);
        errorPasswordLabel.setManaged(false);
    }
    private void viewPassword(){
        //Aun nada
    }
    
}
