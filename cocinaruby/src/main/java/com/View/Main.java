package com.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.AppRunner;
import util.UsuarioTokenSistema;

/**
 * Clase Main para iniciar la aplicación Cocina Ruby
 * Usa AppRunner para inicializar la base de datos y utilidades del sistema
 */
public class Main extends Application {

    private AppRunner app;
    private UsuarioTokenSistema tokenSistema;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root= FXMLLoader.load(getClass().getResource("/com/view/menu.fxml"));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        //Si vas a cargar el login quita setMaximized(true); y setResizable(false);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        
        // Guardar timestamp de cierre de sesión
        System.out.println("cierre sesión 1");
        if (app != null) {
            System.out.println("cierre sesión 2");
            try {
                app.setTokenUsuario();
                System.out.println("✓ Timestamp de cierre guardado");
            } catch (Exception e) {
                System.err.println("⚠ Error al guardar timestamp: " + e.getMessage());
            }
            
            // Detener monitor de impresora si está activo
            app.stopPrinterMonitor();
            app.closeDatabase();
        }
        super.stop();
        System.out.println("✓ Aplicación cerrada correctamente");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
