package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Usuario;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class FichaUsuarioController {

    @FXML private Label lblNombre;
    @FXML private Label lblApellido;
    @FXML private Label lblDni;
    @FXML private Label lblTelefono;
    @FXML private Label lblEmail;
    @FXML private Label lblEmailValidado;
    @FXML private Label lblPassword;
    @FXML private Button btnMostrarPassword;
    @FXML private Label lblTipo;
    @FXML private VBox vboxMatricula;
    @FXML private Label lblMatricula;
    @FXML private Label lblEstado;
    @FXML private Label lblFechaCreacion;
    @FXML private Label lblCreadoPor;
    @FXML private Label lblFechaActualizacion;
    @FXML private Label lblActualizadoPor;

    @FXML private Button btnModificar;
    @FXML private Button btnCambiarEstado;

    private Usuario usuarioActual;
    private String tipoUsuario;
    private String nombreUsuario;
    private boolean passwordVisible = false;
    private String passwordReal;

    public void setUsuario(Usuario usuario, String tipo, String nombre) {
        this.usuarioActual = usuario;
        this.tipoUsuario = tipo;
        this.nombreUsuario = nombre;

        cargarDatos();
        configurarPermisos();
    }

    private void cargarDatos() {
        if (usuarioActual == null) return;

        lblNombre.setText(usuarioActual.getNombre());
        lblApellido.setText(usuarioActual.getApellido());
        lblDni.setText(usuarioActual.getDni());
        lblTelefono.setText(usuarioActual.getTelefono());
        lblEmail.setText(usuarioActual.getEmail());
        lblEmailValidado.setText(usuarioActual.getEmailValidadoTexto());
        
        // Guardar contraseña real y mostrar oculta
        passwordReal = usuarioActual.getPassword();
        lblPassword.setText("••••••••");
        
        lblTipo.setText(usuarioActual.getTipoFormateado());
        lblEstado.setText(usuarioActual.getEstadoTexto());

        // Mostrar matrícula solo si es psicopedagoga
        if ("PSICOPEDAGOGA".equalsIgnoreCase(usuarioActual.getTipo())) {
            vboxMatricula.setVisible(true);
            vboxMatricula.setManaged(true);
            lblMatricula.setText(usuarioActual.getMatricula() != null ? usuarioActual.getMatricula() : "N/A");
        } else {
            vboxMatricula.setVisible(false);
            vboxMatricula.setManaged(false);
        }

        // Información de auditoría
        lblFechaCreacion.setText(usuarioActual.getFechaCreacionFormateada());
        lblCreadoPor.setText(usuarioActual.getCreadoPor() != null ? usuarioActual.getCreadoPor() : "Sistema");
        lblFechaActualizacion.setText(usuarioActual.getFechaActualizacionFormateada());
        lblActualizadoPor.setText(usuarioActual.getActualizadoPor() != null ? usuarioActual.getActualizadoPor() : "N/A");

        // Actualizar texto del botón según estado
        btnCambiarEstado.setText(usuarioActual.getActivo() ? "Dar de Baja" : "Activar");
    }

    private void configurarPermisos() {
        // Solo ADMIN y PSICOPEDAGOGA pueden modificar y cambiar estado
        boolean tienePermisos = "ADMIN".equalsIgnoreCase(tipoUsuario) ||
                                "PSICOPEDAGOGA".equalsIgnoreCase(tipoUsuario) ||
                                "Psicopedagoga".equals(tipoUsuario);

        if (!tienePermisos) {
            btnModificar.setDisable(true);
            btnCambiarEstado.setDisable(true);
            btnMostrarPassword.setDisable(true);
        }
    }

    @FXML
    private void handleMostrarPassword() {
        if (passwordVisible) {
            lblPassword.setText("••••••••");
            btnMostrarPassword.setText("👁");
            passwordVisible = false;
        } else {
            lblPassword.setText(passwordReal);
            btnMostrarPassword.setText("🙈");
            passwordVisible = true;
        }
    }

    @FXML
    private void handleModificar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/formulario-usuario.fxml"));
            Parent root = loader.load();

            FormularioUsuarioController controller = loader.getController();
            controller.setModoEdicion(usuarioActual, nombreUsuario);

            Stage stage = new Stage();
            stage.setTitle("Modificar Usuario - Consultorio Cosmos");

            Scene scene = new Scene(root);

            // Cargar CSS
            try {
                String css = getClass().getResource("/com/consultorio/desktop/styles/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo cargar el CSS: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Hacer la ventana responsive
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setWidth(700);
            stage.setHeight(650);

            stage.showAndWait();

            // Recargar datos del usuario
            Usuario usuarioActualizado = HttpClientUtil.obtenerUsuarioPorId(usuarioActual.getId());
            if (usuarioActualizado != null) {
                this.usuarioActual = usuarioActualizado;
                cargarDatos();
            }
        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario de modificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCambiarEstado() {
        String accion = usuarioActual.getActivo() ? "dar de baja" : "activar";
        String nuevoEstado = usuarioActual.getActivo() ? "Inactivo" : "Activo";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambio de estado");
        confirmacion.setHeaderText("¿Está seguro de " + accion + " a este usuario?");
        confirmacion.setContentText(usuarioActual.getNombreCompleto() + "\nEstado actual: " +
            usuarioActual.getEstadoTexto() + "\nNuevo estado: " + nuevoEstado);

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = HttpClientUtil.cambiarEstadoUsuario(
                usuarioActual.getId(),
                !usuarioActual.getActivo(),
                nombreUsuario
            );

            if (exito) {
                mostrarExito("Estado cambiado", "El estado del usuario ha sido cambiado exitosamente");

                // Recargar datos del usuario desde el backend para obtener auditoría actualizada
                Usuario usuarioActualizado = HttpClientUtil.obtenerUsuarioPorId(usuarioActual.getId());
                if (usuarioActualizado != null) {
                    this.usuarioActual = usuarioActualizado;
                    cargarDatos();
                } else {
                    // Si falla la recarga, al menos actualizar el estado localmente
                    usuarioActual.setActivo(!usuarioActual.getActivo());
                    cargarDatos();
                }
            } else {
                mostrarError("Error", "No se pudo cambiar el estado del usuario");
            }
        }
    }

    @FXML
    private void handleVolver() {
        Stage stage = (Stage) lblNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

