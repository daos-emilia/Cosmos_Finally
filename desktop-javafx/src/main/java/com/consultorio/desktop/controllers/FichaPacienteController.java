package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Paciente;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FichaPacienteController {

    @FXML private Label lblNombreCompleto;
    @FXML private Label lblDni;
    @FXML private Label lblEdad;
    @FXML private Label lblFechaNacimiento;
    @FXML private Label lblFechaRegistro;
    @FXML private Label lblTelefono;
    @FXML private Label lblEmail;
    @FXML private Label lblNivelEducativo;
    @FXML private Label lblCondicion;
    @FXML private TextArea txtAntecedentes;
    @FXML private TextArea txtObservaciones;
    @FXML private ImageView imgFotoPaciente;
    
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnModificarInformacion;
    @FXML private Button btnDarDeBaja;
    @FXML private Button btnRealizarInforme;
    @FXML private Button btnHistorialEvolucion;

    private Paciente pacienteActual;
    private String tipoUsuario;
    private String nombreUsuario;

    @FXML
    public void initialize() {
        // La configuración de permisos se hará cuando se carguen los datos del paciente
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
        configurarPermisos();
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    private void configurarPermisos() {
        // SECRETARIA: solo puede ver, no puede modificar ni dar de baja
        if ("SECRETARIA".equalsIgnoreCase(tipoUsuario) || "Secretaria".equals(tipoUsuario)) {
            btnModificarInformacion.setDisable(true);
            btnDarDeBaja.setDisable(true);
            btnRealizarInforme.setDisable(true);
            btnCambiarFoto.setDisable(true);
        }
    }

    public void cargarDatosPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        
        // Datos básicos
        lblNombreCompleto.setText(paciente.getNombreCompleto());
        lblDni.setText(paciente.getDni());
        lblEdad.setText(paciente.getEdad() + " años");
        lblFechaNacimiento.setText(paciente.getFechaNacimientoFormateada());
        lblFechaRegistro.setText(paciente.getFechaCreacionFormateada());
        
        // Contacto
        lblTelefono.setText(paciente.getTelefono());
        lblEmail.setText(paciente.getEmail());
        
        // Información educativa y clínica
        lblNivelEducativo.setText(paciente.getNivelEducativo());
        lblCondicion.setText(paciente.getCondicion());
        
        // Antecedentes y observaciones
        txtAntecedentes.setText(paciente.getAntecedentes() != null ? paciente.getAntecedentes() : "Sin antecedentes registrados");
        txtObservaciones.setText(paciente.getObservaciones() != null ? paciente.getObservaciones() : "Sin observaciones registradas");
        
        // TODO: Cargar foto si existe
        // if (paciente.getFotoPath() != null) { ... }
    }

    @FXML
    private void handleCambiarFoto() {
        System.out.println("🔵 Intentando cambiar foto del paciente...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto del Paciente");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(btnCambiarFoto.getScene().getWindow());

        if (file != null) {
            System.out.println("📁 Archivo seleccionado: " + file.getAbsolutePath());
            // TODO: Implementar subida de foto al servidor
            mostrarAdvertencia("Funcionalidad en desarrollo",
                "La carga de fotos al servidor se implementará próximamente.\n\n" +
                "Archivo seleccionado: " + file.getName() + "\n" +
                "Tamaño: " + (file.length() / 1024) + " KB");
        } else {
            System.out.println("⚠️ No se seleccionó ningún archivo");
        }
    }

    @FXML
    private void handleModificarInformacion() {
        try {
            System.out.println("🔵 Abriendo formulario de modificación desde ficha...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/registro-paciente.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            RegistroPacienteController controller = loader.getController();
            controller.setUsuario(tipoUsuario); // Pasar el usuario actual
            controller.setModoEdicion(true);
            controller.cargarDatosPaciente(pacienteActual);
            System.out.println("🔵 Datos del paciente cargados");

            // Obtener la ventana actual (ficha) para configurar como padre
            Stage ventanaActual = (Stage) lblNombreCompleto.getScene().getWindow();

            Stage stage = new Stage();
            stage.setTitle("Modificar Información del Paciente");

            // Configurar como modal para bloquear la ficha mientras se edita
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(ventanaActual);

            Scene scene = new Scene(root, 800, 700);

            // Aplicar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.setOnHidden(e -> {
                // Recargar datos del paciente cuando se cierre el formulario
                System.out.println("🔵 Recargando datos del paciente...");
                Paciente pacienteActualizado = HttpClientUtil.getPacienteById(pacienteActual.getId());
                if (pacienteActualizado != null) {
                    cargarDatosPaciente(pacienteActualizado);
                    System.out.println("✅ Datos actualizados en la ficha");
                }
            });

            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Ventana de modificación cerrada");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir formulario de modificación:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de modificación: " + e.getMessage());
        }
    }

    @FXML
    private void handleDarDeBaja() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Baja");
        confirmacion.setHeaderText("¿Está seguro de dar de baja a este paciente?");
        confirmacion.setContentText(pacienteActual.getNombreCompleto() + "\nDNI: " + pacienteActual.getDni());

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = HttpClientUtil.darDeBajaPaciente(pacienteActual.getId(), nombreUsuario);

            if (exito) {
                mostrarExito("Paciente dado de baja", "El paciente ha sido dado de baja exitosamente");
                // Volver a la ventana de pacientes (igual que el botón "Volver")
                handleVolver();
            } else {
                mostrarError("Error", "No se pudo dar de baja al paciente");
            }
        }
    }

    @FXML
    private void handleRealizarInforme() {
        try {
            System.out.println("🔵 Abriendo formulario de informe como modal...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/informe-paciente.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            InformePacienteController controller = loader.getController();
            controller.setPaciente(pacienteActual);

            // Obtener ventana actual (NO cerrarla, solo usarla como padre del modal)
            Stage ventanaActual = (Stage) lblNombreCompleto.getScene().getWindow();

            Stage stage = new Stage();
            stage.setTitle("Crear Informe - " + pacienteActual.getNombreCompleto());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(ventanaActual);
            Scene scene = new Scene(root, 800, 700);

            // Aplicar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Formulario de informe cerrado, volviendo a ficha personal");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir formulario de informe:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de informe: " + e.getMessage());
        }
    }

    @FXML
    private void handleHistorialEvolucion() {
        try {
            System.out.println("🔵 Abriendo historial de evolución como modal...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/historial-evolucion.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            HistorialEvolucionController controller = loader.getController();
            controller.setTipoUsuario(tipoUsuario);
            controller.cargarHistorial(pacienteActual);

            // Obtener ventana actual (NO cerrarla, solo usarla como padre del modal)
            Stage ventanaActual = (Stage) lblNombreCompleto.getScene().getWindow();

            Stage stage = new Stage();
            stage.setTitle("Historial de Evolución - " + pacienteActual.getNombreCompleto());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(ventanaActual);
            Scene scene = new Scene(root, 900, 700);

            // Aplicar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);
            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Historial de evolución cerrado, volviendo a ficha personal");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir historial de evolución:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el historial de evolución: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        try {
            System.out.println("🔵 Volviendo a la ventana de pacientes...");

            // Cargar la ventana de pacientes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/pacientes.fxml"));
            Parent root = loader.load();

            PacientesController controller = loader.getController();
            controller.setUsuario(nombreUsuario, tipoUsuario);

            // Obtener ventana actual para heredar tamaño
            Stage ventanaActual = (Stage) lblNombreCompleto.getScene().getWindow();
            boolean estaMaximizada = ventanaActual.isMaximized();
            double anchoActual = ventanaActual.getWidth();
            double altoActual = ventanaActual.getHeight();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Pacientes");
            Scene scene = new Scene(root, anchoActual, altoActual);

            // Aplicar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);

            // Aplicar estado de maximizado si corresponde
            if (estaMaximizada) {
                stage.setMaximized(true);
            }

            // Cerrar ventana actual y mostrar la de pacientes
            ventanaActual.close();
            stage.show();
            System.out.println("🔵 Ventana de pacientes mostrada");
        } catch (Exception e) {
            System.err.println("❌ ERROR al volver a pacientes:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo volver a la ventana de pacientes: " + e.getMessage());
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblNombreCompleto.getScene().getWindow();
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

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) lblNombreCompleto.getScene().getWindow();
        stage.close();
    }
}

