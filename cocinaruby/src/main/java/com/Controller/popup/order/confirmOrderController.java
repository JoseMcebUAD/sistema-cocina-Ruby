package com.Controller.popup.order;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class confirmOrderController implements Initializable {
    @FXML
    private Button backButton, confirmButton;

    @FXML
    private TextField clientNameField, orderTypeField, totalField, amountPaidField;

    @FXML
    private ComboBox<String> paymentMethodCombo;

    @FXML
    private HBox cashDetailsBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpAllButtons();
        setUpPaymentConfig();
        setUpAmountValidation();
    }

    /**
     * Configura el ComboBox con los 3 métodos de pago y la lógica de visibilidad.
     */

        private void setUpPaymentConfig() {
        paymentMethodCombo.getItems().addAll("Efectivo", "Tarjeta", "Transferencia");
        paymentMethodCombo.setPromptText("Seleccionar...");

        // Definimos cómo se verá cada fila de la lista desplegable
        paymentMethodCombo.setCellFactory(lv -> createCustomCell());

        // Definimos cómo se verá la opción seleccionada (la que queda fija arriba)
        paymentMethodCombo.setButtonCell(createCustomCell());

        // Tu lógica anterior del HBox de efectivo se mantiene igual
        paymentMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCash = "Efectivo".equals(newVal);
            cashDetailsBox.setVisible(isCash);
            cashDetailsBox.setManaged(isCash);
            if (!isCash) amountPaidField.clear();
        });
    }

    // Método auxiliar para crear la celda con imagen
private ListCell<String> createCustomCell() {
    return new ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                try {
                    // Cargamos la imagen según el texto de la opción
                    String path = "/com/images/" + item.toLowerCase() + ".png";
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(path)));
                    icon.setFitHeight(20); // Ajusta el tamaño de la imagen
                    icon.setFitWidth(20);
                    setGraphic(icon);
                } catch (Exception e) {
                    setGraphic(null); // Si no encuentra la imagen, solo muestra texto
                }
            }
        }
    };
}
    /**
     * Valida que el campo de pago en efectivo solo acepte números y un punto decimal.
     */
    private void setUpAmountValidation() {
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                amountPaidField.setText(oldValue);
            }
        });
    }
    
    private void setUpAllButtons() {
        setUpBackButton();
        setUpConfirmButton();
    }

    private void setUpBackButton() {
        backButton.setOnAction(e -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });
    }

    private void setUpConfirmButton() {
        confirmButton.setOnAction(e -> {
            // Lógica para procesar la confirmación del pedido
            String metodo = paymentMethodCombo.getValue();
            if (metodo == null || metodo.equals("Seleccionar...")) {
                // Aquí podrías añadir una alerta si no se ha seleccionado método
                System.out.println("Debe seleccionar un método de pago.");
                return;
            }

            System.out.println("Pedido confirmado con método: " + metodo);
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }
}