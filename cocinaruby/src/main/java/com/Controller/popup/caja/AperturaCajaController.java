package com.Controller.popup.caja;

import com.Model.ModeloAperturaCaja;
import com.Service.CajaService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AperturaCajaController implements Initializable {

    @FXML private TextField montoInicialField;
    @FXML private Button abrirButton;
    @FXML private Label errorLabel;

    private final CajaService cajaService = new CajaService();
    private Consumer<ModeloAperturaCaja> onAperturaCreada;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupValidacion();
        setupAbrirButton();
    }

    /**
     * Callback que el MenuController pasa para recibir la apertura creada.
     */
    public void setOnAperturaCreada(Consumer<ModeloAperturaCaja> callback) {
        this.onAperturaCreada = callback;
    }

    /**
     * Registra el Stage y bloquea su cierre manual (botón X).
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            event.consume();
            mostrarError("Debe registrar el monto inicial para continuar.");
        });
    }

    private void setupValidacion() {
        montoInicialField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                montoInicialField.setText(oldVal);
            }
        });
    }

    private void setupAbrirButton() {
        abrirButton.setOnAction(e -> {
            String texto = montoInicialField.getText().trim();
            if (texto.isEmpty()) {
                mostrarError("Ingrese el monto inicial.");
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(texto);
            } catch (NumberFormatException ex) {
                mostrarError("Formato de monto inválido.");
                return;
            }

            if (monto < 0) {
                mostrarError("El monto no puede ser negativo.");
                return;
            }

            // Usuario 1 por defecto (se puede extender con sesión de usuario)
            if (cajaService.getUltimaAperturaHoy() != null) {
                mostrarError("La caja ya fue abierta hoy.");
                return;
            }

            ModeloAperturaCaja apertura = cajaService.abrirCaja(monto, 1);
            if (apertura == null) {
                mostrarError("Error al registrar la apertura. Verifique la conexión.");
                return;
            }

            if (onAperturaCreada != null) {
                onAperturaCreada.accept(apertura);
            }

            ((Stage) abrirButton.getScene().getWindow()).close();
        });
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
