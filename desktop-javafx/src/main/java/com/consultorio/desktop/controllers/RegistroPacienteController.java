package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Paciente;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistroPacienteController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cmbNivelEducativo;
    @FXML private ComboBox<String> cmbCondicion;
    @FXML private TextArea txtAntecedentes;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardar;

    private boolean modoEdicion = false;
    private Paciente pacienteActual = null;
    private String usuarioActual = null;

    @FXML
    public void initialize() {
        // Configurar ComboBox de Nivel Educativo
        cmbNivelEducativo.getItems().addAll("INICIAL", "PRIMARIA", "SECUNDARIA", "UNIVERSIDAD");

        // Configurar ComboBox de Condición
        cmbCondicion.getItems().addAll(
            "Dislexia",
            "Discalculia",
            "Disgrafía",
            "Trastorno del Déficit de Atención e Hiperactividad (TDAH)",
            "Trastorno del Procesamiento Auditivo (APD)",
            "Trastorno del Procesamiento Visual (VPD)",
            "Trastorno del Lenguaje Receptivo-Expresivo",
            "Trastorno del Espectro Autista (TEA)",
            "Trastorno del Aprendizaje No Verbal (NLD)",
            "Trastorno del Procesamiento Sensorial",
            "Trastorno de Ansiedad Escolar",
            "Otro",
            "Ninguno"
        );
    }

    public void setUsuario(String usuario) {
        this.usuarioActual = usuario;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;

        if (modoEdicion) {
            lblTitulo.setText("Modificar Paciente");
            btnGuardar.setText("Actualizar Paciente");
            txtDni.setEditable(false); // No se puede modificar el DNI
        } else {
            lblTitulo.setText("Registrar Nuevo Paciente");
            btnGuardar.setText("Guardar Paciente");
        }
    }

    public void cargarDatosPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        
        txtNombre.setText(paciente.getNombre());
        txtApellido.setText(paciente.getApellido());
        txtDni.setText(paciente.getDni());
        txtTelefono.setText(paciente.getTelefono());
        txtEmail.setText(paciente.getEmail());
        
        // Fecha de nacimiento
        try {
            LocalDate fecha = LocalDate.parse(paciente.getFechaNacimiento());
            dpFechaNacimiento.setValue(fecha);
        } catch (Exception e) {
            System.out.println("Error parseando fecha: " + e.getMessage());
        }
        
        cmbNivelEducativo.setValue(paciente.getNivelEducativo());
        cmbCondicion.setValue(paciente.getCondicion());
        txtAntecedentes.setText(paciente.getAntecedentes());
        txtObservaciones.setText(paciente.getObservaciones());
    }

    @FXML
    private void handleGuardar() {
        // Validar campos obligatorios
        if (!validarCampos()) {
            return;
        }

        try {
            if (modoEdicion) {
                // Actualizar paciente existente
                actualizarPaciente();
            } else {
                // Crear nuevo paciente
                crearNuevoPaciente();
            }
        } catch (Exception e) {
            mostrarError("Error", "Ocurrió un error al guardar el paciente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void crearNuevoPaciente() {
        Paciente nuevoPaciente = new Paciente();
        
        // Datos del usuario
        nuevoPaciente.setNombre(txtNombre.getText().trim());
        nuevoPaciente.setApellido(txtApellido.getText().trim());
        nuevoPaciente.setDni(txtDni.getText().trim());
        nuevoPaciente.setTelefono(txtTelefono.getText().trim());
        nuevoPaciente.setEmail(txtEmail.getText().trim());
        
        // Datos del paciente
        nuevoPaciente.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        nuevoPaciente.setNivelEducativo(cmbNivelEducativo.getValue());
        nuevoPaciente.setCondicion(cmbCondicion.getValue());
        nuevoPaciente.setAntecedentes(txtAntecedentes.getText().trim());
        nuevoPaciente.setObservaciones(txtObservaciones.getText().trim());

        Paciente resultado = HttpClientUtil.crearPaciente(nuevoPaciente, usuarioActual);

        if (resultado != null) {
            String nombreCompleto = nuevoPaciente.getNombre() + " " + nuevoPaciente.getApellido();
            mostrarExito("Paciente Registrado",
                "El paciente " + nombreCompleto + " ha sido registrado exitosamente.\n\n" +
                "Se ha enviado un email de bienvenida con las credenciales de acceso a:\n" +
                nuevoPaciente.getEmail());
            cerrarVentana();
        } else {
            mostrarError("Error", "No se pudo registrar el paciente. Verifique que el DNI y email no estén en uso.");
        }
    }

    private void actualizarPaciente() {
        if (pacienteActual == null) {
            mostrarError("Error", "No hay paciente para actualizar");
            return;
        }

        System.out.println("🔵 Actualizando paciente ID: " + pacienteActual.getId());

        // Actualizar datos personales
        pacienteActual.setNombre(txtNombre.getText().trim());
        pacienteActual.setApellido(txtApellido.getText().trim());
        pacienteActual.setDni(txtDni.getText().trim());
        pacienteActual.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
        pacienteActual.setEmail(txtEmail.getText().trim());

        // Actualizar datos del paciente
        pacienteActual.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        pacienteActual.setNivelEducativo(cmbNivelEducativo.getValue());
        pacienteActual.setCondicion(cmbCondicion.getValue());
        pacienteActual.setAntecedentes(txtAntecedentes.getText() != null ? txtAntecedentes.getText().trim() : "");
        pacienteActual.setObservaciones(txtObservaciones.getText() != null ? txtObservaciones.getText().trim() : "");

        System.out.println("🔵 Enviando actualización al servidor...");
        Paciente resultado = HttpClientUtil.actualizarPaciente(pacienteActual.getId(), pacienteActual, usuarioActual);

        if (resultado != null) {
            System.out.println("✅ Paciente actualizado exitosamente");
            String nombreCompleto = pacienteActual.getNombre() + " " + pacienteActual.getApellido();
            mostrarExito("Paciente Actualizado",
                "Los datos del paciente " + nombreCompleto + " han sido actualizados exitosamente");
            cerrarVentana();
        } else {
            System.err.println("❌ Error al actualizar paciente");
            mostrarError("Error", "No se pudo actualizar el paciente. Verifique que el DNI y email no estén en uso por otro paciente.");
        }
    }

    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        // Validar nombre
        if (nombre.isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        } else if (!nombre.matches("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]*$")) {
            errores.append("- El nombre debe comenzar con mayúscula y contener solo letras (Ej: María)\n");
        }

        // Validar apellido
        if (apellido.isEmpty()) {
            errores.append("- El apellido es obligatorio\n");
        } else if (!apellido.matches("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]*$")) {
            errores.append("- El apellido debe comenzar con mayúscula y contener solo letras (Ej: González)\n");
        }

        // Validar DNI
        if (dni.isEmpty()) {
            errores.append("- El DNI es obligatorio\n");
        } else if (!dni.matches("\\d+")) {
            errores.append("- El DNI debe contener solo números\n");
        }

        // Validar teléfono
        if (telefono.isEmpty()) {
            errores.append("- El teléfono es obligatorio\n");
        } else if (!telefono.matches("\\d{10,14}")) {
            errores.append("- El teléfono debe tener entre 10 y 14 dígitos (solo números, Ej: 1123456789)\n");
        }

        // Validar email
        if (email.isEmpty()) {
            errores.append("- El email es obligatorio\n");
        } else if (!email.contains("@") || !email.endsWith(".com")) {
            errores.append("- El email debe contener @ y terminar en .com (Ej: usuario@ejemplo.com)\n");
        } else if (!validarEmail(email)) {
            errores.append("- El formato del email no es válido (Ej: usuario@ejemplo.com)\n");
        }

        // Validar fecha de nacimiento
        if (dpFechaNacimiento.getValue() == null) {
            errores.append("- La fecha de nacimiento es obligatoria\n");
        } else if (dpFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            errores.append("- La fecha de nacimiento no puede ser futura\n");
        }

        // Validar nivel educativo
        if (cmbNivelEducativo.getValue() == null) {
            errores.append("- El nivel educativo es obligatorio\n");
        }

        // Validar condición
        if (cmbCondicion.getValue() == null) {
            errores.append("- La condición es obligatoria\n");
        }

        if (errores.length() > 0) {
            mostrarAdvertencia("Campos incompletos o inválidos", "Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean validarEmail(String email) {
        // Validar que tenga formato correcto y termine en .com
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$";
        return email.matches(regex);
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
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

