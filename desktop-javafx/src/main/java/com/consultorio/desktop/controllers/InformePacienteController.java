package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.InformePaciente;
import com.consultorio.desktop.models.Paciente;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InformePacienteController {

    @FXML private Label lblTitulo;
    @FXML private Label lblPaciente;
    @FXML private Label lblFecha;
    @FXML private TextField txtTituloInforme;
    @FXML private TextArea txtContenidoInforme;
    @FXML private Button btnGuardar;

    private Paciente pacienteActual;
    private boolean modoVisualizacion = false;
    private InformePaciente informeActual = null;

    @FXML
    public void initialize() {
        // Mostrar fecha actual
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFecha.setText(ahora.format(formatter));
    }

    public void setPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        lblPaciente.setText(paciente.getNombreCompleto() + " (DNI: " + paciente.getDni() + ")");
    }

    public void setModoVisualizacion(InformePaciente informe, Paciente paciente) {
        this.modoVisualizacion = true;
        this.informeActual = informe;
        this.pacienteActual = paciente;
        
        lblTitulo.setText("Ver Informe");
        lblPaciente.setText(paciente.getNombreCompleto() + " (DNI: " + paciente.getDni() + ")");
        lblFecha.setText(informe.getFechaCreacionFormateada());
        
        txtTituloInforme.setText(informe.getTitulo());
        txtTituloInforme.setEditable(false);
        
        txtContenidoInforme.setText(informe.getContenido());
        txtContenidoInforme.setEditable(false);
        
        btnGuardar.setVisible(false);
    }

    @FXML
    private void handleGuardar() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        try {
            InformePaciente nuevoInforme = new InformePaciente();
            nuevoInforme.setPacienteId(pacienteActual.getId());
            nuevoInforme.setTitulo(txtTituloInforme.getText().trim());
            nuevoInforme.setContenido(txtContenidoInforme.getText().trim());

            InformePaciente resultado = HttpClientUtil.crearInforme(nuevoInforme);
            
            if (resultado != null) {
                mostrarExito("Informe Creado", "El informe ha sido guardado exitosamente");
                cerrarVentana();
            } else {
                mostrarError("Error", "No se pudo guardar el informe");
            }
        } catch (Exception e) {
            mostrarError("Error", "Ocurrió un error al guardar el informe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtTituloInforme.getText().trim().isEmpty()) {
            errores.append("- El título del informe es obligatorio\n");
        }

        if (txtContenidoInforme.getText().trim().isEmpty()) {
            errores.append("- El contenido del informe es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarAdvertencia("Campos Incompletos", "Por favor complete los siguientes campos:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    @FXML
    private void handleVolver() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    // Métodos auxiliares
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

