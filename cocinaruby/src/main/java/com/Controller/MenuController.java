package com.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.Model.Enum.MenuStyleConstants;
import com.Model.Enum.AnimationConstants;

public class MenuController extends BaseController {

    @FXML private BorderPane menuContainer;
    @FXML private ImageView exit,minimize;
    @FXML private Label menu,userNameLabel;
    @FXML private AnchorPane slider;
    @FXML private StackPane content;
    @FXML private StackPane sliderWrapper;
    @FXML private Button btnCorteCaja, ordersButton, salesButton, clientsButton, stopSalesButton;
    private Button currentActiveButton;
    private boolean stopSales = false;
    private double sidebarWidth;
    private boolean sidebarOpen = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFontSize();
        initializeController();
        loadView("/com/view/order.fxml", ordersButton);
    }

    /**
     * Carga una vista FXML en el contenedor principal.
     */
    private void loadView(String fxml, Button sourceButton) {
        try {
            URL resource = getClass().getResource(fxml);
            if (resource == null) return;
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (view instanceof javafx.scene.layout.Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            if (fxml.contains("order.fxml") && stopSales) {
                view.setDisable(true); 
            }
            content.getChildren().setAll(view);
            updateButtonStyle(sourceButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza el estilo del botón activo.
     */
    private void updateButtonStyle(Button activeBtn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("sidebar-item-active");
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-item-active");
            currentActiveButton = activeBtn;
        }
    }

    // =============== CONFIGURACIÓN INICIAL ===============
    /**
     * Configura las propiedades del slider.
     */
    private void setupSliderFunction() {
        sidebarWidth = slider.getPrefWidth();
        sliderWrapper.setMinWidth(0);
        sliderWrapper.setPrefWidth(sidebarWidth);
        sliderWrapper.setMaxWidth(sidebarWidth);
    }

    /**
     * Ajusta el tamaño de la fuente dinámicamente según el tamaño de la ventana.
     */
    public void setupFontSize() {
        menuContainer.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.heightProperty().addListener((o, oldH, newH) -> {
                    double size = newH.doubleValue() / MenuStyleConstants.FONT_SIZE_DIVISOR.getIntValue(); 
                    menuContainer.setStyle("-fx-font-size: " + size + "px;");
                });
            }
        });
    }

    /**
     * Configura los cursores para los elementos interactivos.
     */
    private void setupCursors() {
        exit.setCursor(Cursor.HAND);
        minimize.setCursor(Cursor.HAND);
        menu.setCursor(Cursor.HAND);
    }

    private void setupUserName(){

    }
    // =============== BOTONES ===============
    /**
     * Configura todos los botones del menú.
     */
    @Override
    protected void setupAllButtons() {
        setupExitButton();
        setupMinimizeButton();
        setupMenuFunction();
        setupOrdersButton();
        setupSalesButton();
        setupClientButton();
        setupStopSalesButton();
    }

    /**
     * Configura el botón de salida.
     */
    private void setupExitButton() {
        exit.fitWidthProperty().bind(menuContainer.widthProperty().multiply(0.025));
        exit.fitHeightProperty().bind(exit.fitWidthProperty());
        exit.setOnMouseEntered(e -> { exit.setScaleX(1.15); exit.setScaleY(1.15); });
        exit.setOnMouseExited(e -> { exit.setScaleX(1); exit.setScaleY(1); });
        exit.setOnMouseClicked(event -> System.exit(0));
    }

    /**
     * Configura el botón de minimizar.
     */
    private void setupMinimizeButton(){
        minimize.fitWidthProperty().bind(menuContainer.widthProperty().multiply(0.025));
        minimize.fitHeightProperty().bind(minimize.fitWidthProperty());
        minimize.setOnMouseEntered(e -> { minimize.setScaleX(1.15); minimize.setScaleY(1.15); });
        minimize.setOnMouseExited(e -> { minimize.setScaleX(1); minimize.setScaleY(1); });
        minimize.setOnMouseClicked(event -> {
            javafx.stage.Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setIconified(true);
        });
    }

    /**
     * Configura la animación del menú.
     */
    public void setupMenuFunction() {
        menu.setOnMouseClicked(e -> animateSlider());
    }

    /**
     * Configura el botón de detener ventas.
     */
    private void setupStopSalesButton(){
        stopSalesButton.setOnAction(e -> {
            stopSales = true;
            if (currentActiveButton == ordersButton) {
                loadView("/com/view/order.fxml", ordersButton);
            }
        });
    }
    
    /**
     * Configura el botón de órdenes.
     */
    private void setupOrdersButton(){
        ordersButton.setOnAction(e -> loadView("/com/view/order.fxml", ordersButton));
    }
    
    /**
     * Configura el botón de ventas.
     */
    private void setupSalesButton(){
        salesButton.setOnAction(e -> loadView("/com/view/sales.fxml", salesButton));
    }

    /**
     * Configura el botón de clientes.
     */
    private void setupClientButton(){
        clientsButton.setOnAction(e -> loadView("/com/view/clients.fxml", clientsButton));
    }

    // =============== ANIMACIONES ===============
    /**
     * Anima la apertura y cierre del slider lateral.
     */
    private void animateSlider() {
        if (sidebarOpen) {
            Timeline close = new Timeline(new KeyFrame(AnimationConstants.FADE_DURATION_MEDIUM.getDuration(),
                new KeyValue(slider.translateXProperty(), -slider.getWidth(), Interpolator.EASE_BOTH)));
            close.setOnFinished(e -> {
                menuContainer.setLeft(null);  
                slider.setTranslateX(0);
            });
            close.play();
        } else {
            menuContainer.setLeft(sliderWrapper);
            slider.setTranslateX(-slider.getWidth());
            Timeline open = new Timeline(new KeyFrame(AnimationConstants.FADE_DURATION_MEDIUM.getDuration(),
                new KeyValue(slider.translateXProperty(), 0, Interpolator.EASE_BOTH)));
            open.play();
        }
        sidebarOpen = !sidebarOpen;
    }

    @Override
    protected void setupAdditionalConfig() {
        setupSliderFunction();
        setupCursors();
    }
}
