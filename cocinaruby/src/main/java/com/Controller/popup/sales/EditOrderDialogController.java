package com.Controller.popup.sales;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.Service.OrderService;
import com.Model.DTO.VIEW.ModeloVentasView;
import com.Model.ModeloDetalleOrden;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditOrderDialogController implements Initializable {

    @FXML
    private Label clientLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label originalPaymentLabel;
    @FXML
    private TextField pagoTextField;
    @FXML
    private TableView<ObservableList<String>> detailsTable;
    @FXML
    private Label generalTotalLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private OrderService orderService = new OrderService();
    private ModeloVentasView order;
    private Stage dialogStage;

    // ==================== UI Style Constants ====================
    private static final String CURRENCY_FORMAT = "$%.2f";
    private static final String STATUS_PRINTED = "Impreso";
    private static final String STATUS_NOT_PRINTED = "No Impreso";
    private static final String TYPE_CASH = "efectivo";
    private static final String TYPE_CLIENT_DOMICILIO = "Domicilio";
    private static final String TYPE_CLIENT_MESA = "Mesa";
    private static final String DEFAULT_CLIENT_NAME = "Cliente General";
    private static final String DEFAULT_SPEC = "N/A";

    // ==================== Messages ====================
    private static final String ALERT_SUCCESS_UPDATE = "Orden actualizada correctamente";
    private static final String ALERT_ERROR_TITLE = "Error";
    private static final String ALERT_SUCCESS_TITLE = "Éxito";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonHandlers();
    }

    private void setupButtonHandlers() {
        saveButton.setOnAction(e -> handleSaveOrderChanges());
        cancelButton.setOnAction(e -> dialogStage.close());
    }

    /**
     * Establece el stage del diálogo
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Carga los datos de la orden en el diálogo
     */
    public void setOrderData(ModeloVentasView order) {
        this.order = order;
        loadOrderInfo();
        loadOrderDetails();
    }

    /**
     * Carga la información general de la orden
     */
    private void loadOrderInfo() {
        clientLabel.setText(order.getNombreCliente() != null ? order.getNombreCliente() : DEFAULT_CLIENT_NAME);
        statusLabel.setText(order.isFacturado() ? STATUS_PRINTED : STATUS_NOT_PRINTED);
        originalPaymentLabel.setText(String.format(CURRENCY_FORMAT, order.getPagoCliente()));
        pagoTextField.setText(String.format("%.2f", order.getPagoCliente()));
    }

    /**
     * Carga los detalles de la orden en la tabla
     */
    private void loadOrderDetails() {
        configureOrderDetailsTable();
        ObservableList<ObservableList<String>> detailData = loadOrderDetailsData();
        detailsTable.setItems(detailData);
        updateGeneralTotal();

        // Si no es efectivo, configurar actualización automática de pago
        if (!isPaymentCash()) {
            setupAutoPaymentUpdate(detailData);
        }
    }

    /**
     * Configura las columnas de la tabla de detalles
     */
    private void configureOrderDetailsTable() {
        // Obtener las columnas
        TableColumn<ObservableList<String>, String> prodCol = (TableColumn<ObservableList<String>, String>) detailsTable.getColumns().get(0);
        TableColumn<ObservableList<String>, String> cantCol = (TableColumn<ObservableList<String>, String>) detailsTable.getColumns().get(1);
        TableColumn<ObservableList<String>, String> precioCol = (TableColumn<ObservableList<String>, String>) detailsTable.getColumns().get(2);
        TableColumn<ObservableList<String>, String> totalCol = (TableColumn<ObservableList<String>, String>) detailsTable.getColumns().get(3);

        boolean esEfectivo = isPaymentCash();

        // Configurar cell factories
        prodCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
            param.getValue().size() > 0 ? param.getValue().get(0) : ""));
        prodCol.setCellFactory(tc -> createEditableCell(0));

        cantCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
            param.getValue().size() > 1 ? param.getValue().get(1) : ""));
        cantCol.setCellFactory(tc -> createNumericEditableCell(1, 0, Integer.MAX_VALUE, esEfectivo));

        precioCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
            param.getValue().size() > 2 ? param.getValue().get(2) : ""));
        precioCol.setCellFactory(tc -> createPriceEditableCell(2, esEfectivo));

        totalCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(
            param.getValue().size() > 3 ? param.getValue().get(3) : ""));
    }

    /**
     * Carga los detalles de la orden en datos observables
     */
    private ObservableList<ObservableList<String>> loadOrderDetailsData() {
        ObservableList<ObservableList<String>> detailData = FXCollections.observableArrayList();

        if (order.getDetalleOrden() != null) {
            for (ModeloDetalleOrden detail : order.getDetalleOrden()) {
                double total = detail.getCantidad() * detail.getPrecioDetalleOrden();
                ObservableList<String> row = FXCollections.observableArrayList(
                    detail.getEspecificacionesDetalleOrden() != null ? detail.getEspecificacionesDetalleOrden() : DEFAULT_SPEC,
                    String.valueOf(detail.getCantidad()),
                    String.format(CURRENCY_FORMAT, detail.getPrecioDetalleOrden()),
                    String.format(CURRENCY_FORMAT, total)
                );
                detailData.add(row);
            }
        }
        return detailData;
    }

    /**
     * Verifica si el tipo de pago es efectivo
     */
    private boolean isPaymentCash() {
        String nombreTipoPago = order.getNombreTipoPago() != null ? order.getNombreTipoPago().toLowerCase() : "";
        return TYPE_CASH.equalsIgnoreCase(nombreTipoPago);
    }

    /**
     * Configura la actualización automática del pago cuando no es efectivo
     */
    private void setupAutoPaymentUpdate(ObservableList<ObservableList<String>> detailData) {
        double totalInicial = 0;
        for (ObservableList<String> row : detailData) {
            String totalStr = row.get(3);
            if (totalStr != null && totalStr.startsWith("$")) {
                totalStr = totalStr.substring(1);
            }
            try {
                totalInicial += Double.parseDouble(totalStr);
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }
        pagoTextField.setText(String.format("%.2f", totalInicial));
    }

    /**
     * Crea una celda editable básica para texto
     */
    private TableCell<ObservableList<String>, String> createEditableCell(int columnIndex) {
        return new EditableTextCell(columnIndex, detailsTable);
    }

    /**
     * Crea una celda editable para números enteros
     */
    private TableCell<ObservableList<String>, String> createNumericEditableCell(int columnIndex, int minValue, int maxValue, boolean esEfectivo) {
        return new EditableNumericCell(columnIndex, minValue, maxValue, detailsTable, generalTotalLabel, pagoTextField, esEfectivo);
    }

    /**
     * Crea una celda editable para precios
     */
    private TableCell<ObservableList<String>, String> createPriceEditableCell(int columnIndex, boolean esEfectivo) {
        return new EditablePriceCell(columnIndex, detailsTable, generalTotalLabel, pagoTextField, esEfectivo);
    }

    /**
     * Maneja el guardado de los cambios de la orden
     */
    private void handleSaveOrderChanges() {
        try {
            ObservableList<ObservableList<String>> detailData = detailsTable.getItems();
            double nuevoTotal = 0;
            List<ModeloDetalleOrden> detallesActualizados = new java.util.ArrayList<>();

            // Procesar cada detalle
            for (int i = 0; i < detailData.size(); i++) {
                ObservableList<String> row = detailData.get(i);
                ModeloDetalleOrden detalleOriginal = order.getDetalleOrden().get(i);

                int nuevaCantidad = Integer.parseInt(row.get(1));
                String precioStr = row.get(2);
                if (precioStr.startsWith("$")) {
                    precioStr = precioStr.substring(1);
                }
                double nuevoPrecio = Double.parseDouble(precioStr);

                detalleOriginal.setCantidad(nuevaCantidad);
                detalleOriginal.setPrecioDetalleOrden(nuevoPrecio);
                detallesActualizados.add(detalleOriginal);

                nuevoTotal += nuevaCantidad * nuevoPrecio;
            }

            // Crear orden actualizada
            com.Model.ModeloOrden ordenActualizada = createUpdatedOrder(nuevoTotal);

            // Validar pago
            if (ordenActualizada.getPagoCliente() < nuevoTotal) {
                showPaymentValidationError(nuevoTotal, ordenActualizada.getPagoCliente());
                return;
            }

            // Guardar cambios
            boolean detailsSaved = orderService.updateOrderDetails(order.getIdOrden(), detallesActualizados);
            boolean headerSaved = orderService.updateOrderHeader(ordenActualizada);

            if (headerSaved && detailsSaved) {
                showAlert(ALERT_SUCCESS_TITLE, ALERT_SUCCESS_UPDATE, Alert.AlertType.INFORMATION);
                dialogStage.close();
            } else {
                showAlert(ALERT_ERROR_TITLE, "No se pudo actualizar la orden", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            System.err.println("Error al guardar cambios: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(ALERT_ERROR_TITLE, "Error al guardar: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Crea una orden actualizada del tipo correcto basado en tipoCliente
     */
    private com.Model.ModeloOrden createUpdatedOrder(double nuevoTotal) {
        com.Model.ModeloOrden ordenActualizada;
        String tipoCliente = order.getTipoCliente();

        if (TYPE_CLIENT_DOMICILIO.equalsIgnoreCase(tipoCliente)) {
            ordenActualizada = new com.Model.Orden.ModeloOrdenDomicilio();
        } else if (TYPE_CLIENT_MESA.equalsIgnoreCase(tipoCliente)) {
            ordenActualizada = new com.Model.Orden.ModeloOrdenMesa();
        } else {
            ordenActualizada = new com.Model.Orden.ModeloOrdenMostrador();
        }

        ordenActualizada.setIdOrden(order.getIdOrden());
        ordenActualizada.setIdRelTipoPago(order.getIdRelTipoPago());
        ordenActualizada.setTipoCliente(order.getTipoCliente());
        ordenActualizada.setFechaExpedicionOrden(order.getFechaExpedicionOrden());
        ordenActualizada.setPrecioOrden(nuevoTotal);

        double nuevoPagoCliente;
        try {
            nuevoPagoCliente = Double.parseDouble(pagoTextField.getText());
        } catch (NumberFormatException ex) {
            nuevoPagoCliente = nuevoTotal;
        }

        ordenActualizada.setPagoCliente(nuevoPagoCliente);
        ordenActualizada.setFacturado(order.isFacturado());

        return ordenActualizada;
    }

    /**
     * Muestra un error de validación de pago
     */
    private void showPaymentValidationError(double total, double pago) {
        showAlert(ALERT_ERROR_TITLE,
            "El pago del cliente ($" + String.format("%.2f", pago) +
            ") no puede ser menor al total de la orden ($" + String.format("%.2f", total) + ")",
            Alert.AlertType.ERROR);
    }

    /**
     * Actualiza el total general de la tabla
     */
    private void updateGeneralTotal() {
        try {
            double sumaTotal = 0;
            for (ObservableList<String> row : detailsTable.getItems()) {
                String totalStr = row.get(3);
                if (totalStr != null && totalStr.startsWith("$")) {
                    totalStr = totalStr.substring(1);
                }
                sumaTotal += Double.parseDouble(totalStr);
            }
            generalTotalLabel.setText("Total General: " + String.format(CURRENCY_FORMAT, sumaTotal));
        } catch (Exception e) {
            System.err.println("Error calculando total general: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== Inner Classes for Editable Cells ====================

    /**
     * Celda editable básica para texto
     */
    private class EditableTextCell extends TableCell<ObservableList<String>, String> {
        protected TextField textField;
        protected int columnIndex;
        protected TableView<ObservableList<String>> table;

        public EditableTextCell(int columnIndex, TableView<ObservableList<String>> table) {
            this.columnIndex = columnIndex;
            this.table = table;
        }

        @Override
        public void startEdit() {
            super.startEdit();
            createTextField();
            textField.setText(getItem() != null ? getItem() : "");
            setGraphic(textField);
            setText(null);
            textField.requestFocus();
            textField.selectAll();
        }

        @Override
        public void commitEdit(String newValue) {
            if (newValue != null && !newValue.trim().isEmpty()) {
                super.commitEdit(newValue);
                table.getItems().get(getIndex()).set(columnIndex, newValue);
                cancelEdit();
            } else {
                cancelEdit();
            }
        }

        protected void createTextField() {
            if (textField == null) {
                textField = new TextField();
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                textField.setOnKeyPressed(e -> {
                    if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit(textField.getText());
                    }
                });
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(null);
            }
        }
    }

    /**
     * Celda editable para números enteros con validación
     */
    private class EditableNumericCell extends EditableTextCell {
        private int minValue;
        private int maxValue;
        private TextField pagoTextField;
        private boolean esEfectivo;

        public EditableNumericCell(int columnIndex, int minValue, int maxValue,
                TableView<ObservableList<String>> table,
                Label totalLabel,
                TextField pagoTextField,
                boolean esEfectivo) {
            super(columnIndex, table);
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.pagoTextField = pagoTextField;
            this.esEfectivo = esEfectivo;
        }

        @Override
        public void commitEdit(String newValue) {
            try {
                int valor = Integer.parseInt(newValue);
                if (valor >= minValue && valor <= maxValue) {
                    super.commitEdit(String.valueOf(valor));
                    table.getItems().get(getIndex()).set(columnIndex, String.valueOf(valor));
                    updateTotalRow(table.getItems().get(getIndex()));
                    updateGeneralTotal();

                    if (!esEfectivo) {
                        updateAutoPayment();
                    }
                    cancelEdit();
                } else {
                    showAlert("ERROR", "Valor fuera de rango", Alert.AlertType.ERROR);
                    cancelEdit();
                }
            } catch (NumberFormatException e) {
                showAlert("ERROR", "Ingrese un número válido", Alert.AlertType.ERROR);
                cancelEdit();
            }
        }

        private void updateAutoPayment() {
            double nuevoTotal = 0;
            for (ObservableList<String> row : table.getItems()) {
                String totalStr = row.get(3);
                if (totalStr != null && totalStr.startsWith("$")) {
                    totalStr = totalStr.substring(1);
                }
                try {
                    nuevoTotal += Double.parseDouble(totalStr);
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            pagoTextField.setText(String.format("%.2f", nuevoTotal));
        }
    }

    /**
     * Celda editable para precios con validación
     */
    private class EditablePriceCell extends EditableTextCell {
        private TextField pagoTextField;
        private boolean esEfectivo;

        public EditablePriceCell(int columnIndex,
                TableView<ObservableList<String>> table,
                Label totalLabel,
                TextField pagoTextField,
                boolean esEfectivo) {
            super(columnIndex, table);
            this.pagoTextField = pagoTextField;
            this.esEfectivo = esEfectivo;
        }

        @Override
        public void startEdit() {
            super.startEdit();
            String value = getItem();
            if (value != null && value.startsWith("$")) {
                value = value.substring(1);
            }
            textField.setText(value != null ? value : "");
        }

        @Override
        public void commitEdit(String newValue) {
            try {
                double valor = Double.parseDouble(newValue);
                if (valor > 0) {
                    String formatted = String.format(CURRENCY_FORMAT, valor);
                    super.commitEdit(formatted);
                    table.getItems().get(getIndex()).set(columnIndex, formatted);
                    updateTotalRow(table.getItems().get(getIndex()));
                    updateGeneralTotal();

                    if (!esEfectivo) {
                        updateAutoPayment();
                    }
                    cancelEdit();
                } else {
                    showAlert("ERROR", "El precio debe ser mayor a 0", Alert.AlertType.ERROR);
                    cancelEdit();
                }
            } catch (NumberFormatException e) {
                showAlert("ERROR", "Ingrese un número válido", Alert.AlertType.ERROR);
                cancelEdit();
            }
        }

        private void updateAutoPayment() {
            double nuevoTotal = 0;
            for (ObservableList<String> row : table.getItems()) {
                String totalStr = row.get(3);
                if (totalStr != null && totalStr.startsWith("$")) {
                    totalStr = totalStr.substring(1);
                }
                try {
                    nuevoTotal += Double.parseDouble(totalStr);
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            pagoTextField.setText(String.format("%.2f", nuevoTotal));
        }
    }

    /**
     * Actualiza la celda de total de una fila
     */
    private void updateTotalRow(ObservableList<String> row) {
        try {
            int cantidad = Integer.parseInt(row.get(1));
            String precioStr = row.get(2);
            if (precioStr.startsWith("$")) {
                precioStr = precioStr.substring(1);
            }
            double precio = Double.parseDouble(precioStr);
            double total = cantidad * precio;
            row.set(3, String.format("$%.2f", total));
        } catch (Exception e) {
            System.err.println("Error actualizando total: " + e.getMessage());
        }
    }
}
