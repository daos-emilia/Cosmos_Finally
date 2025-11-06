package com.consultorio.desktop.controllers;

import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class RegistroSecretariaController {

    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField telefonoField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;

    private Stage parentStage;

    // Método para recibir el stage padre
    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    @FXML
    private void initialize() {
        // Solo inicializaciones que no requieran la escena
        System.out.println("Inicializando controlador de registro...");
    }

    @FXML
    private void handleRegistro() {
        String dni = dniField.getText().trim();
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validaciones básicas
        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() ||
                telefono.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrarAlertaError("Error", "Campos incompletos", "Por favor, complete todos los campos");
            return;
        }

        // Validar DNI (solo números)
        if (!dni.matches("\\d+")) {
            mostrarAlertaError("Error", "DNI inválido", "El DNI debe contener solo números.");
            return;
        }

        // Validar que nombre comience con mayúscula
        if (!nombre.matches("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]*$")) {
            mostrarAlertaError("Error", "Nombre inválido",
                "El nombre debe comenzar con mayúscula y contener solo letras.\nEjemplo: María");
            return;
        }

        // Validar que apellido comience con mayúscula
        if (!apellido.matches("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]*$")) {
            mostrarAlertaError("Error", "Apellido inválido",
                "El apellido debe comenzar con mayúscula y contener solo letras.\nEjemplo: González");
            return;
        }

        // Validar teléfono (10-14 dígitos, solo números)
        if (!telefono.matches("\\d{10,14}")) {
            mostrarAlertaError("Error", "Teléfono inválido",
                "El teléfono debe contener entre 10 y 14 dígitos (solo números).\nEjemplo: 1123456789");
            return;
        }

        // Validar email (debe contener @ y .com)
        if (!email.contains("@") || !email.endsWith(".com")) {
            mostrarAlertaError("Error", "Email inválido",
                "El email debe contener @ y terminar en .com\nEjemplo: maria@ejemplo.com");
            return;
        }

        // Validación adicional de formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$")) {
            mostrarAlertaError("Error", "Email inválido",
                "Por favor, ingrese un email válido.\nEjemplo: maria@ejemplo.com");
            return;
        }

        if (password.length() < 4) {
            mostrarAlertaError("Error", "Contraseña muy corta", "La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        try {
            System.out.println("Enviando datos al backend...");

            // Crear usuario secretaria CON EMAIL
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("dni", dni);
            usuario.put("password", password);
            usuario.put("nombre", nombre);
            usuario.put("apellido", apellido);
            usuario.put("telefono", telefono);
            usuario.put("email", email);
            usuario.put("tipo", "SECRETARIA");
            usuario.put("activo", true);

            // Enviar al backend
            Map<String, Object> response = HttpClientUtil.sendPost("/usuarios", usuario);

            System.out.println("Secretaria registrada en BD: " + response);

            mostrarAlertaExito("Registro exitoso",
                    "Secretaria registrada correctamente",
                    "La secretaria " + nombre + " " + apellido + " ha sido registrada en el sistema.\n\n" +
                            "DNI: " + dni + "\n" +
                            "Email: " + email + "\n" +
                            "Contraseña: " + password + "\n\n" +
                            "Se enviará un email de validación para activar la cuenta.");

            // Cerrar ventana
            ((Stage) dniField.getScene().getWindow()).close();

        } catch (Exception e) {
            System.err.println("Error registrando secretaria: " + e.getMessage());
            mostrarAlertaError("Error en registro",
                    "No se pudo registrar la secretaria",
                    "Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        ((Stage) dniField.getScene().getWindow()).close();
    }

    private void mostrarAlertaError(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarAlertaExito(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
