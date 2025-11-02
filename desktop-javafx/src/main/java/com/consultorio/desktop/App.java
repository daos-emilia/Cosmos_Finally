package com.consultorio.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        try {
            // Cargar login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/login.fxml"));
            Parent root = loader.load();

            scene = new Scene(root, 800, 600);

            // Intentar cargar CSS
            try {
                scene.getStylesheets().add(getClass().getResource("/com/consultorio/desktop/styles/styles.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setTitle("Cosmos - Consultorio Psicopedagógico");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // Si no encuentra el FXML, mostrar pantalla de error simple
            System.err.println("Error cargando login.fxml: " + e.getMessage());
            showErrorScreen(stage, "No se pudo cargar la interfaz: " + e.getMessage());
        }
    }

    private void showErrorScreen(Stage stage, String message) {
        VBox errorLayout = new VBox(10);
        errorLayout.getChildren().addAll(
                new Label("❌ Error en la aplicación"),
                new Label(message),
                new Label("Verifica que los archivos FXML estén en la carpeta correcta.")
        );

        Scene errorScene = new Scene(errorLayout, 600, 200);
        stage.setScene(errorScene);
        stage.setTitle("Error - Cosmos");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
