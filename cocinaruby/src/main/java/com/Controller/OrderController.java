package com.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.Model.ModeloDetalleOrden;
import com.Model.Enum.TiposClienteEnum;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OrderController implements Initializable {
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
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpAllButtons();
        setUpTableConfig();
        setUpGlobalClickConfig();
        showClientBar(false);
        showTableBar(false);
        selectOrderType(dineinButton);
        setUpValidations();
    }
    //VALIDACIONES
    private boolean validateInputs() {
        if (productField.getText().isEmpty() || quantityField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showAlert("Campos Vacíos", "Por favor llene todos los campos del producto.");
            return false;
        }
        return true;
    }

    private boolean validateOrderRequirements() {
        if (orderItems.isEmpty()) {
            showAlert("Orden Vacía", "No puede realizar una orden sin productos.");
            return false;
        }
        if (currentOrderType.equals(TiposClienteEnum.PAGO_DOMICILIO)) {
            if (clientNameField.getText().trim().isEmpty() || 
                addressField.getText().trim().isEmpty() || 
                phoneNumberField.getText().trim().isEmpty()) {
                showAlert("Datos Incompletos", "Para Domicilio, debe llenar Nombre, Dirección y Teléfono.");
                return false;
            }
        } 
        else if (currentOrderType.equals(TiposClienteEnum.PAGO_MESA)) {
            if (tableNumberField.getText().trim().isEmpty()) {
                showAlert("Datos Incompletos", "Por favor ingrese el número de mesa.");
                return false;
            }
        }
        return true;
    }

    private void setUpValidations() {
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

    //BOTONES
    private void setUpAllButtons() {
        setUpDeliveryButton();
        setUpDineinButton();
        setUpTableButton();
        setUpselectClientButton();
        setUpMakeOrderButton();
        setUpAddProductoButton();
    }

    private void setUpAddProductoButton(){
        addProductButton.setOnAction(e -> handleAddOrUpdateProduct());
    }

    private void setUpDeliveryButton(){
    deliveryButton.setOnAction(e -> {
            showClientBar(true); 
            showTableBar(false);  
            selectOrderType(deliveryButton);
        });
    }

    private void setUpDineinButton(){
        dineinButton.setOnAction(e -> {
            showClientBar(false); 
            showTableBar(false);  
            selectOrderType(dineinButton);
        });
    }

    private void setUpTableButton() {
    tableButton.setOnAction(e -> {
            showClientBar(false); 
            showTableBar(true);   
            selectOrderType(tableButton);
        });
    }

    private void setUpselectClientButton(){
        selectClientButton.setOnAction(e -> openClientsView());
    }

    private void setUpMakeOrderButton(){
        makeOrderButton.setOnAction(e -> {
            if (validateOrderRequirements()) {
                openConfirmationView();
            }
        });
    }

    private void showClientBar(boolean show) {
        clientBar.setVisible(show);
        clientBar.setManaged(show);
        if (show) playFade(clientBar);
    }

    private void showTableBar(boolean show) {
        if (TableBar != null) {
            TableBar.setVisible(show);
            TableBar.setManaged(show);
            if (show) playFade(TableBar);
        }
    }

    private void playFade(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

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

    private void toggleGroupStyle(Button activeBtn, Button... group) {
        for (Button btn : group) {
            btn.getStyleClass().removeAll("order-button", "order-button-active");
            btn.getStyleClass().add(btn == activeBtn ? "order-button-active" : "order-button");
        }
    }

    //VENTANAS
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openClientsView() {
        showView("/com/view/clients.fxml", "Seleccionar Cliente", 1000, 600);
    }

    private void openConfirmationView() {
        showModal("/com/view/pop-up/order/confirmOrder.fxml", "Detalles de Pago", 539, 481);
    }

    private void showModal(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.TRANSPARENT, true);
    }

    private void showView(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.DECORATED, true);
    }

    private void createStage(String fxmlPath, String title, double width, double height, javafx.stage.StageStyle style, boolean isModal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

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

            Stage stage = new Stage();
            if (style == javafx.stage.StageStyle.TRANSPARENT) {
                stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                Scene scene = new Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                });
            } else {
                stage.initStyle(style);
                stage.setScene(new Scene(root));
            }
            if (isModal) {
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(mainRoot.getScene().getWindow());
            }
            stage.setTitle(title);
            stage.setWidth(width); 
            stage.setHeight(height);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TABLAS

    public void setClientData(int clientId, String name, String address, String phoneNumber) {
        clientNameField.setText(name);
        addressField.setText(address);
        phoneNumberField.setText(phoneNumber);
        this.currentClientId = clientId;
    }

    private void setUpTableConfig() {
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

    //LOGICA TABLA
// Este método decide si agrega uno nuevo o actualiza el seleccionado
    private void handleAddOrUpdateProduct() {
        if (validateInputs()) {
            if (productsTable.getSelectionModel().getSelectedItem() != null) {
                handleUpdateProduct(); // Si hay selección, actualiza
            } else {
                handleAddProduct();    // Si no, agrega nuevo
            }
        }
    }

    private void handleAddProduct() {
        ModeloDetalleOrden newItem = getTextFieldInfo();
        orderItems.add(newItem);
        updateTotalLabel();
        clearProductFields();
    }

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

    // Método auxiliar para obtener objeto desde campos
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
    
    // Método para eliminar (puedes vincularlo a una tecla DELETE o un botón nuevo)
    private void handleDeleteProduct() {
        ModeloDetalleOrden selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            orderItems.remove(selected);
            updateTotalLabel();
            clearProductFields();
        }
    }
    //UTILIDADES

    private void updateTotalLabel() {
        double total = orderItems.stream()
                // Multiplicar Precio x Cantidad
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad()) 
                .sum();
        grandTotalLabel.setText("$" + String.format("%.2f", total));
    }

    private void clearProductFields() {
        productField.clear();
        quantityField.clear();
        priceField.clear();
        productsTable.getSelectionModel().clearSelection();
    }

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

    private void setUpGlobalClickConfig() {
        mainRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node clickedNode = (Node) event.getTarget();
            if (!isChildOfTable(clickedNode) && !isChildOfControl(clickedNode)) {
                productsTable.getSelectionModel().clearSelection();
                clearProductFields();
            }
        });
    }

    private boolean isChildOfControl(Node node) {
        while (node != null) {
            if (node instanceof TextField || node instanceof Button) return true;
            node = node.getParent();
        }
        return false;
    }

    private boolean isChildOfTable(Node node) {
        while (node != null) {
            if (node instanceof TableView) return true;
            node = node.getParent();
        }
        return false;
    }
}