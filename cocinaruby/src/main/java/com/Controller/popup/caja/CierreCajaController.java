package com.Controller.popup.caja;

import com.Model.ModeloCierreCaja;
import com.Service.CajaService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CierreCajaController implements Initializable {

    // Billetes
    @FXML private TextField b1000, b500, b200, b100, b50, b20;
    // Subtotales billetes
    @FXML private Label sub1000, sub500, sub200, sub100, sub50, sub20b;
    // Monedas
    @FXML private TextField m20, m10, m5, m2, m1;
    // Subtotales monedas
    @FXML private Label sub20m, sub10, sub5, sub2, sub1;

    @FXML private Label totalContadoLabel;
    @FXML private VBox observacionesBox;
    @FXML private TextArea observacionesArea;
    @FXML private Button confirmarButton, cancelarButton;

    private final CajaService cajaService = new CajaService();
    private int idApertura;
    private Consumer<ModeloCierreCaja> onCierreSaved;

    // Denominaciones: campo → valor unitario → label subtotal
    private record DenomEntry(TextField campo, double valor, Label subtotalLabel) {}
    private java.util.List<DenomEntry> denominaciones;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Se inicializa en setDatos()
    }

    public void setDatos(int idApertura, Consumer<ModeloCierreCaja> onCierreSaved) {
        this.idApertura = idApertura;
        this.onCierreSaved = onCierreSaved;
        buildDenominaciones();
        setupListeners();
        setupBotones();
    }

    private void buildDenominaciones() {
        denominaciones = new java.util.ArrayList<>();
        denominaciones.add(new DenomEntry(b1000, 1000.0, sub1000));
        denominaciones.add(new DenomEntry(b500,   500.0, sub500));
        denominaciones.add(new DenomEntry(b200,   200.0, sub200));
        denominaciones.add(new DenomEntry(b100,   100.0, sub100));
        denominaciones.add(new DenomEntry(b50,     50.0, sub50));
        denominaciones.add(new DenomEntry(b20,     20.0, sub20b));
        denominaciones.add(new DenomEntry(m20,     20.0, sub20m));
        denominaciones.add(new DenomEntry(m10,     10.0, sub10));
        denominaciones.add(new DenomEntry(m5,       5.0, sub5));
        denominaciones.add(new DenomEntry(m2,       2.0, sub2));
        denominaciones.add(new DenomEntry(m1,       1.0, sub1));
    }

    private void setupListeners() {
        for (DenomEntry d : denominaciones) {
            d.campo().textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    d.campo().setText(oldVal);
                    return;
                }
                actualizarSubtotal(d);
                recalcularTotal();
            });
        }
    }

    private void actualizarSubtotal(DenomEntry d) {
        int cantidad = parseCantidad(d.campo().getText());
        double subtotal = cantidad * d.valor();
        d.subtotalLabel().setText(String.format("= $%.2f", subtotal));
    }

    private void recalcularTotal() {
        double total = 0;
        for (DenomEntry d : denominaciones) {
            total += parseCantidad(d.campo().getText()) * d.valor();
        }
        totalContadoLabel.setText(String.format("$%.2f", total));
    }

    private int parseCantidad(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        try { return Integer.parseInt(texto); } catch (NumberFormatException e) { return 0; }
    }

    private double getMontoReal() {
        double total = 0;
        for (DenomEntry d : denominaciones) {
            total += parseCantidad(d.campo().getText()) * d.valor();
        }
        return total;
    }

    private void setupBotones() {
        cancelarButton.setOnAction(e -> ((Stage) cancelarButton.getScene().getWindow()).close());

        confirmarButton.setOnAction(e -> {
            double montoReal = getMontoReal();
            double montoEsperado = cajaService.calcularMontoEsperado(idApertura);
            double diferencia = montoReal - montoEsperado;

            // Si hay diferencia, mostrar y exigir observaciones
            if (Math.abs(diferencia) > 0.001) {
                observacionesBox.setVisible(true);
                observacionesBox.setManaged(true);

                String obs = observacionesArea.getText().trim();
                String mensaje = diferencia > 0 ? "Hay más dinero en caja que en las ventas del día, con $" +String.format("%.2f", Math.abs(diferencia))  + " de diferencia"   : "Hay más dinero registrado en el sistema que dinero en la caja, con $" + String.format("%.2f", Math.abs(diferencia))  + " de diferencia" ;
                if (obs.isEmpty()) {
                    mostrarAlerta("WARNING", "Observaciones obligatorias",
                        mensaje + 
                        ". Debe ingresar una justificación antes de confirmar.");
                    observacionesArea.requestFocus();
                    return;
                }
            }

            ModeloCierreCaja cierre = new ModeloCierreCaja();
            cierre.setIdRelApertura(idApertura);
            cierre.setFechaCierre(LocalDateTime.now());
            cierre.setMontoEsperado(montoEsperado);
            cierre.setMontoReal(montoReal);
            cierre.setDiferencia(diferencia);
            cierre.setObservaciones(observacionesArea.getText().trim().isEmpty() ? null : observacionesArea.getText().trim());

            if (!cajaService.cerrarCaja(cierre)) {
                mostrarAlerta("ERROR", "Error", "No se pudo guardar el cierre. Verifique la conexión.");
                return;
            }

            mostrarAlerta("SUCCESS", "Cierre de Caja",
                "El dinero en caja es de: $" + String.format("%.2f", montoReal));

            if (onCierreSaved != null) {
                onCierreSaved.accept(cierre);
            }

            ((Stage) confirmarButton.getScene().getWindow()).close();
        });
    }

    private void mostrarAlerta(String tipo, String titulo, String contenido) {
        Alert.AlertType alertType = switch (tipo.toUpperCase()) {
            case "ERROR" -> Alert.AlertType.ERROR;
            case "WARNING" -> Alert.AlertType.WARNING;
            default -> Alert.AlertType.INFORMATION;
        };
        Alert alert = new Alert(alertType);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
