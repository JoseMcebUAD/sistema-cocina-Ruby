package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OrderController implements Initializable {
    @FXML private BorderPane mainRoot;
    @FXML private VBox clientBar;
    @FXML private Button addProductButton, deliveryButton, dineinButton, 
    makeOrderButton, selectClientButton;
    @FXML private TextField addressField, clientNameField, phoneNumberField, priceField, 
    productField, quantityField;
    @FXML private Label grandTotalLabel;
    @FXML private TableView<?> productsTable;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpAllButtons();
        setUpValidations();
        showClientBar(false);
        selectOrderType(dineinButton);
    }

    private void setUpAllButtons() {
        setUpDeliveryButton();
        setUpDineinButton();
        setUpselectClientButton();
        setUpMakeOrderButton();
    }

    private void setUpDeliveryButton(){
            deliveryButton.setOnAction(e -> {
            showClientBar(true);
            selectOrderType(deliveryButton);
        });
    }

    private void setUpDineinButton(){
            dineinButton.setOnAction(e -> {
            selectOrderType(dineinButton);
        });
    }

    private void setUpselectClientButton(){
        selectClientButton.setOnAction(e -> openClientsView());
    }

    private void setUpMakeOrderButton(){
        makeOrderButton.setOnAction(e-> openConfirmationView());
    }


    private void showClientBar(boolean show) {
        clientBar.setVisible(show);
        clientBar.setManaged(show);
        
        if (show) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), clientBar);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void setUpValidations(){

    }

    private void selectOrderType(Button selected) {
        toggleGroupStyle(selected, dineinButton, deliveryButton); 
    }

    private void toggleGroupStyle(Button activeBtn, Button... group) {
        for (Button btn : group) {
            btn.getStyleClass().removeAll("order-button", "order-button-active");
            btn.getStyleClass().add(btn == activeBtn ? "order-button-active" : "order-button");
        }
    }

    private void openClientsView() {
        showView("/com/view/clients.fxml", "Seleccionar Cliente", 1000, 600);
    }

    private void openConfirmationView() {
        showModal("/com/view/pop-up/order/confirmOrder.fxml", "Detalles de Pago", 539, 481);
    }


    private void showModal(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.TRANSPARENT, true);
    }

    private void showView(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.DECORATED, true);
    }

    private void createStage(String fxmlPath, String title, double width, double height, javafx.stage.StageStyle style, boolean isModal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (fxmlPath.contains("clients")) {
                ClientsController controller = loader.getController();
                controller.setParentController(this);
                controller.showConfirmBar();
            }
            Stage stage = new Stage();
            if (style == javafx.stage.StageStyle.TRANSPARENT) {
                stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                Scene scene = new Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                });
            } else {
                stage.initStyle(style);
                stage.setScene(new Scene(root));
            }
            if (isModal) {
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(mainRoot.getScene().getWindow());
            }
            stage.setTitle(title);
            stage.setWidth(width); 
            stage.setHeight(height);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClientData(String name, String address, String phoneNumber) {
        clientNameField.setText(name);
        addressField.setText(address);
        phoneNumberField.setText(phoneNumber);
    }
}