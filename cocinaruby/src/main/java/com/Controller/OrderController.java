package com.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.Model.ModeloDetalleOrden;
import com.Model.ModeloMesa;
import com.Model.Enum.OrderUIConstants;
import com.Service.MesaService;
import com.Controller.order.OrderSelectedInterface;
import com.Controller.order.OrdersSelected;
import com.Controller.order.OrderSelectedInterface.MSGValidacion;
import com.Controller.popup.order.confirmOrderController;

import java.util.List;

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

public class OrderController extends BaseController {
    @FXML protected BorderPane mainRoot;
    @FXML public VBox clientBar,TableBar,DeliveryBar;
    @FXML public Button addProductButton, tableButton, deliveryButton, dineinButton,
    makeOrderButton, selectClientButton,deleteButton;
    @FXML public TextField addressField, clientNameField, phoneNumberField, priceField,
    productField, quantityField, dineinNameField, tarifaDomicilioField;
    @FXML public ComboBox<ModeloMesa> tableComboBox;
    @FXML public Label grandTotalLabel;
    @FXML protected TableView<ModeloDetalleOrden> productsTable;
    public ObservableList<ModeloDetalleOrden> orderItems = FXCollections.observableArrayList();
    protected boolean isUpdatingFromSelection = false;
    public int currentClientId = 0;
    protected int mesaSeleccionadaId = 0;
    public MesaService mesaService = new MesaService();

    // Strategy pattern for order types
    private OrderSelectedInterface orderStrategy;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    @Override
    protected void setupAdditionalConfig() {
        setupGlobalClickConfig();
        // Inicializar con orden de mostrador por defecto
        setOrderStrategy(new OrdersSelected.OrderDineinSelected(this));
        toggleGroupStyle(dineinButton, dineinButton, deliveryButton, tableButton); 
    }

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
     * @return true si la validación fue exitosa (sin errores), false si hubo errores
     */
    private boolean validateOrderRequirements() {
        MSGValidacion validacion = this.orderStrategy.validateOrderRequirements();

        // Si hay error, mostrar alerta
        if(!validacion.hasError()) {
            showAlert(validacion.fieldValues(), validacion.errorMesagge());
            return false;
        }

        return true; // Validación exitosa
    }
    
    @Override
    protected void setupAllButtons() {
        setupDeliveryButton();
        setupDineinButton();
        setupTableButton();
        setupSelectClientButton();
        setupMakeOrderButton();
        setupAddProductButton();
        setupDeleteProductoButton(false);
    }

    /**
     * Configura el botón para agregar o actualizar productos.
     */
    private void setupAddProductButton(){
        addProductButton.setOnAction(e -> handleAddOrUpdateProduct());
    }
    /*
    para mostrar el boton para eliminar
    */
    private void setupDeleteProductoButton(boolean show){
        deleteButton.setVisible(show);
        deleteButton.setManaged(show);
        deleteButton.setOnAction(e -> handleDeleteProduct());
    }

    /**
     * Establece la estrategia de orden y actualiza la UI.
     */
    private void setOrderStrategy(OrderSelectedInterface strategy) {
        this.orderStrategy = strategy;
        strategy.showOrderBar(true);
        strategy.barChanged();
        updateTotalLabel();
    }

    /**
     * Configura el botón de entrega a domicilio.
     */
    private void setupDeliveryButton(){
        deliveryButton.setOnAction(e -> {
            setOrderStrategy(new OrdersSelected.OrderDomicilioSelected(this));
            toggleGroupStyle(deliveryButton, dineinButton, deliveryButton, tableButton);
        });
    }

    /**
     * Configura el botón de comer en el lugar.
    */
   private void setupDineinButton(){
       dineinButton.setOnAction(e -> {
            setOrderStrategy(new OrdersSelected.OrderDineinSelected(this));
            toggleGroupStyle(dineinButton, dineinButton, deliveryButton, tableButton);
        });
    }

    /**
     * Configura el botón de compra por mesa.
     */
    private void setupTableButton() {
        tableButton.setOnAction(e -> {
            setOrderStrategy(new OrdersSelected.OrderTableSelected(this));
            toggleGroupStyle(tableButton, dineinButton, deliveryButton, tableButton);
        });
    }

    /**
     * Carga las mesas disponibles desde la base de datos en el ComboBox.
     */
    public void cargarMesasDisponibles() {
        try {
            List<ModeloMesa> mesas = mesaService.getMesasDisponibles();
            tableComboBox.getItems().clear();
            tableComboBox.getItems().addAll(mesas);

            // Configurar cómo se muestra cada mesa en la lista desplegable
            tableComboBox.setCellFactory(param -> new ListCell<ModeloMesa>() {
                @Override
                protected void updateItem(ModeloMesa item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumeroMesaDisplay() + " - " + item.getEstadoMesa());
                    }
                }
            });

            // Configurar cómo se muestra el item seleccionado en el botón
            tableComboBox.setButtonCell(new ListCell<ModeloMesa>() {
                @Override
                protected void updateItem(ModeloMesa item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumeroMesaDisplay());
                    }
                }
            });

            // Guardar ID cuando se selecciona una mesa
            tableComboBox.setOnAction(evt -> {
                ModeloMesa selected = tableComboBox.getValue();
                if (selected != null) {
                    mesaSeleccionadaId = selected.getIdMesa();
                }
            });

        } catch (Exception e) {
            showAlert("ERROR", "Error al cargar mesas", "No se pudieron cargar las mesas: " + e.getMessage());
        }
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
            confirmOrderController controller = loader.getController();
            controller.setOrderData(
                new ArrayList<>(orderItems),
                orderStrategy
            );
            
            controller.setParentController(this);
        }
    }

    // =============== TABLAS ===============
    /**
     * Establece los datos del cliente en los campos correspondientes.
     */
    public void setClientData(int clientId, String name, String address, String phoneNumber,double tarifaDomicilio) {
        clientNameField.setText(name);
        addressField.setText(address);
        phoneNumberField.setText(phoneNumber);
        tarifaDomicilioField.setText(String.valueOf(tarifaDomicilio));
        this.currentClientId = clientId;
    }

    public void setCostumerDeliveryName(String deliveryCustomerName){
        dineinNameField.setText(deliveryCustomerName);
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
                System.out.println("se esta eliminando en actualizar");
                addProductButton.setText("ACTUALIZAR");
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
            } else {
                deleteButton.setVisible(false);
                deleteButton.setManaged(false);
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
     * Actualiza el label del total usando la estrategia de orden actual.
     */
    private void updateTotalLabel() {
        double total = orderStrategy != null ?
            orderStrategy.calculateOrderTotal() :
            orderItems.stream().mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad()).sum();
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
        tableComboBox.setValue(null);
        productField.clear();
        dineinNameField.clear();
        quantityField.clear();
        priceField.clear();
        grandTotalLabel.setText("$0.00");
        toggleGroupStyle(dineinButton,dineinButton,deliveryButton,tableButton);
        currentClientId = 0;
        mesaSeleccionadaId = 0;
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