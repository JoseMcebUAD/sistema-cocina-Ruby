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

public class MenuController implements Initializable {

    @FXML private BorderPane menuContainer;
    @FXML private ImageView exit;
    @FXML private Label menu;
    @FXML private AnchorPane slider;
    @FXML private StackPane content;
    @FXML private Button btnCorteCaja, ordersButton, salesButton, clientsButton, stopSalesButton;

    private double originalWidth;
    private Button currentActiveButton;
    private boolean stopSales = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setFontSize();
        setUpAllButtons();
        setUpSliderFunction();
        loadView("/com/view/order.fxml", ordersButton);
    }

    private void loadView(String fxml, Button sourceButton) {
    try {
        URL resource = getClass().getResource(fxml);
        if (resource == null) return;
        
        FXMLLoader loader = new FXMLLoader(resource);
        Parent view = loader.load();

            if (view instanceof javafx.scene.layout.Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            if (fxml.contains("order.fxml") && stopSales) {
                view.setDisable(true); 
                //Logica para el pop up por colocar
            }
            content.getChildren().setAll(view);
            updateButtonStyle(sourceButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStyle(Button activeBtn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("sidebar-item-active");
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-item-active");
            currentActiveButton = activeBtn;
        }
    }

    private void setUpSliderFunction() {
        originalWidth = slider.getPrefWidth();
        slider.setMinWidth(0);
    }

    private void setUpExitButton() {
        exit.fitWidthProperty().bind(menuContainer.widthProperty().multiply(0.025));
        exit.fitHeightProperty().bind(exit.fitWidthProperty());
        exit.setOnMouseEntered(e -> { exit.setScaleX(1.15); exit.setScaleY(1.15); });
        exit.setOnMouseExited(e -> { exit.setScaleX(1); exit.setScaleY(1); });
        exit.setOnMouseClicked(event -> System.exit(0));
    }

    public void setUpMenuFunction() {
        menu.setOnMouseClicked(e -> animateSlider(!slider.isVisible()));
    }

    private void setUpStopSalesButton(){
            stopSalesButton.setOnAction(e -> {
            stopSales = true;
            if (currentActiveButton == ordersButton) {
                loadView("/com/view/order.fxml", ordersButton);
            }
        });
    }
    private void setUpOrdersButton(){
        ordersButton.setOnAction(e -> loadView("/com/view/order.fxml", ordersButton));
    }
    
    private void setUpSalesButton(){
        salesButton.setOnAction(e -> loadView("/com/view/sales.fxml", salesButton));
    }

    private void setUpClientButton(){
        clientsButton.setOnAction(e -> loadView("/com/view/clients.fxml", clientsButton));
    }
    private void setUpAllButtons() {
        setUpExitButton();
        setUpMenuFunction();
        setUpOrdersButton();
        setUpSalesButton();
        setUpClientButton();
        setUpStopSalesButton();
    }

    private void animateSlider(boolean open) {
        Timeline timeline = new Timeline();
        double targetWidth = open ? originalWidth : 0;
        if (open) {
            slider.setVisible(true);
            slider.setManaged(true);
        }
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

    public void setFontSize() {
        menuContainer.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.heightProperty().addListener((o, oldH, newH) -> {
                    double size = newH.doubleValue() / 45; 
                    menuContainer.setStyle("-fx-font-size: " + size + "px;");
                });
            }
        });
    }
}
