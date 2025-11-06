package com.consultorio.desktop.controllers;

import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlertaError("Error", "Campos vacíos", "Por favor, complete todos los campos.");
            return;
        }

        try {
            // Intentar login con backend
            Map<String, Object> usuario = HttpClientUtil.login(username, password);

            // VALIDAR ROLES PERMITIDOS - SOLO ADMIN, PSICOPEDAGOGA Y SECRETARIA
            String tipo = (String) usuario.get("tipo");
            if (!esRolPermitido(tipo)) {
                mostrarAlertaError("Acceso denegado",
                        "Rol no habilitado para sistema de escritorio",
                        "Los pacientes deben ingresar por la plataforma web.");
                return;
            }

            System.out.println("Login exitoso! Usuario: " + usuario.get("nombre") + " - Rol: " + tipo);
            navegarADashboard(usuario);
            limpiarCamposLogin();

        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            mostrarAlertaError("Error de inicio de sesión",
                    "Credenciales inválidas",
                    "Usuario o contraseña incorrectos");
        }
    }

    @FXML
    private void handleRegistro() {
        try {
            String fxmlPath = "/com/consultorio/desktop/fxml/registro-secretaria.fxml";
            java.net.URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("No se encuentra el archivo: " + fxmlPath);
                mostrarAlertaError("Error", "Archivo no encontrado",
                        "No se pudo encontrar: " + fxmlPath + "\n\nVerifica la ubicación del archivo.");
                return;
            }

            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Registro de Secretaria - Consultorio Cosmos");

            Scene scene = new Scene(root);

            // Cargar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Hacer la ventana responsive (igual que formulario-usuario)
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setWidth(700);
            stage.setHeight(650);

            // Obtener el stage padre y pasarlo al controlador
            Stage parentStage = (Stage) usernameField.getScene().getWindow();
            RegistroSecretariaController registroController = loader.getController();
            registroController.setParentStage(parentStage);

            stage.showAndWait(); // showAndWait para que sea modal

        } catch (Exception e) {
            System.err.println("Error completo: " + e.getMessage());
            e.printStackTrace();
            mostrarAlertaError("Error", "No se pudo cargar el formulario de registro",
                    "Error detallado: " + e.getMessage() + "\n\nStack trace en consola.");
        }
    }

    @FXML
    private void handleRecuperarPassword() {
        String dni = usernameField.getText().trim();

        if (dni.isEmpty()) {
            mostrarAlertaInfo("Recuperar Contraseña", "Ingrese su DNI", "Por favor, ingrese su DNI en el campo de usuario para recuperar la contraseña.");
            return;
        }

        if (!esNumeroDNI(dni)) {
            mostrarAlertaError("Error", "DNI inválido", "Ingrese un DNI válido (solo números)");
        }

        try {
            // Validar que el DNI existe en la base de datos
            System.out.println("Validando DNI en base de datos: " + dni);
            Map<String, Object> usuario = HttpClientUtil.getUsuarioByDni(dni);

            if (usuario != null && !usuario.isEmpty()) {
                String nombre = (String) usuario.get("nombre");
                String apellido = (String) usuario.get("apellido");
                String email = (String) usuario.get("email");
                Boolean emailValidado = (Boolean) usuario.get("emailValidado");

                // VERIFICAR SI EL MAIL ESTÁ VALIDADO
                if (Boolean.TRUE.equals(emailValidado)) {
                    // Email validado - ENVIAR RECORDATORIO DE CONTRASEÑA
                    System.out.println("DNI validado - Email verificado: " + email);
                    System.out.println("Enviando recordatorio de contraseña...");

                    boolean emailEnviado = HttpClientUtil.enviarRecordatorioPassword(dni);

                    if (emailEnviado) {
                        mostrarAlertaInfo("Recuperación de Contraseña",
                                "Email enviado correctamente",
                                "Se ha enviado un recordatorio de contraseña a:\n\n" +
                                        "- " + nombre + " " + apellido + "\n" +
                                        "- " + email + "\n" +
                                        "- DNI: " + dni + "\n\n" +
                                        "Revise su correo electrónico para ver su contraseña.");
                    } else {
                        mostrarAlertaError("Error",
                                "No se pudo enviar el email",
                                "Hubo un problema al enviar el recordatorio de contraseña.\n\n" +
                                        "Por favor, intente nuevamente más tarde o contacte al administrador.");
                    }

                } else {
                    // Email NO validado - debe validarlo primero
                    mostrarAlertaError("Email no validado",
                            "Debe validar su email primero",
                            "Para recuperar su contraseña, primero debe validar su dirección de email.\n\n" +
                                    "- " + nombre + " " + apellido + "\n" +
                                    "- " + email + "\n" +
                                    "- DNI: " + dni + "\n\n" +
                                    "Al registrarse, se le envió un email automático para confirmar su dirección de email.\n" +
                                    " Por favor, revise su correo y complete la validación.");
                }

            } else {
                mostrarAlertaError("DNI no encontrado",
                        "Usuario no registrado",
                        "El DNI " + dni + " no se encuentra registrado en el sistema.\n\n" +
                                "Por favor, verifique el número o contacte a la Psicopedagoga.");
            }

        } catch (Exception e) {
            System.err.println("Error validando DNI: " + e.getMessage());
            mostrarAlertaError("Error de conexión",
                    "No se pudo verificar el DNI",
                    "Error al conectar con la base de datos: " + e.getMessage() + "\n\n" +
                            "Por favor, intente nuevamente o contacte a la Psicopedagoga.");
        }
    }

    private void navegarADashboard(Map<String, Object> usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/dashboard.fxml"));
            Parent root = loader.load();

            String nombre = (String) usuario.get("nombre");
            String apellido = (String) usuario.get("apellido");
            String tipo = (String) usuario.get("tipo");
            String rol = convertirTipoARol(tipo);
            String nombreCompleto = nombre + " " + apellido;

            DashboardController dashboardController = loader.getController();

            // Pasar el stage del login para heredar el estado
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            dashboardController.setUsuario(nombreCompleto, rol, loginStage);

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Cosmos - Dashboard - " + nombreCompleto);
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/consultorio/desktop/styles/styles.css").toExternalForm());
            dashboardStage.setScene(scene);

            // Heredar estado de maximizado del login
            dashboardStage.setMaximized(loginStage.isMaximized());

            dashboardStage.show();
            loginStage.close(); // Cerrar el login

        } catch (Exception e) {
            mostrarAlertaError("Error", "No se pudo cargar el dashboard", e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para validar roles permitidos en escritorio
    private boolean esRolPermitido(String tipo) {
        return "ADMIN".equals(tipo) || "PSICOPEDAGOGA".equals(tipo) || "SECRETARIA".equals(tipo);
    }

    private String convertirTipoARol(String tipo) {
        if (tipo == null) return "Usuario";

        switch (tipo.toUpperCase()) {
            case "ADMIN": return "Administrador";
            case "PSICOPEDAGOGA": return "Psicopedagoga";
            case "SECRETARIA": return "Secretaria";
            case "PACIENTE": return "Paciente";
            default: return "Usuario";
        }
    }

    private void limpiarCamposLogin() {
        usernameField.clear();
        passwordField.clear();
    }

    private boolean esNumeroDNI(String texto) {
        return texto.matches("\\d+");
    }

    private void mostrarAlertaError(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarAlertaInfo(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}