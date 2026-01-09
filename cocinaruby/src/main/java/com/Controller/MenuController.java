package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

//Verificar metodos
public class MenuController implements Initializable{

    @FXML
    private BorderPane menuContainer;
    @FXML
    private ImageView exit;
    @FXML
    private Label menu;
    @FXML
    private AnchorPane slider;
    @FXML
    private StackPane content;
    @FXML
    private Button btnResume;

    private double originalWidth;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setFontSize();
        setExitButton();
        setMenuFunction();
        setResumeButton();
        originalWidth = slider.getPrefWidth(); 
        slider.setMinWidth(0);
    }

    public void setFontSize(){
    menuContainer.sceneProperty().addListener((obs, oldScene, scene) -> {
        if (scene != null) {
            scene.heightProperty().addListener((o, oldH, newH) -> {
                double size = newH.doubleValue() / 45;
                menuContainer.setStyle("-fx-font-size: " + size + "px;");
            });
        }
    });
    }

    public void setExitButton(){
    //Sirve para escalar el boton a un tamaño correspondiente a la pantalla completa.
    exit.fitWidthProperty().bind(
        menuContainer.widthProperty().multiply(0.025)
    );

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

    public void setMenuFunction() {
        menu.setOnMouseClicked(e -> {
            animateSlider(!slider.isVisible());
        });
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();

            if (view instanceof javafx.scene.layout.Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }

            content.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setResumeButton() {
        btnResume.setOnAction(e -> {
            loadView("/com/view/resume.fxml");
        });
    }

    private void animateSlider(boolean open) {
    Timeline timeline = new Timeline();
    
    // Si abrimos, el destino es el ancho original. Si cerramos, es 0.
    double targetWidth = open ? originalWidth : 0;

    if (open) {
        slider.setVisible(true);
        slider.setManaged(true);
    }

    // Animamos de donde esté ahora hasta el destino
    KeyValue kv = new KeyValue(slider.prefWidthProperty(), targetWidth);
    KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
    
    timeline.getKeyFrames().add(kf);

    timeline.setOnFinished(e -> {
        if (!open) {
            slider.setVisible(false);
            slider.setManaged(false);
        }
    });

    timeline.play();
}
}
