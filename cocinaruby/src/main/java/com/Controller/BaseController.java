package com.Controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase base abstracta para todos los controladores de la aplicación.
 * Define métodos comunes y proporciona una estructura uniforme.
 */
public abstract class BaseController implements Initializable {

    /**
     * Método de plantilla que coordina la inicialización del controlador.
     * Las subclases pueden sobrescribir los pasos específicos.
     */
    protected final void initializeController() {
        setupTableConfig();
        setupAllButtons();
        setupAdditionalConfig();
    }

    /**
     * Configura la tabla (si aplica). Las subclases sobrescriben si no aplica.
     */
    protected void setupTableConfig() {

    }

    /**
     * Configura todos los botones del controlador.
     * Las subclases deben implementar su lógica específica.
     */
    protected abstract void setupAllButtons();

    protected void setupAdditionalConfig() {

    }

    /**
     * Método unificado para mostrar alertas.
     * 
     * @param type Tipo de alerta: "ERROR", "SUCCESS", "WARNING", "INFO"
     * @param title Título de la alerta
     * @param content Contenido del mensaje
     */
    protected final void showAlert(String type, String title, String content) {
        AlertType alertType = AlertType.INFORMATION;
        
        switch (type.toUpperCase()) {
            case "ERROR":
                alertType = AlertType.ERROR;
                break;
            case "WARNING":
                alertType = AlertType.WARNING;
                break;
            case "SUCCESS":
                alertType = AlertType.INFORMATION;
                break;
            case "INFO":
            default:
                alertType = AlertType.INFORMATION;
                break;
        }

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Agregar icono a la alerta
        try {
            String iconPath = "/icons/app.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Si hay error al cargar el icono, continuar sin él
        }
        
        alert.showAndWait();
    }

    /**
     * Versión simplificada de showAlert con solo 2 parámetros.
     * Utiliza el tipo INFO por defecto.
     * 
     * @param title Título de la alerta
     * @param content Contenido del mensaje
     */
    protected final void showAlert(String title, String content) {
        showAlert("INFO", title, content);
    }

    // =============== MANEJO DE VENTANAS ===============
    /**
     * Crea y muestra un modal (ventana con estilo transparente).
     * 
     * @param fxmlPath Ruta al archivo FXML
     * @param title Título de la ventana
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     */
    protected final void showModal(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.TRANSPARENT, true);
    }

    /**
     * Crea y muestra una vista (ventana con estilo decorado).
     * 
     * @param fxmlPath Ruta al archivo FXML
     * @param title Título de la ventana
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     */
    protected final void showView(String fxmlPath, String title, double width, double height) {
        createStage(fxmlPath, title, width, height, javafx.stage.StageStyle.DECORATED, true);
    }

    /**
     * Método genérico para crear y configurar ventanas.
     * Puede mostrar diálogos modales o vistas normales con diferentes estilos.
     * 
     * @param fxmlPath Ruta al archivo FXML
     * @param title Título de la ventana
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @param style Estilo de la ventana (TRANSPARENT, DECORATED, etc.)
     * @param isModal Si es true, la ventana será modal
     */
    protected final void createStage(String fxmlPath, String title, double width, double height, 
                                     javafx.stage.StageStyle style, boolean isModal) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            // Hook para que los subclases configuren el controlador cargado si es necesario
            configureLoadedController(fxmlPath, loader);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            if (style == javafx.stage.StageStyle.TRANSPARENT) {
                stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                setupDraggableWindow(stage, root);
            } else {
                stage.initStyle(style);
                stage.setScene(new javafx.scene.Scene(root));
            }
            if (isModal) {
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            }
            stage.setTitle(title);
            stage.setWidth(width); 
            stage.setHeight(height);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al crear stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hook para que los subclases configuren el controlador de la ventana cargada.
     * Se invoca después de cargar el FXML pero antes de mostrar la ventana.
     * 
     * @param fxmlPath Ruta del FXML cargado
     * @param loader FXMLLoader con el controlador ya cargado
     */
    protected void configureLoadedController(String fxmlPath, javafx.fxml.FXMLLoader loader) {
        // Implementación vacía - los subclases pueden sobrescribir si necesitan
    }

    /**
     * Configura una ventana para que sea arrastrable.
     * Se usa para ventanas con estilo TRANSPARENT.
     */
    private void setupDraggableWindow(javafx.stage.Stage stage, javafx.scene.Parent root) {
        final double[] offset = {0, 0};
        root.setOnMousePressed(event -> {
            offset[0] = event.getSceneX();
            offset[1] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offset[0]);
            stage.setY(event.getScreenY() - offset[1]);
        });
    }
}
