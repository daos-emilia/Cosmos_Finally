package com.consultorio.desktop.controllers;

import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AgendaController {

    @FXML private Label lblUsuario;
    @FXML private DatePicker datePicker;
    @FXML private ListView<String> listHorarios;
    @FXML private VBox panelDetalleTurno;
    @FXML private Label lblFechaSeleccionada;
    @FXML private Label lblHoraSeleccionada;
    @FXML private Label lblDuracion;
    @FXML private Label lblUbicacion;
    @FXML private Label lblEstadoTurno;

    private String usuarioActual;
    private String rolActual;
    private Stage previousStage;
    private boolean wasMaximized;

    @FXML
    public void initialize() {
        // Configurar DatePicker para mostrar el mes actual
        datePicker.setValue(LocalDate.now());

        // Configurar formato de fecha en español
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        // Ocultar panel de detalle inicialmente
        panelDetalleTurno.setVisible(false);

        // Configurar listener para cuando se selecciona una fecha
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            onFechaSeleccionada(newValue);
        });

        // Cargar horarios para hoy
        onFechaSeleccionada(LocalDate.now());
    }

    public void setUsuario(String usuario, String rol, Stage previousStage, boolean wasMaximized) {
        this.usuarioActual = usuario;
        this.rolActual = rol;
        this.previousStage = previousStage;
        this.wasMaximized = wasMaximized;
        lblUsuario.setText("Bienvenido/a, " + usuario + " (" + rol + ")");
    }

    private void onFechaSeleccionada(LocalDate fecha) {
        if (fecha == null) return;

        // Mostrar la fecha seleccionada
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaFormateada = fecha.format(formatter);
        fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
        lblFechaSeleccionada.setText("Fecha: " + fechaFormateada);

        // Cargar horarios disponibles desde el backend
        cargarHorariosDisponibles(fecha);

        // Ocultar detalle hasta que seleccionen un horario
        panelDetalleTurno.setVisible(false);
    }

    private void cargarHorariosDisponibles(LocalDate fecha) {
        listHorarios.getItems().clear();
        listHorarios.getItems().add("🔄 Cargando horarios disponibles...");

        try {
            System.out.println("🔄 Cargando horarios disponibles para: " + fecha);

            List<String> horarios = HttpClientUtil.getHorariosDisponibles(fecha);

            listHorarios.getItems().clear();

            if (horarios == null || horarios.isEmpty()) {
                listHorarios.getItems().add("❌ No hay horarios disponibles para esta fecha");
                System.out.println("📭 No hay horarios disponibles para: " + fecha);
                return;
            }

            // ✅ MEJORA: Formatear horarios con estilo
            for (String horario : horarios) {
                listHorarios.getItems().add("🕐 " + horario + " hs - Disponible");
            }

            System.out.println("✅ " + horarios.size() + " horarios cargados para: " + fecha);

            // Configurar selección de horario
            listHorarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.contains("Cargando") && !newValue.contains("No hay")) {
                    // Extraer solo la hora del formato "🕐 HH:mm hs - Disponible"
                    String horarioLimpio = newValue.replace("🕐 ", "").replace(" hs - Disponible", "").trim();
                    mostrarDetalleTurno(horarioLimpio);
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Error cargando horarios: " + e.getMessage());
            e.printStackTrace();
            listHorarios.getItems().clear();
            listHorarios.getItems().add("❌ Error conectando con el servidor");
        }
    }

    private void mostrarDetalleTurno(String horario) {
        System.out.println("📋 Mostrando detalle para horario: " + horario);

        panelDetalleTurno.setVisible(true);
        lblHoraSeleccionada.setText("Horario: " + horario + " hs");
        lblDuracion.setText("Duración: 40 minutos");
        lblUbicacion.setText("Ubicación: Consultorio Principal");

        // ✅ MEJORA: Mostrar que es solo vista (no se puede pedir turno desde desktop)
        if (lblEstadoTurno != null) {
            lblEstadoTurno.setText("Estado: Disponible (Solo lectura)");
            lblEstadoTurno.setStyle("-fx-text-fill: #7FBBB2; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleConfiguracion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/configuracion-horarios.fxml"));
            Parent root = loader.load();

            ConfiguracionHorariosController controller = loader.getController();
            controller.setUsuario(usuarioActual, rolActual);

            Stage stage = new Stage();
            stage.setTitle("Configuración de Horarios - Cosmos");

            // ✅ CORRECCIÓN: CREAR SCENE PRIMERO
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Cargar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.showAndWait();

            // Recargar horarios después de guardar configuración
            onFechaSeleccionada(datePicker.getValue());

        } catch (IOException e) {
            mostrarAlertaError("Error", "No se pudo abrir la configuración", e.getMessage());
        }
    }

    @FXML
    private void handleVolverDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUsuario(usuarioActual, rolActual, null);

            Stage stage = new Stage();
            stage.setTitle("Cosmos - Dashboard");

            // ✅ CORRECCIÓN: CREAR SCENE PRIMERO, LUEGO APLICAR CSS
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);

            // Usar el mismo tamaño que tenía antes
            if (wasMaximized) {
                stage.setMaximized(true);
            }

            // Cargar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) lblUsuario.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            mostrarAlertaError("Error", "No se pudo volver al dashboard", e.getMessage());
        }
    }

    private void mostrarAlertaError(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
