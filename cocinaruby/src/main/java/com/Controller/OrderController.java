package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrderController implements Initializable {
    @FXML
    private BorderPane mainRoot;
    @FXML
    private VBox clientBar;
    @FXML
    private VBox topPanel;
    @FXML
    private HBox orderCenter;
    @FXML
    private Button addProductButton;
    @FXML
    private Button cardButton;
    @FXML
    private Button cashButton;
    @FXML
    private Button deliveryButton;
    @FXML
    private Button dineinButton;
    @FXML
    private Button makeOrderButton;
    @FXML
    private Button selectClientButton;
    @FXML
    private Button transferButton;
    @FXML
    private TextField addressField;
    @FXML
    private TextField clientNameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField productField;
    @FXML
    private TextField quantityField;
    @FXML
    private Label grandTotalLabel;
    @FXML
    private TableView<?> productsTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupViewConfig();
        setUpAllButtons();
    }

    private void setupViewConfig(){

    }

    private void setUpAllButtons(){
        setUpDineInButton();
        setUpDeliveryButton();
        setUpSelectClientButton();
    }
    private void setUpDeliveryButton(){
        deliveryButton.setOnAction(e -> showClientBar(true));
    }
    
    private void setUpDineInButton(){
        dineinButton.setOnAction(e -> showClientBar(false));
    }

    private void setUpSelectClientButton(){
        selectClientButton.setOnAction(e -> openClientsView());
    }
    
    private void openClientsView(){
        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/view/clients.fxml")); 
        Parent root = loader.load();
            ClientsController controller = loader.getController();
            controller.setParentController(this);
            controller.showConfirmBar();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(mainRoot.getScene().getWindow()); 
                stage.setScene(new Scene(root));
                stage.setWidth(1000); 
                stage.setHeight(600);
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
        showClientBar(true);
    }

    private void showClientBar(boolean show) {
        double startOpacity = show ? 0.0 : 1.0;
        double endOpacity = show ? 1.0 : 0.0;
        if (show) {
            clientBar.setVisible(true);
            clientBar.setManaged(true);
            clientBar.setOpacity(0); 
        }
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
        javafx.util.Duration.millis(300), clientBar
        );
        fade.setFromValue(startOpacity);
        fade.setToValue(endOpacity);
        fade.setOnFinished(e -> {
            if (!show) {
                clientBar.setVisible(false);
                clientBar.setManaged(false);
            }
        });
        fade.play();
    }
    
}
