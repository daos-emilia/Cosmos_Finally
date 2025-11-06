package com.consultorio.desktop.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionEstadisticasController {

    @FXML private ComboBox<String> cmbTema;
    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private DatePicker dpFechaInicio;
    @FXML private VBox vboxPeriodo;
    @FXML private VBox vboxFechaInicio;

    private String temaSeleccionado;
    private String periodoSeleccionado;
    private Map<String, Object> configuracion;
    private boolean generado = false;

    @FXML
    public void initialize() {
        System.out.println("✅ ConfiguracionEstadisticasController inicializado");
        configurarComboBoxes();
        configurarDatePicker();
    }

    private void configurarComboBoxes() {
        // Temas disponibles
        cmbTema.setItems(FXCollections.observableArrayList(
                "Nuevos Pacientes",
                "Nivel Educativo",
                "Condición",
                "Demanda Mensual"
        ));

        // Periodos disponibles
        cmbPeriodo.setItems(FXCollections.observableArrayList(
                "Un día",
                "Una semana",
                "Un mes",
                "Un semestre",
                "Un año"
        ));
    }

    private void configurarDatePicker() {
        // Configurar formato de fecha para evitar errores de parseo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dpFechaInicio.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (Exception e) {
                        System.out.println("⚠️ Error parseando fecha: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            }
        });

        // Establecer fecha por defecto como hoy
        dpFechaInicio.setValue(LocalDate.now());
    }

    @FXML
    private void handleTemaSeleccionado() {
        temaSeleccionado = cmbTema.getValue();
        System.out.println("📋 Tema seleccionado: " + temaSeleccionado);

        // Mostrar campos de periodo solo para "Nuevos Pacientes"
        if ("Nuevos Pacientes".equals(temaSeleccionado)) {
            vboxPeriodo.setVisible(true);
            vboxPeriodo.setManaged(true);
        } else {
            vboxPeriodo.setVisible(false);
            vboxPeriodo.setManaged(false);
            vboxFechaInicio.setVisible(false);
            vboxFechaInicio.setManaged(false);
        }

        // Ajustar tamaño de ventana
        ajustarTamanoVentana();
    }

    @FXML
    private void handlePeriodoSeleccionado() {
        periodoSeleccionado = cmbPeriodo.getValue();
        System.out.println("📅 Periodo seleccionado: " + periodoSeleccionado);

        // Mostrar campo de fecha de inicio
        if (periodoSeleccionado != null) {
            vboxFechaInicio.setVisible(true);
            vboxFechaInicio.setManaged(true);
        }

        // Ajustar tamaño de ventana
        ajustarTamanoVentana();
    }

    @FXML
    private void handleGenerar() {
        System.out.println("🔄 Generando gráfico: " + temaSeleccionado);

        // Validar tema
        if (temaSeleccionado == null || temaSeleccionado.isEmpty()) {
            mostrarAlerta("Validación", "Por favor seleccione un tema");
            return;
        }

        // Validar campos específicos para "Nuevos Pacientes"
        if ("Nuevos Pacientes".equals(temaSeleccionado)) {
            if (periodoSeleccionado == null || periodoSeleccionado.isEmpty()) {
                mostrarAlerta("Validación", "Por favor seleccione un periodo");
                return;
            }

            if (dpFechaInicio.getValue() == null) {
                mostrarAlerta("Validación", "Por favor seleccione una fecha de inicio");
                return;
            }

            // Validar que la fecha no sea futura
            if (dpFechaInicio.getValue().isAfter(LocalDate.now())) {
                mostrarAlerta("Validación", "La fecha de inicio no puede ser futura");
                return;
            }
        }

        // Guardar configuración
        configuracion = new HashMap<>();
        configuracion.put("tema", temaSeleccionado);
        configuracion.put("periodo", periodoSeleccionado);
        configuracion.put("fechaInicio", dpFechaInicio.getValue());

        generado = true;
        cerrarVentana();
    }

    @FXML
    private void handleCancelar() {
        generado = false;
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) cmbTema.getScene().getWindow();
        stage.close();
    }

    private void ajustarTamanoVentana() {
        // Ajustar el tamaño de la ventana según los campos visibles
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) cmbTema.getScene().getWindow();
            if (stage != null) {
                stage.sizeToScene();
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public boolean isGenerado() {
        return generado;
    }

    public Map<String, Object> getConfiguracion() {
        return configuracion;
    }
}

