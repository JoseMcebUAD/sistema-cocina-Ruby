package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
//Verificar Metodos
public class ResumeController implements Initializable  {

    @FXML 
    private Button ordersButton, dateSearchButton, searchButton;
    @FXML 
    private HBox dateBar, searchBar, grandTotalBar;
    @FXML 
    private DatePicker startDate, endDate;
    @FXML 
    private Label detailLabel, totalLabel;
    @FXML 
    private TableView<ObservableList<String>> OrderTable;
    @FXML 
    private TableView<ObservableList<String>> orderDetailTable;
    @FXML
    StackPane TableBar;
    @FXML 
    private BorderPane mainRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupVisibilityLogic();
        setupSelectionLogic();
        insertTestData(); // Datos de prueba
        toggleSearchMode(false);
        setupGlobalClickPolicy();
    }

    private void setupVisibilityLogic() {
        dateSearchButton.setOnAction(e -> toggleSearchMode(true));
        ordersButton.setOnAction(e -> toggleSearchMode(false));
    }
    private void showDetail(boolean visible) {
    // Animación para la tabla de detalle
    double startOpacity = visible ? 0.0 : 1.0;
    double endOpacity = visible ? 1.0 : 0.0;
    if (visible) {
        orderDetailTable.setVisible(true);
        orderDetailTable.setManaged(true);
        orderDetailTable.setOpacity(0); 
    }
    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
        javafx.util.Duration.millis(300), orderDetailTable
    );
    fade.setFromValue(startOpacity);
    fade.setToValue(endOpacity);
    fade.setOnFinished(e -> {
        if (!visible) {
            orderDetailTable.setVisible(false);
            orderDetailTable.setManaged(false);
        }
    });
    // Manejo de visibilidad de labels y total
    detailLabel.setVisible(!visible);
    detailLabel.setManaged(!visible);
    grandTotalBar.setVisible(!visible);
    grandTotalBar.setManaged(!visible);
    // Si estamos ocultando el detalle, aseguramos que los labels tengan opacidad 1
    if (!visible) {
        detailLabel.setOpacity(1.0);
        grandTotalBar.setOpacity(1.0);
    }
    fade.play();
    }

    private void setupSelectionLogic() {
        //Permite escuchar la fila seleccionada.
        OrderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            showDetail(newSelection != null);
        });
        TableBar.setOnMouseClicked(event -> {
            if (event.getTarget() == TableBar || event.getTarget() instanceof javafx.scene.shape.Shape) {
                OrderTable.getSelectionModel().clearSelection();
            }
        });
    }


    private void setupGlobalClickPolicy() {
    mainRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
        // Obtenemos el componente que recibió el clic
        Node clickedNode = (Node) event.getTarget();
        // Verificamos si el clic NO fue dentro de las tablas
        if (!isChildOfTable(clickedNode)) {
            OrderTable.getSelectionModel().clearSelection();
            orderDetailTable.getSelectionModel().clearSelection();
            // Esto disparará automáticamente tu listener de selección y ocultará el detalle
            }
        });
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

    private void toggleSearchMode(boolean showDates) {
    // Manejo de visibilidad y espacio
    dateBar.setVisible(showDates);
    dateBar.setManaged(showDates);
    searchBar.setVisible(showDates);
    searchBar.setManaged(showDates);

        if (showDates) {
        dateSearchButton.setStyle("-fx-background-color: #146886;"); 
        ordersButton.setStyle("-fx-background-color: #032d4d;");     
        } else {
        ordersButton.setStyle("-fx-background-color: #146886;");    
        dateSearchButton.setStyle("-fx-background-color: #032d4d;"); 
        }
    }

    //Datos de prueba
    private void insertTestData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        data.add(FXCollections.observableArrayList("101", "$150.00", "$174.00", "Boton"));
        data.add(FXCollections.observableArrayList("102", "$200.00", "$232.00", "Boton"));
        
        for (int i = 0; i < OrderTable.getColumns().size(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) OrderTable.getColumns().get(i);
            col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(colIndex)));
        }
        OrderTable.setItems(data);

        ObservableList<ObservableList<String>> detailData = FXCollections.observableArrayList();
        detailData.add(FXCollections.observableArrayList("Producto A", "2 unidades"));
        detailData.add(FXCollections.observableArrayList("Producto B", "1 unidad"));
        
        for (int i = 0; i < orderDetailTable.getColumns().size(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) orderDetailTable.getColumns().get(i);
            col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(colIndex)));
        }
        orderDetailTable.setItems(detailData);
    }
}



