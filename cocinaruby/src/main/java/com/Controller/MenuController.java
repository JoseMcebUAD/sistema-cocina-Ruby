package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class MenuController implements Initializable{

    @FXML
    private BorderPane menuContainer;

    @FXML
    private ImageView exit;

    @FXML
    private Label menu;

    @FXML
    private AnchorPane slider;

    private boolean menuOpen = false;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setFontSize();
        setExitButton();
        setMenuFunction();
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

    public void setMenuFunction(){
    slider.setTranslateX(-220);
        menu.setOnMouseClicked(event -> {
        
        menu.setScaleX(1.15);
        menu.setScaleY(1.15);
        
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
        if (!menuOpen) {
            slide.setToX(0);
            slide.setOnFinished(e -> menuOpen = true);
        } else {
            slide.setToX(-220);
            slide.setOnFinished(e -> menuOpen = false);
            menu.setScaleX(1);
            menu.setScaleY(1);
        }

        slide.play();
    });
    }

}
