package com.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import com.Service.OrderService;
import com.Service.TicketServices.TicketOrderService;
import com.Service.TicketServices.TicketOrderService.PrintResultEnum;
import com.Model.DTO.VIEW.ModeloVentasView;
import com.Model.ModeloDetalleOrden;
import com.Model.DTO.ModeloRecibo;
import com.Model.DTO.ModeloOrdenCompleta;
import com.Model.Orden.ModeloOrdenMostrador;
import com.Model.ModeloOrden;
import com.Model.Enum.SalesStyleConstants;
import com.Model.Enum.SalesFormatConstants;
import com.Model.Enum.SalesUILabels;
import com.Model.Enum.SalesMessages;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.Controller.popup.sales.EditOrderDialogController;
import com.Model.ModeloTipoPago;

public class SalesController extends BaseController {
    @FXML 
    private Button todaySalesButton, clearButton, statsButton;
    @FXML
    private MenuButton searchButton;
    @FXML
    private MenuItem dateSearchButton,clientTypeButton,clientNameButton, PayTypeButton;
    @FXML 
    private HBox dateBar, searchBar, grandTotalBar;
    @FXML 
    private DatePicker startDate, endDate;
    @FXML 
    private Label detailLabel, totalLabel, templateLabel;
    @FXML
    private TextField searchField;
    @FXML 
    private TableView<ObservableList<String>> OrderTable;
    @FXML 
    private TableView<ObservableList<String>> orderDetailTable;
    @FXML
    StackPane TableBar;
    @FXML
    private HBox fieldBar;
    @FXML 
    private BorderPane mainRoot;

    private OrderService orderService = new OrderService();
    private TicketOrderService ticketService = new TicketOrderService();
    private Map<Integer, Integer> orderIdMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    /**
     * Configura la lógica adicional de visibilidad y carga de datos.
     */
    @Override
    protected void setupAdditionalConfig() {
        setupVisibilityLogic();
        setupSelectionLogic();
        loadOrdersFromService();
        setupSearchDates(false);
        setupGlobalClickConfig();
        todaySalesButton.getStyleClass().add("tab-active");
    }

