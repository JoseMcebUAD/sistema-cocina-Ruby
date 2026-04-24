package com.Controller.popup.caja;

import com.Service.CajaService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RetiroCajaController implements Initializable {

    @FXML private TextField montoRetiroField;
    @FXML private TextArea razonRetiroArea;
    @FXML private Button registrarButton;
    @FXML private Button cancelarButton;
    @FXML private Label errorLabel;

    private final CajaService cajaService = new CajaService();
    private int idApertura;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupValidacion();
        setupBotones();
    }

    public void setIdApertura(int idApertura) {
        this.idApertura = idApertura;
    }

    private void setupValidacion() {
        montoRetiroField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                montoRetiroField.setText(oldVal);
            }
        });
    }

    private void setupBotones() {
        cancelarButton.setOnAction(e -> cerrarVentana());

        registrarButton.setOnAction(e -> {
            String montoTexto = montoRetiroField.getText().trim();
            String razon = razonRetiroArea.getText().trim();

            if (montoTexto.isEmpty()) {
                mostrarError("Ingrese el monto a retirar.");
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoTexto);
            } catch (NumberFormatException ex) {
                mostrarError("Formato de monto inválido.");
                return;
            }

            if (monto <= 0) {
                mostrarError("El monto debe ser mayor a cero.");
                return;
            }

            if (razon.isEmpty()) {
                mostrarError("Ingrese la razón del retiro.");
                return;
            }

            if (!cajaService.registrarRetiro(idApertura, monto, razon)) {
                mostrarError("No hay dinero suficiente en caja.");
                return;
            }

            cerrarVentana();
        });
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void cerrarVentana() {
        ((Stage) cancelarButton.getScene().getWindow()).close();
    }
}
