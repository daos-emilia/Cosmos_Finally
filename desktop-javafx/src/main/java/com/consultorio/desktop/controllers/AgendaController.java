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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AgendaController {

    @FXML private Label lblUsuario;

    // Labels para configuración horaria
    @FXML private Label lblDiasTrabajo;
    @FXML private Label lblTurnoManana;
    @FXML private Label lblTurnoTarde;
    @FXML private Label lblDuracionSesion;
    @FXML private Label lblTiempoDescanso;
    @FXML private Label lblUltimaActualizacion;

    private String usuarioActual;
    private String rolActual;
    private Stage previousStage;
    private boolean wasMaximized;

    @FXML
    public void initialize() {
        System.out.println("🔄 Inicializando AgendaController...");

        // Cargar configuración horaria actual
        cargarConfiguracionHoraria();
    }

    public void setUsuario(String usuario, String rol, Stage previousStage, boolean wasMaximized) {
        this.usuarioActual = usuario;
        this.rolActual = rol;
        this.previousStage = previousStage;
        this.wasMaximized = wasMaximized;
        lblUsuario.setText("Bienvenido/a, " + usuario + " (" + rol + ")");
    }

    private void cargarConfiguracionHoraria() {
        try {
            System.out.println("🔄 Cargando configuración horaria actual...");

            List<Map<String, Object>> configuraciones = HttpClientUtil.getConfiguracionHoraria();

            if (configuraciones == null || configuraciones.isEmpty()) {
                lblDiasTrabajo.setText("No configurado");
                lblTurnoManana.setText("No configurado");
                lblTurnoTarde.setText("No configurado");
                lblDuracionSesion.setText("No configurado");
                lblTiempoDescanso.setText("No configurado");
                lblUltimaActualizacion.setText("Sin datos");
                return;
            }

            // Obtener días activos
            List<String> diasActivos = configuraciones.stream()
                    .filter(config -> Boolean.TRUE.equals(config.get("activo")))
                    .map(config -> {
                        String dia = (String) config.get("diaSemana");
                        return dia.substring(0, 1).toUpperCase() + dia.substring(1).toLowerCase();
                    })
                    .collect(Collectors.toList());

            if (diasActivos.isEmpty()) {
                lblDiasTrabajo.setText("Ningún día activo");
            } else {
                lblDiasTrabajo.setText(String.join(", ", diasActivos));
            }

            // Obtener configuración del primer día (todos tienen la misma configuración de horarios)
            Map<String, Object> primeraConfig = configuraciones.get(0);

            // DEBUG: Imprimir todas las claves disponibles
            System.out.println("🔍 DEBUG - Claves disponibles en primeraConfig:");
            for (String key : primeraConfig.keySet()) {
                System.out.println("   - " + key + " = " + primeraConfig.get(key));
            }

            // Turno Mañana
            Boolean turnoManana = (Boolean) primeraConfig.get("turnoManana");
            if (Boolean.TRUE.equals(turnoManana)) {
                // El backend devuelve "horaInicioManana" y "horaFinManana"
                String inicioManana = (String) primeraConfig.get("horaInicioManana");
                String finManana = (String) primeraConfig.get("horaFinManana");
                System.out.println("🔍 DEBUG - Turno Mañana: horaInicioManana=" + inicioManana + ", horaFinManana=" + finManana);
                if (inicioManana != null && finManana != null) {
                    // Formatear para mostrar solo HH:mm (sin segundos)
                    String inicioFormateado = inicioManana.substring(0, 5);
                    String finFormateado = finManana.substring(0, 5);
                    lblTurnoManana.setText("Activo (" + inicioFormateado + " - " + finFormateado + ")");
                } else {
                    lblTurnoManana.setText("Activo (sin horarios definidos)");
                }
            } else {
                lblTurnoManana.setText("Inactivo");
            }

            // Turno Tarde
            Boolean turnoTarde = (Boolean) primeraConfig.get("turnoTarde");
            if (Boolean.TRUE.equals(turnoTarde)) {
                // El backend devuelve "horaInicioTarde" y "horaFinTarde"
                String inicioTarde = (String) primeraConfig.get("horaInicioTarde");
                String finTarde = (String) primeraConfig.get("horaFinTarde");
                System.out.println("🔍 DEBUG - Turno Tarde: horaInicioTarde=" + inicioTarde + ", horaFinTarde=" + finTarde);
                if (inicioTarde != null && finTarde != null) {
                    // Formatear para mostrar solo HH:mm (sin segundos)
                    String inicioFormateado = inicioTarde.substring(0, 5);
                    String finFormateado = finTarde.substring(0, 5);
                    lblTurnoTarde.setText("Activo (" + inicioFormateado + " - " + finFormateado + ")");
                } else {
                    lblTurnoTarde.setText("Activo (sin horarios definidos)");
                }
            } else {
                lblTurnoTarde.setText("Inactivo");
            }

            // Duración de sesión
            Integer duracionSesion = (Integer) primeraConfig.get("duracionSesion");
            if (duracionSesion != null) {
                lblDuracionSesion.setText(duracionSesion + " minutos");
            } else {
                lblDuracionSesion.setText("No configurado");
            }

            // Tiempo de descanso
            Integer tiempoDescanso = (Integer) primeraConfig.get("tiempoDescanso");
            if (tiempoDescanso != null) {
                lblTiempoDescanso.setText(tiempoDescanso + " minutos");
            } else {
                lblTiempoDescanso.setText("No configurado");
            }

            // Última actualización (buscar la más reciente)
            String ultimaActualizacion = null;
            String usuarioActualizacion = null;

            for (Map<String, Object> config : configuraciones) {
                String fechaActStr = (String) config.get("fechaActualizacion");
                System.out.println("🔍 DEBUG - Revisando config: fechaActualizacion=" + fechaActStr + ", actualizadoPor=" + config.get("actualizadoPor"));
                if (fechaActStr != null) {
                    if (ultimaActualizacion == null || fechaActStr.compareTo(ultimaActualizacion) > 0) {
                        ultimaActualizacion = fechaActStr;
                        usuarioActualizacion = (String) config.get("actualizadoPor");
                    }
                }
            }

            System.out.println("🔍 DEBUG - Última actualización encontrada: " + ultimaActualizacion + " por " + usuarioActualizacion);

            if (ultimaActualizacion != null) {
                try {
                    LocalDateTime fechaHora = LocalDateTime.parse(ultimaActualizacion);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String fechaFormateada = fechaHora.format(formatter);

                    if (usuarioActualizacion != null && !usuarioActualizacion.isEmpty()) {
                        lblUltimaActualizacion.setText(fechaFormateada + " por " + usuarioActualizacion);
                    } else {
                        lblUltimaActualizacion.setText(fechaFormateada);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error parseando fecha: " + e.getMessage());
                    lblUltimaActualizacion.setText(ultimaActualizacion);
                }
            } else {
                lblUltimaActualizacion.setText("Sin datos de actualización");
            }

            System.out.println("✅ Configuración horaria cargada correctamente");

        } catch (Exception e) {
            System.out.println("❌ Error cargando configuración horaria: " + e.getMessage());
            e.printStackTrace();
            lblDiasTrabajo.setText("Error al cargar");
            lblTurnoManana.setText("Error al cargar");
            lblTurnoTarde.setText("Error al cargar");
            lblDuracionSesion.setText("Error al cargar");
            lblTiempoDescanso.setText("Error al cargar");
            lblUltimaActualizacion.setText("Error al cargar");
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

            // Recargar configuración horaria después de guardar
            cargarConfiguracionHoraria();

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
