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

    private boolean registerMode= false;
    
    private UserService userService = new UserService();

    @FXML
    private void handleHyperlink() {

        animateText(titlelabel,
                registerMode ? "Iniciar Sesión" : "Crear Usuario");

        animateText(subtitlelabel,
                registerMode ? "Accede con tu usuario" : "Crea un nuevo usuario");

        animateText(formButton,
                registerMode ? "Entrar" : "Crear");

        footerText.setText(
                registerMode ? "¿No tienes usuario?" : "¿Ya tienes un usuario?"
        );

        footerLink.setText(
                registerMode ? "Crea un usuario" : "Inicia sesión"
        );

        registerMode = !registerMode;
    }


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
        return userService.EsUsuariorRegistrado(createUser(txtUser, txtPassword));
    }

    private ModeloUsuario createUser(TextField txtUser, PasswordField txtPassword){
        ModeloUsuario usuario = new ModeloUsuario();
        usuario.setNombreUsuario(txtUser.getText());
        usuario.setContrasenaUsuario(txtPassword.getText());
        return usuario;
    }

    private void login() {
        boolean isUser= userValidation(txtUser,txtPassword);
        if (isUser) {
        openMenu();
        }else{
            //Aun nada
        }
    }

    private void register() {
        //Aun nada
        System.out.println("REGISTER");
    }

    public void setExitButtonConfig(){
    exit.fitHeightProperty().bind(
        exit.fitWidthProperty()
    );
    //Sirve para hacer el boton mas grande cuando el mouse pasa encima.
    exit.setOnMouseEntered(e->{
        exit.setScaleX(1.15);
        exit.setScaleY(1.15);
    });

    exit.setOnMouseExited(e ->{
        exit.setScaleX(1);
        exit.setScaleY(1);
    });
    //Provee la funcionalidad al boton de cerrar la aplicacion.
    exit.setOnMouseClicked(event -> {
            System.exit(0);
        });
    }

    private void animateText(Labeled node, String newText) {
        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(150), node);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(
            Duration.millis(150), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut.setOnFinished(e -> {
            node.setText(newText);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void viewPassword(){
        //Aun nada
    }
    
}
