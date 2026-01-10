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
    @FXML private Button addProductButton, cardButton, cashButton, deliveryButton, dineinButton, 
    makeOrderButton, selectClientButton, transferButton;
    @FXML private TextField addressField, clientNameField, phoneNumberField, priceField, 
    productField, quantityField;
    @FXML private Label grandTotalLabel;
    @FXML private TableView<?> productsTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpAllButtons();
        // Configuraciones iniciales
        showClientBar(false);
        selectOrderType(dineinButton);
        selectPaymentMethod(cashButton);
    }

    private void setUpAllButtons() {
        deliveryButton.setOnAction(e -> {
            showClientBar(true);
            selectOrderType(deliveryButton);
        });
        
        dineinButton.setOnAction(e -> {
            showClientBar(false);
            selectOrderType(dineinButton);
        });

        selectClientButton.setOnAction(e -> openClientsView());
        cashButton.setOnAction(e -> selectPaymentMethod(cashButton));
        cardButton.setOnAction(e -> selectPaymentMethod(cardButton));
        transferButton.setOnAction(e -> selectPaymentMethod(transferButton));
        
        makeOrderButton.setOnAction(e -> {
            if (cashButton.getStyleClass().contains("order-button-active")) {
                openCashPopup();
            } else {
                openConfirmationView();
            }
        });
    }

    private void showClientBar(boolean show) {
        // Al usar managed(false), el BorderPane ignora este elemento para el cálculo del centro
        clientBar.setVisible(show);
        clientBar.setManaged(show);
        
        if (show) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), clientBar);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void selectOrderType(Button selected) {
        toggleGroupStyle(selected, dineinButton, deliveryButton); 
    }

    private void selectPaymentMethod(Button selected) {
        toggleGroupStyle(selected, cashButton, cardButton, transferButton);
    }

    private void toggleGroupStyle(Button activeBtn, Button... group) {
        for (Button btn : group) {
            btn.getStyleClass().removeAll("order-button", "order-button-active");
            btn.getStyleClass().add(btn == activeBtn ? "order-button-active" : "order-button");
        }
    }

    private void openClientsView() {
        showModal("/com/view/clients.fxml", "Seleccionar Cliente", 1000, 600);
    }

    private void openCashPopup() {
        showModal("/com/view/cashDetails.fxml", "Detalles de Pago", 500, 400);
    }

    private void openConfirmationView() {
        showModal("/com/view/confirmOrder.fxml", "Confirmar Pedido", 600, 450);
    }

    private void showModal(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (fxmlPath.contains("clients")) {
                ClientsController controller = loader.getController();
                controller.setParentController(this);
                controller.showConfirmBar();
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(mainRoot.getScene().getWindow()); 
            stage.setScene(new Scene(root));
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