    // =============== CONFIGURACIÓN VISUAL ===============
    /**
     * Configura la lógica de visibilidad de las barras de búsqueda.
     */
    private void setupVisibilityLogic() {
        dateSearchButton.setOnAction(e -> setupSearchDates(true));
        todaySalesButton.setOnAction(e -> setupSearchDates(false));
        clientNameButton.setOnAction(e -> setupSearchMode("NOMBRE DEL CLIENTE:", "Ingrese el nombre del cliente", clientNameButton));
        PayTypeButton.setOnAction(e -> setupSearchMode("TIPO DE COBRO:", "Ingrese el tipo de cobro", PayTypeButton));
        clearButton.setOnAction(e -> handleClearButton());
        startDate.valueProperty().addListener((obs, oldVal, newVal) -> handleDateRangeSearch());
        endDate.valueProperty().addListener((obs, oldVal, newVal) -> handleDateRangeSearch());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearchFieldChange());
    }
    
    /**
     * Configura el modo de búsqueda genérico.
     */
    private void setupSearchMode(String label, String prompt, MenuItem activeButton) {
        setVisibility(dateBar, false);
        setVisibility(fieldBar, true);
        templateLabel.setText(label);
        searchField.clear();
        searchField.setPromptText(prompt);
        searchField.requestFocus();
        toggleMenuItemStyle(activeButton, dateSearchButton, clientTypeButton, clientNameButton, PayTypeButton);
    }
    
    /**
     * Establece visibilidad y managed de un nodo.
     */
    private void setVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Muestra u oculta los detalles de la orden con transición.
     */
    private void showDetail(boolean visible) {
        double startOpacity = visible ? 0.0 : 1.0;
        double endOpacity = visible ? 1.0 : 0.0;
        
        if (visible) {
            orderDetailTable.setVisible(true);
            orderDetailTable.setManaged(true);
            orderDetailTable.setOpacity(0); 
        }
        
        FadeTransition fade = createFadeTransition(orderDetailTable, startOpacity, endOpacity);
        fade.setOnFinished(e -> {
            if (!visible) {
                orderDetailTable.setVisible(false);
                orderDetailTable.setManaged(false);
            }
        });
        
        updateDetailPanelsVisibility(!visible);
        fade.play();
    }
    
    /**
     * Crea una transición de desvanecimiento reutilizable.
     */
    private FadeTransition createFadeTransition(Node node, double fromValue, double toValue) {
        FadeTransition fade = new FadeTransition(Duration.millis(SalesStyleConstants.FADE_DURATION_MS), node);
        fade.setFromValue(fromValue);
        fade.setToValue(toValue);
        return fade;
    }
    
    /**
     * Actualiza la visibilidad de los paneles de detalles.
     */
    private void updateDetailPanelsVisibility(boolean visible) {
        detailLabel.setVisible(visible);
        detailLabel.setManaged(visible);
        grandTotalBar.setVisible(visible);
        grandTotalBar.setManaged(visible);
        if (visible) {
            detailLabel.setOpacity(1.0);
            grandTotalBar.setOpacity(1.0);
        }
    }

    /**
     * Configura la lógica de selección de filas en la tabla principal.
     */
    private void setupSelectionLogic() {
        OrderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && !newSelection.isEmpty()) {
                int rowIndex = OrderTable.getSelectionModel().getSelectedIndex();
                if (orderIdMap.containsKey(rowIndex)) {
                    int orderId = orderIdMap.get(rowIndex);
                    loadOrderDetails(orderId);
                }
            }
            showDetail(newSelection != null && !newSelection.isEmpty());
        });
        TableBar.setOnMouseClicked(event -> {
            if (event.getTarget() == TableBar || event.getTarget() instanceof javafx.scene.shape.Shape) {
                OrderTable.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * Configura los listeners globales del formulario.
     */
    private void setupGlobalClickConfig() {
        mainRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node clickedNode = (Node) event.getTarget();
            if (!isChildOfTable(clickedNode)) {
                OrderTable.getSelectionModel().clearSelection();
                orderDetailTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    /**
     * Verifica si un nodo es hijo de una tabla.
     */
    private boolean isChildOfTable(Node node) {
        while (node != null) {
            if (node instanceof TableView) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    /**
     * Muestra u oculta la barra de búsqueda por fechas.
     */
    private void setupSearchDates(boolean showDates) {
        setVisibility(dateBar, showDates);
        setVisibility(fieldBar, false);
        
        if (showDates) {
            toggleMenuItemStyle(dateSearchButton, dateSearchButton, clientTypeButton, clientNameButton, PayTypeButton);
        } else {
            toggleMenuItemStyle(null, dateSearchButton, clientTypeButton, clientNameButton, PayTypeButton);
        }
        updateTabStyles(showDates);
    }

    /**
     * Actualiza los estilos de las pestañas de búsqueda.
     */
    private void updateTabStyles(boolean showDates) {
        dateSearchButton.getStyleClass().removeAll(SalesStyleConstants.TAB_BUTTON.getValue(), SalesStyleConstants.TAB_ACTIVE.getValue());
        todaySalesButton.getStyleClass().removeAll(SalesStyleConstants.TAB_BUTTON.getValue(), SalesStyleConstants.TAB_ACTIVE.getValue());
        if (showDates) {
            dateSearchButton.getStyleClass().add(SalesStyleConstants.TAB_ACTIVE.getValue());
            todaySalesButton.getStyleClass().add(SalesStyleConstants.TAB_BUTTON.getValue());
        } else {
            todaySalesButton.getStyleClass().add(SalesStyleConstants.TAB_ACTIVE.getValue());
            dateSearchButton.getStyleClass().add(SalesStyleConstants.TAB_BUTTON.getValue());
        }
    }

    /**
     * Maneja el clic en el botón "LIMPIAR CAMPOS".
     * Limpia los DatePicker, el campo de búsqueda y la tabla.
     */
    private void handleClearButton() {
        startDate.setValue(null);
        endDate.setValue(null);
        searchField.clear();
        updateTotalLabel(0.0);
        OrderTable.setItems(FXCollections.observableArrayList());
        orderDetailTable.setItems(FXCollections.observableArrayList());
        setVisibility(fieldBar, false);
    }
    
    /**
     * Actualiza la etiqueta del total de ventas.
     */
    private void updateTotalLabel(double total) {
        totalLabel.setText(SalesMessages.MSG_TOTAL_SALES.getValue() + "$" + String.format("%.2f", total));
    }

    // =============== BOTONES ===============
    /**
     * No implementa setupAllButtons ya que no hay botones comunes.
     */
    @Override
    protected void setupAllButtons() {
        // Los botones se configuran en setupAdditionalConfig
    }

    // =============== CARGA Y CONSTRUCCIÓN DE DATOS ===============
    /**
     * Carga las órdenes del día desde el servicio.
     */
    private void loadOrdersFromService() {
        try {
            List<ModeloVentasView> todayOrders = orderService.getTodayOrders();
            if (todayOrders == null || todayOrders.isEmpty()) {
                System.out.println(SalesMessages.MSG_NO_ORDERS.getValue());
                updateTotalLabel(0.0);
                return;
            }
            loadOrdersToTable(todayOrders);
        } catch (Exception e) {
            System.err.println("Error Cargando Ordenes de service: " + e.getMessage());
            e.printStackTrace();
            OrderTable.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Carga órdenes en la tabla y actualiza el total.
     */
    private void loadOrdersToTable(List<ModeloVentasView> orders) {
        ObservableList<ObservableList<String>> orderTableData = buildOrderTableData(orders);
        double totalVentas = calculateTotalSales(orders);
        updateTotalLabel(totalVentas);
        configureOrderTableColumns();
        OrderTable.setItems(orderTableData);
    }
    
    /**
     * Construye los datos de la tabla de órdenes.
     */
    private ObservableList<ObservableList<String>> buildOrderTableData(List<ModeloVentasView> todayOrders) {
        ObservableList<ObservableList<String>> orderTableData = FXCollections.observableArrayList();
        orderIdMap.clear();
        int rowIndex = 0;
        
        for (ModeloVentasView order : todayOrders) {
            String status = order.isFacturado() ? SalesFormatConstants.STATUS_PRINTED.getValue() : SalesFormatConstants.STATUS_NOT_PRINTED.getValue();
            String clientName = order.getNombreCliente() != null ? order.getNombreCliente() : SalesFormatConstants.DEFAULT_CLIENT_NAME.getValue();
            String paymentType = order.getNombreTipoPago() != null ? order.getNombreTipoPago() : SalesFormatConstants.DEFAULT_PAYMENT_TYPE.getValue();
            
            // Calcular el total desde los detalles de la orden
            double totalOrden = calculateOrderTotal(order);
            
            ObservableList<String> row = FXCollections.observableArrayList(
                clientName,
                paymentType,
                String.format(SalesFormatConstants.CURRENCY_FORMAT.getValue(), totalOrden),
                status,
                SalesUILabels.BTN_EDIT.getValue() + "|" + SalesUILabels.BTN_DELETE.getValue()
            );
            orderTableData.add(row);
            orderIdMap.put(rowIndex, order.getIdOrden());
            rowIndex++;
        }
        
        return orderTableData;
    }
    
    /**
     * Calcula el total de una orden sumando los detalles.
     */
    private double calculateOrderTotal(ModeloVentasView order) {
        if (order.getDetalleOrden() == null || order.getDetalleOrden().isEmpty()) {
            return 0.0;
        }
        return order.getDetalleOrden().stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioDetalleOrden())
                .sum();
    }
    
    /**
     * Calcula el total de ventas.
     */
    private double calculateTotalSales(List<ModeloVentasView> orders) {
        double total = 0;
        for (ModeloVentasView order : orders) {
            total += calculateOrderTotal(order);
        }
        System.out.println("Calculando total para " + orders.size() + " órdenes: $" + String.format("%.2f", total));
        for (ModeloVentasView order : orders) {
            double orderTotal = calculateOrderTotal(order);
            System.out.println("  Orden " + order.getIdOrden() + ": $" + String.format("%.2f", orderTotal));
        }
        
        return total;
    }
    
    /**
     * Configura las columnas de la tabla de órdenes.
     */
    private void configureOrderTableColumns() {
        for (int colIndex = 0; colIndex < OrderTable.getColumns().size(); colIndex++) {
            final int index = colIndex;
            TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) OrderTable.getColumns().get(colIndex);
            
            if (colIndex == OrderTable.getColumns().size() - 1) {
                col.setCellFactory(tc -> createOrderActionCell());
            } else {
                col.setCellValueFactory(param -> {
                    if (param.getValue().size() > index) {
                        return new SimpleStringProperty(param.getValue().get(index));
                    }
                    return new SimpleStringProperty("");
                });
            }
        }
    }
    
    /**
     * Crea una celda con botones de acción.
     */
    private TableCell<ObservableList<String>, String> createOrderActionCell() {
        return new TableCell<ObservableList<String>, String>() {
            private final Button editBtn = createStyledButton(SalesUILabels.BTN_EDIT.getValue());
            private final Button deleteBtn = createStyledButton(SalesUILabels.BTN_DELETE.getValue());
            private final Button reprintBtn = createStyledButton(SalesUILabels.BTN_REPRINT.getValue());
            private final HBox hbox = new HBox(SalesStyleConstants.BUTTON_SPACING);
            
            {
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(editBtn, deleteBtn, reprintBtn);
                
                editBtn.setOnAction(e -> handleEditButtonClick(getIndex()));
                deleteBtn.setOnAction(e -> handleDeleteButtonClick(getIndex()));
                reprintBtn.setOnAction(e -> handleReprintButtonClick(getIndex()));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        };
    }
    
    /**
     * Crea un botón con estilos predefinidos.
     */
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add(SalesStyleConstants.TABLE_ACTION_BTN.getValue());
        return btn;
    }


        /**
     * Cambia los estilos CSS de los botones para indicar cuál está activo.
     */



    private void toggleGroupStyle(Button activeBtn, Button... group) {
        for (Button btn : group) {
            btn.getStyleClass().removeAll("order-button", "order-button-active");
            btn.getStyleClass().add(btn == activeBtn ? "order-button-active" : "order-button");
        }
    }
    /**
     * Cambia los estilos CSS de los MenuItem para indicar cuál está activo.
     * Similar a toggleGroupStyle pero para MenuItem.
     */
    private void toggleMenuItemStyle(MenuItem activeItem, MenuItem... group) {
        for (MenuItem item : group) {
            // Solo remover la clase activa, no tocar otras clases
            item.getStyleClass().remove("sales-tab-active");
            // Agregar la clase activa solo al item seleccionado
            if (item == activeItem) {
                item.getStyleClass().add("sales-tab-active");
            }
        }
    }

    // =============== MANEJADORES DE ACCIONES ===============
    /**
     * Maneja el clic en el botón Editar.
     */
    private void handleEditButtonClick(int rowIndex) {
        if (orderIdMap.containsKey(rowIndex)) {
            int orderId = orderIdMap.get(rowIndex);
            handleEditOrderById(orderId, OrderTable.getItems().get(rowIndex));
        }
    }
    
    /**
     * Maneja el clic en el botón Borrar.
     */
    private void handleDeleteButtonClick(int rowIndex) {
        if (orderIdMap.containsKey(rowIndex)) {
            int orderId = orderIdMap.get(rowIndex);
            handleDeleteOrderById(orderId, OrderTable.getItems().get(rowIndex));
        }
    }

    /**
     * Maneja el clic en el botón Reimprimir.
     */
    private void handleReprintButtonClick(int rowIndex) {
        if (orderIdMap.containsKey(rowIndex)) {
            int orderId = orderIdMap.get(rowIndex);
            handleReprintOrderById(orderId);
        }
    }
    
    /**
     * Reimprime el ticket de una orden.
     */
    private void handleReprintOrderById(int orderId) {
        try {
            // Obtener orden con detalles
            ModeloVentasView order = orderService.getOrderWithDetails(orderId);
            if (order == null) {
                showAlert("ERROR", "Orden no encontrada", "No se pudo cargar la orden " + orderId);
                return;
            }
            
            // Convertir a ModeloOrden
            ModeloOrden ordenBase = createOrderFromSale(order);
            
            // Obtener detalles
            List<ModeloDetalleOrden> detalles = orderService.getOrderDetails(orderId);
            
            // Crear modelo de recibo
            ModeloRecibo recibo = new ModeloRecibo();
            recibo.setOrden(new ModeloOrdenCompleta(ordenBase, detalles));
            recibo.setFechaExpedicion(ordenBase.getFechaExpedicionOrden());
            
            // Intentar imprimir
            PrintResultEnum printResult = ticketService.printOrderTicket(recibo);
            
            // Manejar resultado de impresión
            handlePrintResult(printResult, orderId, order);
            
        } catch (Exception e) {
            System.err.println("Error al intentar reimprimir: " + e.getMessage());
            e.printStackTrace();
            showAlert("ERROR", "Error", "Error al reimprimir: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el resultado de la impresión y actualiza el estado de la orden si es necesario.
     */
    private void handlePrintResult(PrintResultEnum result, int orderId, ModeloVentasView order) {
        switch(result) {
            case SUCCESS:
                // Marcar como impreso si no lo estaba
                if (!order.isFacturado()) {
                    order.setFacturado(true);
                    orderService.updateOrderHeader(new ModeloOrdenMostrador());
                    // Actualizar el orden en la BD
                    ModeloOrden modeloOrden = createOrderFromSale(order);
                    modeloOrden.setFacturado(true);
                    orderService.updateOrderHeader(modeloOrden);
                }
                showAlert("SUCCESS", "Éxito", "Ticket impreso correctamente.\nOrden #" + orderId + " marcada como impresa.");
                loadOrdersFromService();
                break;
            case PRINTER_NOT_AVAILABLE:
                showAlert("WARNING", "Impresora no disponible", 
                    "No se pudo imprimir el ticket de la orden #" + orderId + 
                    ".\nVerifique que la impresora esté conectada y disponible.");
                break;
            case NO_PAPER:
                showAlert("WARNING", "Sin papel", 
                    "No se pudo imprimir el ticket de la orden #" + orderId + 
                    ".\nVerifique que la impresora tenga papel.");
                break;
            case PRINT_ERROR:
                showAlert("ERROR", "Error de impresión", 
                    "Ocurrió un error al intentar imprimir el ticket de la orden #" + orderId + 
                    ".\nIntente nuevamente.");
                break;
        }
    }
    
    /**
     * Crea un objeto ModeloOrden a partir de un ModeloVentasView.
     */
    private ModeloOrden createOrderFromSale(ModeloVentasView venta) {
        ModeloOrdenMostrador orden = new ModeloOrdenMostrador();
        orden.setIdOrden(venta.getIdOrden());
        orden.setIdRelTipoPago(venta.getIdRelTipoPago());
        orden.setTipoCliente(venta.getTipoCliente());
        orden.setFechaExpedicionOrden(venta.getFechaExpedicionOrden() != null ? 
            venta.getFechaExpedicionOrden() : LocalDateTime.now());
        orden.setPrecioOrden(venta.getPrecioOrden());
        orden.setPagoCliente(venta.getPagoCliente());
        orden.setFacturado(venta.isFacturado());
        return orden;
    }

    /**
     * Edita una orden por su ID.
     */
    private void handleEditOrderById(int orderId, ObservableList<String> row) {
        try {
            ModeloVentasView order = orderService.getOrderWithDetails(orderId);
            if (order == null) {
                showAlert("ERROR", "Orden no encontrada", "No se pudo cargar la orden " + orderId);
                return;
            }
            
            showEditOrderDialog(order);
        } catch (Exception e) {
            System.err.println("Error al editar orden: " + e.getMessage());
            e.printStackTrace();
            showAlert("ERROR", "Error", "Error al cargar la orden: " + e.getMessage());
        }
    }

    /**
     * Elimina una orden por su ID.
     */
    private void handleDeleteOrderById(int orderId, ObservableList<String> row) {
        if (showDeleteConfirmationDialog(orderId)) {
            try {
                if (orderService.deleteOrder(orderId)) {
                    showAlert("SUCCESS", "Éxito", SalesMessages.ALERT_SUCCESS_DELETE.getValue());
                    loadOrdersFromService();
                } else {
                    showAlert("ERROR", "Error", "No se pudo eliminar la orden");
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar orden: " + e.getMessage());
                e.printStackTrace();
                showAlert("ERROR", "Error", "Error al eliminar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Muestra un diálogo de confirmación para eliminar.
     */
    private boolean showDeleteConfirmationDialog(int orderId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar orden?");
        alert.setContentText(SalesMessages.ALERT_CONFIRM_DELETE.getValue() + orderId + "?\n" + SalesMessages.ALERT_DELETE_WARNING.getValue());
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra el diálogo de edición de orden.
     */
    private void showEditOrderDialog(ModeloVentasView order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/view/pop-up/sales/editOrderDialog.fxml"));
            
            Parent dialogRoot = loader.load();
            
            EditOrderDialogController dialogController = loader.getController();
            
            Stage editStage = new Stage();
            editStage.setTitle("Editar Orden #" + order.getIdOrden());
            editStage.setResizable(true);
            dialogController.setDialogStage(editStage);
            
            dialogController.setOrderData(order);
            
            Scene scene = new Scene(dialogRoot);
            editStage.setScene(scene);
            editStage.showAndWait();
            
            loadOrdersFromService();
        } catch (Exception e) {
            System.err.println("Error al abrir diálogo de edición: " + e.getMessage());
            e.printStackTrace();
            showAlert("ERROR", "Error", "Error al abrir el diálogo de edición: " + e.getMessage());
        }
    }

    /**
     * Configura el controlador de la ventana cargada según el tipo de FXML.
     */
    @Override
    protected void configureLoadedController(String fxmlPath, javafx.fxml.FXMLLoader loader) {
        // No se necesita configuración adicional para SalesController
    }

    /**
     * Carga los detalles de una orden específica.
     */
    private void loadOrderDetails(int orderId) {
        try {
            ModeloVentasView orderWithDetails = orderService.getOrderWithDetails(orderId);
            ObservableList<ObservableList<String>> detailData = FXCollections.observableArrayList();

            if (orderWithDetails != null && orderWithDetails.getDetalleOrden() != null) {
                for (ModeloDetalleOrden detail : orderWithDetails.getDetalleOrden()) {
                    double total = detail.getCantidad() * detail.getPrecioDetalleOrden();
                    ObservableList<String> row = FXCollections.observableArrayList(
                        detail.getEspecificacionesDetalleOrden() != null ? detail.getEspecificacionesDetalleOrden() : "N/A",
                        String.valueOf(detail.getCantidad()),
                        String.format("$%.2f", detail.getPrecioDetalleOrden()),
                        String.format("$%.2f", total)
                    );
                    detailData.add(row);
                }
            }

            for (int colIndex = 0; colIndex < orderDetailTable.getColumns().size(); colIndex++) {
                final int index = colIndex;
                TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) orderDetailTable.getColumns().get(colIndex);
                col.setCellValueFactory(param -> {
                    if (param.getValue().size() > index) {
                        return new SimpleStringProperty(param.getValue().get(index));
                    }
                    return new SimpleStringProperty("");
                });
            }

            orderDetailTable.setItems(detailData);
        } catch (Exception e) {
            System.err.println("Error loading order details for ID: " + orderId + " - " + e.getMessage());
            e.printStackTrace();
            orderDetailTable.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Maneja la búsqueda de órdenes por rango de fechas.
     */
    private void handleDateRangeSearch() {
        LocalDate startDateValue = startDate.getValue();
        LocalDate endDateValue = endDate.getValue();
        
        if (startDateValue == null || endDateValue == null) {
            return;
        }
        
        if (startDateValue.isAfter(endDateValue)) {
            showAlert("VALIDACIÓN", "Rango de fechas inválido", 
                "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }
        
        try {
            List<ModeloVentasView> ordersInRange = orderService.getOrdersByDateRange(
                startDateValue, endDateValue, null, 0, null);
            
            if (ordersInRange == null || ordersInRange.isEmpty()) {
                System.out.println("No hay órdenes en el rango de fechas seleccionado");
                updateTotalLabel(0.0);
                OrderTable.setItems(FXCollections.observableArrayList());
                return;
            }
            
            loadOrdersToTable(ordersInRange);
            System.out.println("Búsqueda por fechas completada: " + ordersInRange.size() + " órdenes encontradas");
        } catch (Exception e) {
            handleSearchError("Error al buscar órdenes por rango de fechas: ", e);
        }
    }
    /**
     * Maneja la búsqueda cuando el usuario ingresa texto en el campo de búsqueda.
     */
    private void handleSearchFieldChange() {
        String searchText = searchField.getText().trim();
        String templateText = templateLabel.getText();

        if (searchText.isEmpty()) {
            updateTotalLabel(0.0);
            OrderTable.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<ModeloVentasView> filteredOrders = getFilteredOrders(searchText, templateText);
            
            if (filteredOrders == null || filteredOrders.isEmpty()) {
                System.out.println("No hay órdenes que coincidan con la búsqueda: " + searchText);
                updateTotalLabel(0.0);
                OrderTable.setItems(FXCollections.observableArrayList());
                return;
            }

            loadOrdersToTable(filteredOrders);
            System.out.println("Búsqueda completada: " + filteredOrders.size() + " órdenes encontradas");
        } catch (Exception e) {
            handleSearchError("Error al buscar órdenes: ", e);
        }
    }
    
    /**
     * Obtiene órdenes filtradas según el tipo de búsqueda.
     */
    private List<ModeloVentasView> getFilteredOrders(String searchText, String templateText) {
        if (templateText.contains("NOMBRE DEL CLIENTE")) {
            System.out.println("Buscando por nombre de cliente: " + searchText);
            return orderService.getTodayOrdersFiltered(null, 0, searchText);
        } else if (templateText.contains("TIPO DE COBRO")) {
            System.out.println("Buscando por tipo de cobro: " + searchText);
            return searchOrdersByPaymentType(searchText);
        }
        return List.of();
    }
    
    /**
     * Maneja errores de búsqueda.
     */
    private void handleSearchError(String message, Exception e) {
        System.err.println(message + e.getMessage());
        e.printStackTrace();
        showAlert("ERROR", "Error en la búsqueda", "Ocurrió un error al buscar: " + e.getMessage());
    }

    /**
     * Busca órdenes por tipo de pago.
     */
    private List<ModeloVentasView> searchOrdersByPaymentType(String paymentTypeName) {
        try {
            List<ModeloTipoPago> paymentTypes = orderService.getPaymentTypes();
            System.out.println("Tipos de pago disponibles: " + paymentTypes.size());
            
            int matchingPaymentTypeId = findPaymentTypeId(paymentTypes, paymentTypeName);
            
            if (matchingPaymentTypeId == -1) {
                System.out.println("No se encontró tipo de pago que coincida con: " + paymentTypeName);
                return List.of();
            }
            
            LocalDate veryOldDate = LocalDate.of(2000, 1, 1);
            LocalDate today = LocalDate.now();
            List<ModeloVentasView> allOrders = orderService.getOrdersByDateRange(
                veryOldDate, today, null, matchingPaymentTypeId, null);
            
            System.out.println("Órdenes encontradas con tipo de pago ID " + matchingPaymentTypeId + ": " + allOrders.size());
            return allOrders;
        } catch (Exception e) {
            System.err.println("Error al buscar por tipo de pago: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Busca el ID del tipo de pago que coincida con el nombre.
     */
    private int findPaymentTypeId(List<ModeloTipoPago> paymentTypes, String paymentTypeName) {
        for (ModeloTipoPago paymentType : paymentTypes) {
            if (paymentType.getNombreTipoPago() != null && 
                paymentType.getNombreTipoPago().toLowerCase().contains(paymentTypeName.toLowerCase())) {
                System.out.println("Tipo de pago encontrado: " + paymentType.getNombreTipoPago() + 
                                 " (ID: " + paymentType.getIdTipoPago() + ")");
                return paymentType.getIdTipoPago();
            }
        }
        return -1;
    }
}



