package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.Model.ModeloCliente;
import com.Service.ClientService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class ClientsController implements Initializable {
    @FXML
    private Button addButton, deleteButton, editButton, confirmButton, clearButton;
    @FXML
    private TextField nameField, phoneField, addressField;
    @FXML
    private TableView<ModeloCliente> clienteTable;
    @FXML
    private HBox confirmBar;
    @FXML
    private BorderPane mainRoot;

    private ClientService clientService = new ClientService();
    private ObservableList<ModeloCliente> masterData = FXCollections.observableArrayList();
    private FilteredList<ModeloCliente> filteredData;
    private OrderController parentController;
    private boolean isUpdatingFromSelection = false;

    public void setParentController(OrderController parent) {
        this.parentController = parent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTableConfig();
        loadDataFromService();
        setUpAllButton();
        setupSearchFilter();
        setUpGlobalClickConfig();
        setUpPhoneValidator();
    }

    private void setupSearchFilter(){
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFromSelection) searchClients();
        });
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFromSelection) searchClients();
        });
    }

    private void handleAddClient() {
        ModeloCliente newClient = getTextFieldInfo();
        if (newClient.getNombreCliente().isEmpty()) {
            showAlert("Error", "El nombre es obligatorio.");
            return;
        }
        ModeloCliente savedClient = clientService.addClient(newClient);
            if (savedClient != null) {
                masterData.add(savedClient);
                clearFields();
                showAlert("Guardado", "El cliente se ha agregado con exito");
            } else {
                showAlert("Aviso", "No se pudo agregar: El cliente ya existe o hubo un error de conexión.");
            }
    }

    private void handleUpdateClient(){
        ModeloCliente selectedClient = clienteTable.getSelectionModel().getSelectedItem();
        if(selectedClient==null){
        showAlert("Atención", "Por favor, selecciona un cliente de la tabla para editar.");
        return;
        }
        selectedClient.setNombreCliente(nameField.getText());
        selectedClient.setDirecciones(addressField.getText());
        selectedClient.setTelefono(phoneField.getText());

        if(clientService.updateClient(selectedClient)){
        clienteTable.refresh(); 
        clearFields();
        showAlert("Actualizacion","Cliente actualizado correctamente.");
        }
    }

    private void handleDeleteCLient(){
        ModeloCliente SelectedClient = clienteTable.getSelectionModel().getSelectedItem();
        if (SelectedClient == null) return;

        if (clientService.deleteClient(SelectedClient)) {
            masterData.remove(SelectedClient);
            clearFields();
            showAlert("Eliminado", "Cliente eliminado correctamente.");
        }
    }

    private void showAlert(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void clearFields() {
        nameField.clear();
        addressField.clear();
        phoneField.clear();
    }

    private ModeloCliente getTextFieldInfo(){
        ModeloCliente client = new ModeloCliente();
        client.setNombreCliente(nameField.getText());
        client.setDirecciones(addressField.getText());
        String rawPhone = phoneField.getText().replaceAll("[^\\d]", "");
        if (rawPhone.isEmpty()) {
            client.setTelefono("0");
        } else {
            client.setTelefono(rawPhone);
        }
            return client;
    }

    private void setUpPhoneValidator() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 10) {
                phoneField.setText(oldValue);
            }
        });
    }

    private void setUpTableConfig(){
    ObservableList<TableColumn<ModeloCliente, ?>> columns = clienteTable.getColumns();
    if (columns.size() >= 3) {
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("direcciones"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<>("telefono"));
    }
    filteredData = new FilteredList<>(masterData, p -> true);
    clienteTable.setItems(filteredData);
    setupTableSelectionListener();
    }

    private void setupTableSelectionListener() {
        clienteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            isUpdatingFromSelection = true;
            if (newSelection != null) {
                nameField.setText(newSelection.getNombreCliente());
                addressField.setText(newSelection.getDirecciones());
                phoneField.setText(newSelection.getTelefono());
            } else {
                clearFields();
            }
            isUpdatingFromSelection = false;
        });
    }

    private void loadDataFromService() {
        masterData.setAll(clientService.getAllClients());
    }

    public void showConfirmBar() {
        if (confirmBar != null) {
            confirmBar.setVisible(true);
            confirmBar.setManaged(true);
        }
    }

    private void setUpAllButton(){
        setUpConfirmButton();
        setUpAddButton();
        setUpEditButton();
        setUpDeleteButton();
        setUpClearButton();
    }


    private void setUpConfirmButton(){
        confirmButton.setOnAction(event -> {
            if (parentController != null) {
                String nombreTest = nameField.getText().isEmpty() ? "Cliente de Prueba" : nameField.getText();
                String dirTest = addressField.getText().isEmpty() ? "Calle Falsa 123" : addressField.getText();
                String telTest = phoneField.getText().isEmpty() ? "555-0123" : phoneField.getText();
                parentController.setClientData(nombreTest, dirTest, telTest);
            }
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }

    private void setUpAddButton(){
        addButton.setOnAction(event -> handleAddClient());
    }

    private void setUpEditButton(){
        editButton.setOnAction(e-> handleUpdateClient());
    }

    private void setUpDeleteButton(){
        deleteButton.setOnAction(e-> handleDeleteCLient());
    }

    private void setUpClearButton(){
        clearButton.setOnAction(e -> {
            isUpdatingFromSelection = true;
            clearFields();
            clienteTable.getSelectionModel().clearSelection();
            filteredData.setPredicate(client -> true);
            isUpdatingFromSelection = false;
        });
    }

    private void setUpGlobalClickConfig() {
        mainRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
        Node clickedNode = (Node) event.getTarget();
        boolean isTable = isChildOfTable(clickedNode);
        boolean isControl = isChildOfControl(clickedNode);
        if (!isTable && !isControl) {
            clienteTable.getSelectionModel().clearSelection();
            clearFields(); 
        }
    });
    }

    private boolean isChildOfControl(Node node) {
        while (node != null) {
            if (node instanceof TextField || node instanceof Button) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private boolean isChildOfTable(Node node) {
    while (node != null) {
        if (node instanceof TableView) {
            return true;
            }
        node = node.getParent();
        }
    return false;
    }

    private void searchClients(){
        String nameFilter = nameField.getText().toLowerCase();
        String phoneFilter = phoneField.getText();
        filteredData.setPredicate(client -> {
            if ((nameFilter == null || nameFilter.isEmpty()) && 
                (phoneFilter == null || phoneFilter.isEmpty())) {
                return true;
            }
            boolean matchName = false;
            if (nameFilter != null && !nameFilter.isEmpty()) {
                if (client.getNombreCliente().toLowerCase().contains(nameFilter)) {
                    matchName = true;
                }
            } else {
                matchName = true;
            }
            boolean matchPhone = false;
            if (phoneFilter != null && !phoneFilter.isEmpty()) {
                if (client.getTelefono().contains(phoneFilter)) {
                    matchPhone = true;
                }
            } else {
                matchPhone = true; 
            }
            return matchName && matchPhone;
        });
    }

        

}

