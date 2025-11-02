package com.consultorio.desktop.controllers;

import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfiguracionHorariosController {

    @FXML private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;
    @FXML private CheckBox cbManana, cbTarde;
    @FXML private Spinner<Integer> spMananaInicioHora, spMananaInicioMinuto;
    @FXML private Spinner<Integer> spMananaFinHora, spMananaFinMinuto;
    @FXML private Spinner<Integer> spTardeInicioHora, spTardeInicioMinuto;
    @FXML private Spinner<Integer> spTardeFinHora, spTardeFinMinuto;
    @FXML private Spinner<Integer> spDuracionSesion;
    @FXML private Spinner<Integer> spTiempoDescanso;
    @FXML private Button btnGuardar, btnCancelar;

    private String usuarioActual;
    private String rolActual;
    private List<Map<String, Object>> configuracionesActuales;

    @FXML
    public void initialize() {
        configurarSpinners();
        configurarEventos();
        cargarConfiguracionActual();
    }

    public void setUsuario(String usuario, String rol) {
        this.usuarioActual = usuario;
        this.rolActual = rol;
    }

    private void configurarSpinners() {
        spMananaInicioHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 11, 8));
        spMananaInicioMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spMananaFinHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 12, 12));
        spMananaFinMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spTardeInicioHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(13, 17, 15));
        spTardeInicioMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spTardeFinHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(14, 20, 19));
        spTardeFinMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spDuracionSesion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 120, 40));
        spTiempoDescanso.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 10));

        formatearSpinners();
    }

    private void formatearSpinners() {
        configurarFormatoMinutos(spMananaInicioMinuto);
        configurarFormatoMinutos(spMananaFinMinuto);
        configurarFormatoMinutos(spTardeInicioMinuto);
        configurarFormatoMinutos(spTardeFinMinuto);
    }

    private void configurarFormatoMinutos(Spinner<Integer> spinner) {
        spinner.getValueFactory().setConverter(new javafx.util.converter.IntegerStringConverter() {
            @Override
            public String toString(Integer value) {
                return value == null ? "00" : String.format("%02d", value);
            }

            @Override
            public Integer fromString(String value) {
                if (value == null || value.trim().isEmpty()) return 0;
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });
    }

    private void configurarEventos() {
        cbManana.selectedProperty().addListener((obs, old, nuevo) -> {
            boolean enabled = nuevo != null && nuevo;
            spMananaInicioHora.setDisable(!enabled);
            spMananaInicioMinuto.setDisable(!enabled);
            spMananaFinHora.setDisable(!enabled);
            spMananaFinMinuto.setDisable(!enabled);
        });

        cbTarde.selectedProperty().addListener((obs, old, nuevo) -> {
            boolean enabled = nuevo != null && nuevo;
            spTardeInicioHora.setDisable(!enabled);
            spTardeInicioMinuto.setDisable(!enabled);
            spTardeFinHora.setDisable(!enabled);
            spTardeFinMinuto.setDisable(!enabled);
        });

        spMananaInicioHora.setDisable(true);
        spMananaInicioMinuto.setDisable(true);
        spMananaFinHora.setDisable(true);
        spMananaFinMinuto.setDisable(true);
        spTardeInicioHora.setDisable(true);
        spTardeInicioMinuto.setDisable(true);
        spTardeFinHora.setDisable(true);
        spTardeFinMinuto.setDisable(true);
    }

    private void cargarConfiguracionActual() {
        try {
            configuracionesActuales = HttpClientUtil.getConfiguracionHoraria();

            if (configuracionesActuales != null && !configuracionesActuales.isEmpty()) {
                Map<String, Object> primeraConfig = configuracionesActuales.get(0);

                if (primeraConfig.containsKey("horaInicioManana") && primeraConfig.get("horaInicioManana") != null) {
                    LocalTime time = LocalTime.parse((String) primeraConfig.get("horaInicioManana"));
                    spMananaInicioHora.getValueFactory().setValue(time.getHour());
                    spMananaInicioMinuto.getValueFactory().setValue(time.getMinute());
                }

                if (primeraConfig.containsKey("horaFinManana") && primeraConfig.get("horaFinManana") != null) {
                    LocalTime time = LocalTime.parse((String) primeraConfig.get("horaFinManana"));
                    spMananaFinHora.getValueFactory().setValue(time.getHour());
                    spMananaFinMinuto.getValueFactory().setValue(time.getMinute());
                }

                if (primeraConfig.containsKey("horaInicioTarde") && primeraConfig.get("horaInicioTarde") != null) {
                    LocalTime time = LocalTime.parse((String) primeraConfig.get("horaInicioTarde"));
                    spTardeInicioHora.getValueFactory().setValue(time.getHour());
                    spTardeInicioMinuto.getValueFactory().setValue(time.getMinute());
                }

                if (primeraConfig.containsKey("horaFinTarde") && primeraConfig.get("horaFinTarde") != null) {
                    LocalTime time = LocalTime.parse((String) primeraConfig.get("horaFinTarde"));
                    spTardeFinHora.getValueFactory().setValue(time.getHour());
                    spTardeFinMinuto.getValueFactory().setValue(time.getMinute());
                }

                if (primeraConfig.containsKey("duracionSesion")) {
                    spDuracionSesion.getValueFactory().setValue((Integer) primeraConfig.get("duracionSesion"));
                }

                if (primeraConfig.containsKey("tiempoDescanso")) {
                    spTiempoDescanso.getValueFactory().setValue((Integer) primeraConfig.get("tiempoDescanso"));
                }

                for (Map<String, Object> config : configuracionesActuales) {
                    String diaSemana = (String) config.get("diaSemana");
                    Boolean activo = (Boolean) config.get("activo");
                    Boolean turnoManana = (Boolean) config.get("turnoManana");
                    Boolean turnoTarde = (Boolean) config.get("turnoTarde");

                    switch (diaSemana) {
                        case "LUNES": cbLunes.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "MARTES": cbMartes.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "MIERCOLES": cbMiercoles.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "JUEVES": cbJueves.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "VIERNES": cbViernes.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "SABADO": cbSabado.setSelected(Boolean.TRUE.equals(activo)); break;
                        case "DOMINGO": cbDomingo.setSelected(Boolean.TRUE.equals(activo)); break;
                    }

                    if (turnoManana != null) cbManana.setSelected(turnoManana);
                    if (turnoTarde != null) cbTarde.setSelected(turnoTarde);
                }
            } else {
                cargarConfiguracionPorDefecto();
            }

        } catch (Exception e) {
            System.out.println("❌ Error cargando configuración: " + e.getMessage());
            cargarConfiguracionPorDefecto();
        }
    }

    private void cargarConfiguracionPorDefecto() {
        cbLunes.setSelected(true);
        cbMartes.setSelected(true);
        cbMiercoles.setSelected(true);
        cbJueves.setSelected(true);
        cbViernes.setSelected(true);
        cbSabado.setSelected(false);
        cbDomingo.setSelected(false);
        cbManana.setSelected(true);
        cbTarde.setSelected(true);
    }

    @FXML
    private void handleGuardar() {
        try {
            System.out.println("\n💾 ===== INICIANDO GUARDADO DE CONFIGURACIÓN =====");

            if (!validarHorarios()) return;

            LocalTime horaInicioManana = LocalTime.of(spMananaInicioHora.getValue(), spMananaInicioMinuto.getValue());
            LocalTime horaFinManana = LocalTime.of(spMananaFinHora.getValue(), spMananaFinMinuto.getValue());
            LocalTime horaInicioTarde = LocalTime.of(spTardeInicioHora.getValue(), spTardeInicioMinuto.getValue());
            LocalTime horaFinTarde = LocalTime.of(spTardeFinHora.getValue(), spTardeFinMinuto.getValue());

            int duracionSesion = spDuracionSesion.getValue();
            int tiempoDescanso = spTiempoDescanso.getValue();

            List<Map<String, Object>> configuraciones = new ArrayList<>();

            CheckBox[] checkboxes = {cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo};
            String[] diasSemana = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"};

            for (int i = 0; i < diasSemana.length; i++) {
                Map<String, Object> config = new HashMap<>();
                config.put("diaSemana", diasSemana[i]);
                config.put("activo", checkboxes[i].isSelected());
                config.put("turnoManana", cbManana.isSelected());
                config.put("turnoTarde", cbTarde.isSelected());
                config.put("horaInicioManana", horaInicioManana.toString());
                config.put("horaFinManana", horaFinManana.toString());
                config.put("horaInicioTarde", horaInicioTarde.toString());
                config.put("horaFinTarde", horaFinTarde.toString());
                config.put("duracionSesion", duracionSesion);
                config.put("tiempoDescanso", tiempoDescanso);
                configuraciones.add(config);
            }

            boolean exito = HttpClientUtil.actualizarConfiguracionHoraria(configuraciones, usuarioActual);

            if (exito) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText("Configuración guardada");
                alert.setContentText("La configuración horaria se guardó correctamente.");
                alert.showAndWait();

                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al guardar");
                alert.setContentText("No se pudo guardar la configuración.");
                alert.showAndWait();
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error inesperado");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean validarHorarios() {
        if (!cbManana.isSelected() && !cbTarde.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validación");
            alert.setHeaderText("Turnos requeridos");
            alert.setContentText("Debe seleccionar al menos un turno (Mañana o Tarde).");
            alert.showAndWait();
            return false;
        }

        if (cbManana.isSelected()) {
            LocalTime inicio = LocalTime.of(spMananaInicioHora.getValue(), spMananaInicioMinuto.getValue());
            LocalTime fin = LocalTime.of(spMananaFinHora.getValue(), spMananaFinMinuto.getValue());

            if (inicio.isAfter(fin) || inicio.equals(fin)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validación");
                alert.setHeaderText("Horario inválido");
                alert.setContentText("El horario de inicio de la mañana debe ser anterior al horario de fin.");
                alert.showAndWait();
                return false;
            }
        }

        if (cbTarde.isSelected()) {
            LocalTime inicio = LocalTime.of(spTardeInicioHora.getValue(), spTardeInicioMinuto.getValue());
            LocalTime fin = LocalTime.of(spTardeFinHora.getValue(), spTardeFinMinuto.getValue());

            if (inicio.isAfter(fin) || inicio.equals(fin)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validación");
                alert.setHeaderText("Horario inválido");
                alert.setContentText("El horario de inicio de la tarde debe ser anterior al horario de fin.");
                alert.showAndWait();
                return false;
            }
        }

        if (!cbLunes.isSelected() && !cbMartes.isSelected() && !cbMiercoles.isSelected() &&
                !cbJueves.isSelected() && !cbViernes.isSelected() && !cbSabado.isSelected() && !cbDomingo.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validación");
            alert.setHeaderText("Días requeridos");
            alert.setContentText("Debe seleccionar al menos un día de atención.");
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
