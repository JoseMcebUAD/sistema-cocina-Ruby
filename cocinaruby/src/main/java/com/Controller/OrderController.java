package com.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.Model.ModeloDetalleOrden;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Enum.OrderUIConstants;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class OrderController extends BaseController {
    @FXML private BorderPane mainRoot;
    @FXML private VBox clientBar,TableBar;
    @FXML private Button addProductButton, tableButton, deliveryButton, dineinButton, 
    makeOrderButton, selectClientButton;
    @FXML private TextField addressField, clientNameField, phoneNumberField, priceField, 
    productField, quantityField,tableNumberField;
    @FXML private Label grandTotalLabel;
    @FXML private TableView<ModeloDetalleOrden> productsTable;
    private ObservableList<ModeloDetalleOrden> orderItems = FXCollections.observableArrayList();
    private boolean isUpdatingFromSelection = false;
    private String currentOrderType;
    private int currentClientId = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    @Override
    protected void setupAdditionalConfig() {
        setupGlobalClickConfig();
        showClientBar(false);
        showTableBar(false);
        selectOrderType(dineinButton);
        setupValidations();
    }

    // =============== VALIDACIONES ===============
    /**
     * Valida que los campos obligatorios del producto no estén vacíos.
     */
    private boolean validateInputs() {
        if (productField.getText().isEmpty() || quantityField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showAlert(OrderUIConstants.MSG_EMPTY_FIELDS.getValue(), OrderUIConstants.MSG_FILL_FIELDS.getValue());
            return false;
        }
        return true;
    }

    /**
     * Valida que la orden cumpla con los requisitos según su tipo.
     */
    private boolean validateOrderRequirements() {
        if (orderItems.isEmpty()) {
            showAlert(OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_NO_PRODUCTS.getValue());
            return false;
        }
        if (currentOrderType.equals(TiposClienteEnum.PAGO_DOMICILIO)) {
            if (clientNameField.getText().trim().isEmpty() || 
                addressField.getText().trim().isEmpty() || 
                phoneNumberField.getText().trim().isEmpty()) {
                showAlert(OrderUIConstants.MSG_INCOMPLETE_DATA.getValue(), OrderUIConstants.MSG_DELIVERY_REQUIRED.getValue());
                return false;
            }
        } 
        else if (currentOrderType.equals(TiposClienteEnum.PAGO_MESA)) {
            if (tableNumberField.getText().trim().isEmpty()) {
                showAlert(OrderUIConstants.MSG_INCOMPLETE_DATA.getValue(), OrderUIConstants.MSG_TABLE_REQUIRED.getValue());
                return false;
            }
        }
        return true;
    }

    /**
     * Configura validadores para que solo acepten caracteres numéricos o decimales.
     */
    private void setupValidations() {
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        tableNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tableNumberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });
    }

    // =============== BOTONES ===============
    @Override
    protected void setupAllButtons() {
        setupDeliveryButton();
        setupDineinButton();
        setupTableButton();
        setupSelectClientButton();
        setupMakeOrderButton();
        setupAddProductButton();
    }

    /**
     * Configura el botón para agregar o actualizar productos.
     */
    private void setupAddProductButton(){
        addProductButton.setOnAction(e -> handleAddOrUpdateProduct());
    }

    /**
     * Configura el botón de entrega a domicilio.
     */
    private void setupDeliveryButton(){
        deliveryButton.setOnAction(e -> {
            showClientBar(true); 
            showTableBar(false);  
            selectOrderType(deliveryButton);
        });
    }

    /**
     * Configura el botón de comer en el lugar.
     */
    private void setupDineinButton(){
        dineinButton.setOnAction(e -> {
            showClientBar(false); 
            showTableBar(false);  
            selectOrderType(dineinButton);
        });
    }

    /**
     * Configura el botón de compra por mesa.
     */
    private void setupTableButton() {
        tableButton.setOnAction(e -> {
            showClientBar(false); 
            showTableBar(true);   
            selectOrderType(tableButton);
        });
    }

    /**
     * Configura el botón para seleccionar cliente.
     */
    private void setupSelectClientButton(){
        selectClientButton.setOnAction(e -> openClientsView());
    }

    /**
     * Configura el botón para crear la orden.
     */
    private void setupMakeOrderButton(){
        makeOrderButton.setOnAction(e -> {
            if (validateOrderRequirements()) {
                openConfirmationView();
            }
        });
    }

    /**
     * Muestra u oculta la barra de cliente con animación de transición.
     */
    private void showClientBar(boolean show) {
        clientBar.setVisible(show);
        clientBar.setManaged(show);
        if (show) playFade(clientBar);
    }

    /**
     * Muestra u oculta la barra de mesa con animación de transición.
     */
    private void showTableBar(boolean show) {
        if (TableBar != null) {
            TableBar.setVisible(show);
            TableBar.setManaged(show);
            if (show) playFade(TableBar);
        }
    }

    /**
     * Reproduce una animación de transición de opacidad.
     */
    private void playFade(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Establece el tipo de orden actual según el botón seleccionado.
     */
    private void selectOrderType(Button selected) {
        toggleGroupStyle(selected, dineinButton, deliveryButton, tableButton); 

        if (selected == dineinButton) {
            currentOrderType = TiposClienteEnum.PAGO_MOSTRADOR;
        } else if (selected == deliveryButton) {
            currentOrderType = TiposClienteEnum.PAGO_DOMICILIO;
        } else if (selected == tableButton) {
            currentOrderType = TiposClienteEnum.PAGO_MESA;
        } 
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

    // =============== VENTANAS ===============
    /**
     * Abre la vista de selección de clientes.
     */
    private void openClientsView() {
        showView("/com/view/clients.fxml", "Seleccionar Cliente", 1000, 600);
    }

    /**
     * Abre el modal de confirmación de orden.
     */
    private void openConfirmationView() {
        showModal("/com/view/pop-up/order/confirmOrder.fxml", "Detalles de Pago", 539, 481);
    }

    /**
     * Configura el controlador de la ventana cargada según el tipo de FXML.
     */
    @Override
    protected void configureLoadedController(String fxmlPath, javafx.fxml.FXMLLoader loader) {
        if (fxmlPath.contains("clients")) {
            ClientsController controller = loader.getController();
            controller.setParentController(this);
            controller.showConfirmBar();
        }

        if (fxmlPath.contains("confirmOrder")) {
            com.Controller.popup.order.confirmOrderController controller = loader.getController();
            
            double currentTotal = orderItems.stream()
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad())
                .sum();

            // Decidir qué "Nombre" enviar: Nombre del Cliente O Número de Mesa
            String clientIdentifier = clientNameField.getText();
            if (currentOrderType.equals(TiposClienteEnum.PAGO_MESA)) {
                clientIdentifier = "Mesa " + tableNumberField.getText();
            }

            controller.setOrderData(
                new ArrayList<>(orderItems),
                currentTotal,
                currentOrderType,
                currentClientId,
                clientIdentifier,
                addressField.getText(),
                phoneNumberField.getText()
            );
            
            controller.setParentController(this);
        }
    }

    // =============== TABLAS ===============
    /**
     * Establece los datos del cliente en los campos correspondientes.
     */
    public void setClientData(int clientId, String name, String address, String phoneNumber) {
        clientNameField.setText(name);
        addressField.setText(address);
        phoneNumberField.setText(phoneNumber);
        this.currentClientId = clientId;
    }

    /**
     * Configura las columnas de la tabla de productos.
     */
    @Override
    protected void setupTableConfig() {
        ObservableList<TableColumn<ModeloDetalleOrden, ?>> columns = productsTable.getColumns();
        
        if (columns.size() >= 3) {
            columns.get(0).setCellValueFactory(new PropertyValueFactory<>("especificacionesDetalleOrden"));
            columns.get(1).setCellValueFactory(new PropertyValueFactory<>("cantidad"));
            columns.get(2).setCellValueFactory(new PropertyValueFactory<>("precioDetalleOrden"));
        }
        productsTable.setItems(orderItems);
        setupTableSelectionListener();
        productsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                handleDeleteProduct();
            }
        });
    }

    /**
     * Configura el listener para cuando se selecciona un item de la tabla.
     */
    private void setupTableSelectionListener() {
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            isUpdatingFromSelection = true;
            if (newSelection != null) {
                // Rellenar campos para edición
                productField.setText(newSelection.getEspecificacionesDetalleOrden());
                quantityField.setText(String.valueOf(newSelection.getCantidad()));
                priceField.setText(String.valueOf(newSelection.getPrecioDetalleOrden()));
                
                // Cambiar texto del botón visualmente para indicar edición (Opcional)
                addProductButton.setText("ACTUALIZAR");
            } else {
                clearProductFields();
                addProductButton.setText("AGREGAR");
            }
            isUpdatingFromSelection = false;
        });
    }

    // =============== LÓGICA DE TABLA ===============
    /**
     * Decide si agregar un nuevo producto o actualizar el seleccionado.
     */
    private void handleAddOrUpdateProduct() {
        if (validateInputs()) {
            if (productsTable.getSelectionModel().getSelectedItem() != null) {
                handleUpdateProduct(); // Si hay selección, actualiza
            } else {
                handleAddProduct();    // Si no, agrega nuevo
            }
        }
    }

    /**
     * Agrega un nuevo producto a la orden.
     */
    private void handleAddProduct() {
        ModeloDetalleOrden newItem = getTextFieldInfo();
        orderItems.add(newItem);
        updateTotalLabel();
        clearProductFields();
    }

    /**
     * Actualiza un producto existente en la tabla.
     */
    private void handleUpdateProduct() {
        ModeloDetalleOrden selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Actualizamos el objeto existente
            selectedItem.setEspecificacionesDetalleOrden(productField.getText());
            selectedItem.setCantidad(Integer.parseInt(quantityField.getText()));
            selectedItem.setPrecioDetalleOrden(Double.parseDouble(priceField.getText()));
            
            productsTable.refresh(); // Refrescar vista
            updateTotalLabel();
            clearProductFields();
            productsTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Obtiene la información de los campos y crea un objeto ModeloDetalleOrden.
     */
    private ModeloDetalleOrden getTextFieldInfo() {
        ModeloDetalleOrden item = new ModeloDetalleOrden();
        item.setEspecificacionesDetalleOrden(productField.getText());
        try {
            item.setCantidad(Integer.parseInt(quantityField.getText()));
            item.setPrecioDetalleOrden(Double.parseDouble(priceField.getText()));
        } catch (NumberFormatException e) {
            showAlert("Error de Formato", "Cantidad y Precio deben ser números.");
        }
        return item;
    }
    
    /**
     * Elimina el producto seleccionado de la tabla.
     */
    private void handleDeleteProduct() {
        ModeloDetalleOrden selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            orderItems.remove(selected);
            updateTotalLabel();
            clearProductFields();
        }
    }

    // =============== UTILIDADES ===============
    /**
     * Actualiza el label del total multiplicando precio por cantidad.
     */
    private void updateTotalLabel() {
        double total = orderItems.stream()
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad()) 
                .sum();
        grandTotalLabel.setText("$" + String.format("%.2f", total));
    }

    /**
     * Limpia los campos de entrada de productos.
     */
    private void clearProductFields() {
        productField.clear();
        quantityField.clear();
        priceField.clear();
        productsTable.getSelectionModel().clearSelection();
    }

    /**
     * Limpia todos los datos de la orden actual.
     */
    public void clearOrder() {
        orderItems.clear();
        clientNameField.clear();
        addressField.clear();
        phoneNumberField.clear();
        tableNumberField.clear();
        productField.clear();
        quantityField.clear();
        priceField.clear();
        grandTotalLabel.setText("$0.00");
        selectOrderType(dineinButton);
        currentClientId = 0;
    }

    /**
     * Configura los listeners globales del formulario para limpiar campos solo si están vacíos.
     */
    private void setupGlobalClickConfig() {
        mainRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node clickedNode = (Node) event.getTarget();
            if (!isChildOfTable(clickedNode) && !isChildOfControl(clickedNode)) {
                productsTable.getSelectionModel().clearSelection();
                // Solo limpiar campos si están vacíos
                if (productField.getText().isEmpty() && 
                    quantityField.getText().isEmpty() && 
                    priceField.getText().isEmpty()) {
                    clearProductFields();
                }
            }
        });
    }

    /**
     * Verifica si un nodo es hijo de un control de entrada.
     */
    private boolean isChildOfControl(Node node) {
        while (node != null) {
            if (node instanceof TextField || node instanceof Button) return true;
            node = node.getParent();
        }
        return false;
    }

    /**
     * Verifica si un nodo es hijo de una tabla.
     */
    private boolean isChildOfTable(Node node) {
        while (node != null) {
            if (node instanceof TableView) return true;
            node = node.getParent();
        }
        return false;
    }
}