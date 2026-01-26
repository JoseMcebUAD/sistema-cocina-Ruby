package com.Controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.Model.ModeloMesa;
import com.Model.Enum.EstadoMesaEnum;
import com.Service.MesaService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

public class TablesController extends BaseController {

    @FXML
    private BorderPane mainRoot;
    @FXML
    private Button addButton, changeStateButton, deleteButton, clearButton, showAllButton;
    @FXML
    private ComboBox<String> estadoComboBox, filtroEstadoComboBox;
    @FXML
    private TableView<ModeloMesa> mesaTable;
    @FXML
    private TableColumn<ModeloMesa, String> colNumero;
    @FXML
    private TableColumn<ModeloMesa, String> colEstado;
    @FXML
    private Label siguienteMesaLabel;

    private MesaService mesaService = new MesaService();
    private ObservableList<ModeloMesa> masterData = FXCollections.observableArrayList();
    private ModeloMesa selectedMesa = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    @Override
    protected void setupAdditionalConfig() {
        setupTableColumns();
        setupComboBoxes();
        loadDataFromService();
        updateSiguienteMesaLabel();
    }

    /**
     * Configura las columnas de la tabla.
     */
    private void setupTableColumns() {
        // Columna número usa un factory custom para mostrar "Mesa X"
        colNumero.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNumeroMesaDisplay())
        );
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoMesa"));

        // Setup row selection
        mesaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMesa = newSelection;
                estadoComboBox.setValue(newSelection.getEstadoMesa());
            }
        });
    }

    /**
     * Configura los ComboBoxes con los estados disponibles.
     */
    private void setupComboBoxes() {
        ObservableList<String> estados = FXCollections.observableArrayList(
            EstadoMesaEnum.DISPONIBLE,
            EstadoMesaEnum.OCUPADO,
            EstadoMesaEnum.SUSPENDIDO
        );
        estadoComboBox.setItems(estados);

        ObservableList<String> filtroEstados = FXCollections.observableArrayList(
            "Todos",
            EstadoMesaEnum.DISPONIBLE,
            EstadoMesaEnum.OCUPADO,
            EstadoMesaEnum.SUSPENDIDO
        );
        filtroEstadoComboBox.setItems(filtroEstados);
        filtroEstadoComboBox.setValue("Todos");

        // Listener para filtrar por estado
        filtroEstadoComboBox.setOnAction(e -> filtrarPorEstado());
    }

    /**
     * Carga los datos desde el servicio.
     */
    private void loadDataFromService() {
        try {
            List<ModeloMesa> mesas = mesaService.getAllMesas();
            masterData.setAll(mesas);
            mesaTable.setItems(masterData);
        } catch (Exception e) {
            showAlert("ERROR", "Error al cargar mesas", "No se pudieron cargar las mesas: " + e.getMessage());
        }
    }

    /**
     * Actualiza el label que muestra el siguiente número de mesa.
     */
    private void updateSiguienteMesaLabel() {
        try {
            String siguiente = mesaService.getSiguienteNumeroMesa();
            siguienteMesaLabel.setText("SIGUIENTE: " + siguiente);
        } catch (Exception e) {
            siguienteMesaLabel.setText("SIGUIENTE: Mesa 1");
        }
    }

    /**
     * Filtra las mesas por estado seleccionado.
     */
    private void filtrarPorEstado() {
        String filtro = filtroEstadoComboBox.getValue();
        try {
            if (filtro == null || filtro.equals("Todos")) {
                loadDataFromService();
            } else {
                List<ModeloMesa> mesas = mesaService.getMesasPorEstado(filtro);
                masterData.setAll(mesas);
            }
        } catch (Exception e) {
            showAlert("ERROR", "Error al filtrar", "No se pudo filtrar por estado: " + e.getMessage());
        }
    }

    @Override
    protected void setupAllButtons() {
        addButton.setOnAction(e -> handleAdd());
        changeStateButton.setOnAction(e -> handleChangeState());
        deleteButton.setOnAction(e -> handleDelete());
        clearButton.setOnAction(e -> handleClear());
        showAllButton.setOnAction(e -> {
            filtroEstadoComboBox.setValue("Todos");
            loadDataFromService();
        });
    }

    /**
     * Agrega una nueva mesa con estado DISPONIBLE.
     */
    private void handleAdd() {
        try {
            ModeloMesa nuevaMesa = mesaService.addMesa();
            showAlert("SUCCESS", "Mesa agregada", "Mesa " + nuevaMesa.getIdMesa() + " agregada exitosamente");
            loadDataFromService();
            updateSiguienteMesaLabel();
            handleClear();
        } catch (Exception e) {
            showAlert("ERROR", "Error al agregar mesa", "No se pudo agregar la mesa: " + e.getMessage());
        }
    }

    /**
     * Cambia el estado de la mesa seleccionada.
     */
    private void handleChangeState() {
        if (selectedMesa == null) {
            showAlert("WARNING", "Seleccionar mesa", "Por favor seleccione una mesa de la tabla");
            return;
        }

        String nuevoEstado = estadoComboBox.getValue();
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            showAlert("WARNING", "Seleccionar estado", "Por favor seleccione un nuevo estado");
            return;
        }

        try {
            boolean success = mesaService.cambiarEstado(selectedMesa.getIdMesa(), nuevoEstado);
            if (success) {
                showAlert("SUCCESS", "Estado actualizado", "Mesa " + selectedMesa.getIdMesa() + " ahora está " + nuevoEstado);
                loadDataFromService();
                handleClear();
            } else {
                showAlert("ERROR", "Error", "No se pudo cambiar el estado de la mesa");
            }
        } catch (Exception e) {
            showAlert("ERROR", "Error al cambiar estado", "No se pudo cambiar el estado: " + e.getMessage());
        }
    }

    /**
     * Elimina la mesa seleccionada si no tiene órdenes asociadas.
     */
    private void handleDelete() {
        if (selectedMesa == null) {
            showAlert("WARNING", "Seleccionar mesa", "Por favor seleccione una mesa de la tabla");
            return;
        }

        boolean confirmed = showConfirmation(
            "Confirmar eliminación",
            "¿Está seguro de eliminar la Mesa " + selectedMesa.getIdMesa() + "?\nEsta acción no se puede deshacer."
        );

        if (!confirmed) {
            return;
        }

        try {
            boolean success = mesaService.deleteMesa(selectedMesa.getIdMesa());
            if (success) {
                showAlert("SUCCESS", "Mesa eliminada", "Mesa " + selectedMesa.getIdMesa() + " eliminada exitosamente");
                loadDataFromService();
                updateSiguienteMesaLabel();
                handleClear();
            } else {
                showAlert("ERROR", "Error", "No se pudo eliminar la mesa");
            }
        } catch (Exception e) {
            showAlert("ERROR", "Error al eliminar", e.getMessage());
        }
    }

    /**
     * Muestra un diálogo de confirmación.
     */
    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Limpia la selección y los campos.
     */
    private void handleClear() {
        mesaTable.getSelectionModel().clearSelection();
        estadoComboBox.setValue(null);
        selectedMesa = null;
    }
}
