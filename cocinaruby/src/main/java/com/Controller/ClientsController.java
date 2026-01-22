package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.Model.ModeloCliente;
import com.Service.ClientService;
import com.Model.Enum.ClientsUIConstants;
import com.Model.Enum.AnimationConstants;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
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


public class ClientsController extends BaseController {
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

    /**
     * Establece el controlador padre (OrderController).
     */
    public void setParentController(OrderController parent) {
        this.parentController = parent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeController();
    }

    /**
     * Configura validadores, filtros y listeners adicionales.
     */
    @Override
    protected void setupAdditionalConfig() {
        setupSearchFilter();
        setupGlobalClickConfig();
        setupPhoneValidator();
        loadDataFromService();
    }

    // =============== BÚSQUEDA Y FILTRADO ===============
    /**
     * Configura los listeners para filtrar por nombre y teléfono.
     */
    private void setupSearchFilter(){
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFromSelection) searchClients();
        });
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingFromSelection) searchClients();
        });
    }

    /**
     * Filtra los clientes según los valores de búsqueda.
     */
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

    // =============== OPERACIONES CRUD ===============
    /**
     * Agrega un nuevo cliente a la base de datos.
     * Valida que al menos el nombre tenga contenido.
     */
    private void handleAddClient() {
        // Obtener datos y validar que no estén vacíos
        String nombre = nameField.getText().trim();
        String direccion = addressField.getText().trim();
        String telefono = phoneField.getText().trim();
        
        // Validar que al menos el nombre esté completo
        if (nombre.isEmpty()) {
            showAlert("Error", ClientsUIConstants.MSG_NAME_REQUIRED.getValue());
            return;
        }
        
        // Crear el cliente
        ModeloCliente newClient = new ModeloCliente();
        newClient.setNombreCliente(nombre);
        newClient.setDirecciones(direccion);
        
        // Validar teléfono
        if (!telefono.isEmpty()) {
            String rawPhone = telefono.replaceAll("[^\\d]", "");
            newClient.setTelefono(rawPhone.isEmpty() ? "0" : rawPhone);
        } else {
            newClient.setTelefono("0");
        }
        
        // Guardar cliente
        ModeloCliente savedClient = clientService.addClient(newClient);
        if (savedClient != null) {
            masterData.add(savedClient);
            clearFields();
            showAlert("Guardado", ClientsUIConstants.MSG_CLIENT_ADDED.getValue());
        } else {
            showAlert("Aviso", ClientsUIConstants.MSG_CLIENT_EXISTS.getValue());
        }
    }

    /**
     * Actualiza un cliente existente.
     */
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
            showAlert("Actualización", ClientsUIConstants.MSG_CLIENT_UPDATED.getValue());
        }
    }

    /**
     * Elimina el cliente seleccionado.
     */
    private void handleDeleteClient(){
        ModeloCliente selectedClient = clienteTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) return;

        if (clientService.deleteClient(selectedClient)) {
            masterData.remove(selectedClient);
            clearFields();
            showAlert("Eliminado", ClientsUIConstants.MSG_CLIENT_DELETED.getValue());
        }
    }

    // =============== BOTONES ===============
    /**
     * Configura todos los botones del controlador.
     */
    @Override
    protected void setupAllButtons(){
        setupConfirmButton();
        setupAddButton();
        setupEditButton();
        setupDeleteButton();
        setupClearButton();
    }

    /**
     * Configura el botón de confirmación.
     */
    private void setupConfirmButton(){
        confirmButton.setOnAction(event -> {
            if (parentController != null) {
                // Obtener los datos del cliente seleccionado o vacíos si no hay selección
                String nombre = nameField.getText();
                String direccion = addressField.getText();
                String telefono = phoneField.getText();
                int clientId = 0;
                
                ModeloCliente selected = clienteTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    clientId = selected.getIdCliente(); 
                }
                
                // Pasar los datos tal como están, sin valores por defecto
                parentController.setClientData(clientId, nombre, direccion, telefono);
            }
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Configura el botón para agregar cliente.
     */
    private void setupAddButton(){
        addButton.setOnAction(event -> handleAddClient());
    }

    /**
     * Configura el botón para editar cliente.
     */
    private void setupEditButton(){
        editButton.setOnAction(e-> handleUpdateClient());
    }

    /**
     * Configura el botón para eliminar cliente.
     */
    private void setupDeleteButton(){
        deleteButton.setOnAction(e-> handleDeleteClient());
    }

    /**
     * Configura el botón para limpiar campos y filtros.
     */
    private void setupClearButton(){
        clearButton.setOnAction(e -> {
            isUpdatingFromSelection = true;
            clearFields();
            clienteTable.getSelectionModel().clearSelection();
            filteredData.setPredicate(client -> true);
            isUpdatingFromSelection = false;
        });
    }

    // =============== TABLAS ===============
    /**
     * Configura las columnas y propiedades de la tabla.
     */
    @Override
    protected void setupTableConfig(){
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

    /**
     * Configura el listener para cuando se selecciona un cliente en la tabla.
     */
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

    /**
     * Carga todos los clientes desde el servicio.
     */
    private void loadDataFromService() {
        masterData.setAll(clientService.getAllClients());
    }

    /**
     * Muestra la barra de confirmación cuando se utiliza como selector de clientes.
     */
    public void showConfirmBar() {
        if (confirmBar != null) {
            confirmBar.setVisible(true);
            confirmBar.setManaged(true);
        }
    }

    // =============== VALIDACIONES ===============
    /**
     * Configura el validador para el teléfono (solo dígitos, máximo 10).
     */
    private void setupPhoneValidator() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 10) {
                phoneField.setText(oldValue);
            }
        });
    }

    // =============== UTILIDADES ===============
    /**
     * Limpia todos los campos de entrada.
     */
    private void clearFields() {
        nameField.clear();
        addressField.clear();
        phoneField.clear();
    }

    /**
     * Obtiene la información de los campos y crea un objeto ModeloCliente.
     */
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

    /**
     * Configura los listeners globales del formulario.
     */
    private void setupGlobalClickConfig() {
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

    /**
     * Verifica si un nodo es hijo de un control de entrada.
     */
    private boolean isChildOfControl(Node node) {
        while (node != null) {
            if (node instanceof TextField || node instanceof Button) {
                return true;
            }
            node = node.getParent();
        }
        return false;
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
}

