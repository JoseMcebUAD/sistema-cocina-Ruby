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

public class SalesController implements Initializable  {
    @FXML 
    private Button ordersButton, dateSearchButton;
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
        setUpSelectionLogic();
        insertTestData(); // Datos de prueba
        setUpSearchDates(false);
        setUpGlobalClickConfig();
    }

    private void setupVisibilityLogic() {
        dateSearchButton.setOnAction(e -> setUpSearchDates(true));
        ordersButton.setOnAction(e -> setUpSearchDates(false));
    }

    private void showDetail(boolean visible) {
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
    detailLabel.setVisible(!visible);
    detailLabel.setManaged(!visible);
    grandTotalBar.setVisible(!visible);
    grandTotalBar.setManaged(!visible);
    if (!visible) {
        detailLabel.setOpacity(1.0);
        grandTotalBar.setOpacity(1.0);
    }
    fade.play();
    }

    private void setUpSelectionLogic() {
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


    private void setUpGlobalClickConfig() {
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

    private void setUpSearchDates(boolean showDates) {
        dateBar.setVisible(showDates);
        dateBar.setManaged(showDates);
        updateTabStyles(showDates);
    }

    private void updateTabStyles(boolean showDates) {
        dateSearchButton.getStyleClass().removeAll("sales-tab-button", "sales-tab-active");
        ordersButton.getStyleClass().removeAll("sales-tab-button", "sales-tab-active");
        if (showDates) {
            dateSearchButton.getStyleClass().add("sales-tab-active");
            ordersButton.getStyleClass().add("sales-tab-button");
        } else {
            ordersButton.getStyleClass().add("sales-tab-active");
            dateSearchButton.getStyleClass().add("sales-tab-button");
        }
    }


    private void insertTestData() {
    // --- TABLA PRINCIPAL ---
    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
    // Agregamos un 4to elemento para que coincida con tus 4 columnas (Orden, Total, Fecha, Estado)
    data.add(FXCollections.observableArrayList("101", "$174.00", "2024-05-20", "Impreso","Imprimir"));
    data.add(FXCollections.observableArrayList("102", "$232.00", "2024-05-20", "No impreso","Imprimir"));
    
    for (int i = 0; i < OrderTable.getColumns().size(); i++) {
        final int colIndex = i;
        TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) OrderTable.getColumns().get(i);
        col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(colIndex)));
    }
    OrderTable.setItems(data);

    ObservableList<ObservableList<String>> detailData = FXCollections.observableArrayList();
    detailData.add(FXCollections.observableArrayList("Producto A", "2 unidades", "$50.00"));
    detailData.add(FXCollections.observableArrayList("Producto B", "1 unidad", "$100.00"));
    
    for (int i = 0; i < orderDetailTable.getColumns().size(); i++) {
        final int colIndex = i;
        TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) orderDetailTable.getColumns().get(i);
        col.setCellValueFactory(param -> {
            if (param.getValue().size() > colIndex) {
                return new javafx.beans.property.SimpleStringProperty(param.getValue().get(colIndex));
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
    }
    orderDetailTable.setItems(detailData);
}
}



