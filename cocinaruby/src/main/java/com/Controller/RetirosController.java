package com.Controller;

import com.Controller.popup.caja.RetiroCajaController;
import com.Model.ModeloAperturaCaja;
import com.Model.ModeloCierreCaja;
import com.Model.ModeloRetiroCaja;
import com.Service.CajaService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RetirosController extends BaseController {

    @FXML private Button esteMesButton;
    @FXML private Button todoElAnioButton;
    @FXML private Button agregarRetiroButton;
    @FXML private ComboBox<Integer> anioComboBox;

    @FXML private TableView<ModeloRetiroCaja> retirosTable;
    @FXML private TableColumn<ModeloRetiroCaja, String> fechaCol;
    @FXML private TableColumn<ModeloRetiroCaja, String> horaCol;
    @FXML private TableColumn<ModeloRetiroCaja, String> montoCol;
    @FXML private TableColumn<ModeloRetiroCaja, String> razonCol;
    @FXML private Label totalLabel;

    private final CajaService cajaService = new CajaService();
    private final ObservableList<ModeloRetiroCaja> retirosList = FXCollections.observableArrayList();

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    private enum Filtro { MES, ANIO }
    private Filtro filtroActual = Filtro.MES;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    @Override
    protected void setupTableConfig() {
        fechaCol.setCellValueFactory(cell -> {
            var fecha = cell.getValue().getFechaRetiro();
            return new SimpleStringProperty(fecha != null ? fecha.format(FECHA_FMT) : "");
        });
        horaCol.setCellValueFactory(cell -> {
            var fecha = cell.getValue().getFechaRetiro();
            return new SimpleStringProperty(fecha != null ? fecha.format(HORA_FMT) : "");
        });
        montoCol.setCellValueFactory(cell ->
            new SimpleStringProperty("$" + String.format("%.2f", cell.getValue().getMontoRetirado())));
        razonCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getRazonRetiro()));
        retirosTable.setItems(retirosList);
    }

    @Override
    protected void setupAllButtons() {
        esteMesButton.setOnAction(e -> {
            filtroActual = Filtro.MES;
            cargarRetirosMesActual();
            toggleFiltroStyle(esteMesButton);
        });

        todoElAnioButton.setOnAction(e -> {
            filtroActual = Filtro.ANIO;
            Integer anio = anioComboBox.getValue();
            if (anio != null) cargarRetirosPorAnio(anio);
            toggleFiltroStyle(todoElAnioButton);
        });

        agregarRetiroButton.setOnAction(e -> abrirPopupAgregarRetiro());
    }

    @Override
    protected void setupAdditionalConfig() {
        cargarAniosDisponibles();
        cargarRetirosMesActual();
        toggleFiltroStyle(esteMesButton);
    }

    private void cargarAniosDisponibles() {
        List<Integer> anios = cajaService.getAniosConRetiros();
        int anioActual = LocalDate.now().getYear();
        if (!anios.contains(anioActual)) {
            anios.add(0, anioActual);
        }
        anioComboBox.setItems(FXCollections.observableArrayList(anios));
        anioComboBox.setValue(anioActual);

        anioComboBox.setOnAction(e -> {
            if (filtroActual == Filtro.ANIO) {
                Integer anio = anioComboBox.getValue();
                if (anio != null) cargarRetirosPorAnio(anio);
            }
        });
    }

    private void cargarRetirosMesActual() {
        LocalDate hoy = LocalDate.now();
        actualizarTabla(cajaService.getRetirosPorMesAnio(hoy.getMonthValue(), hoy.getYear()));
    }

    private void cargarRetirosPorAnio(int anio) {
        actualizarTabla(cajaService.getRetirosPorAnio(anio));
    }

    private void actualizarTabla(List<ModeloRetiroCaja> retiros) {
        retirosList.setAll(retiros);
        double total = retiros.stream().mapToDouble(ModeloRetiroCaja::getMontoRetirado).sum();
        totalLabel.setText("$" + String.format("%.2f", total));
    }

    private void abrirPopupAgregarRetiro() {
        ModeloAperturaCaja apertura = cajaService.getAperturaActivaHoy();
        if (apertura == null) {
            showAlert("Sin apertura activa", "No hay una caja abierta. Abra la caja primero desde el menú.");
            return;
        }
        ModeloCierreCaja cierre = cajaService.getCierrePorApertura(apertura.getIdApertura());
        if (cierre != null) {
            showAlert("Caja cerrada", "La caja ya está cerrada. No se pueden registrar retiros.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/view/pop-up/caja/retiroCaja.fxml"));
            Parent root = loader.load();

            RetiroCajaController ctrl = loader.getController();
            ctrl.setIdApertura(apertura.getIdApertura());

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Agregar Retiro de Caja");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refrescar tabla y años disponibles al cerrar el popup
            cargarAniosDisponibles();
            if (filtroActual == Filtro.MES) {
                cargarRetirosMesActual();
            } else {
                Integer anio = anioComboBox.getValue();
                if (anio != null) cargarRetirosPorAnio(anio);
            }
        } catch (Exception e) {
            showAlert("ERROR", "Error", "Error al abrir el formulario de retiro: " + e.getMessage());
        }
    }

    private void toggleFiltroStyle(Button activeBtn) {
        for (Button btn : new Button[]{esteMesButton, todoElAnioButton}) {
            btn.getStyleClass().removeAll("order-button", "order-button-active");
            btn.getStyleClass().add(btn == activeBtn ? "order-button-active" : "order-button");
        }
    }
}
