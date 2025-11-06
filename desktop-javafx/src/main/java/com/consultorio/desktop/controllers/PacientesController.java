package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Paciente;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PacientesController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colApellido;
    @FXML private TableColumn<Paciente, String> colDni;
    @FXML private TableColumn<Paciente, Integer> colEdad;
    @FXML private TableColumn<Paciente, String> colCondicion;
    @FXML private TableColumn<Paciente, String> colEstado;

    @FXML private Button btnAgregarPaciente;
    @FXML private Button btnModificarPaciente;
    @FXML private Button btnDarDeBaja;
    @FXML private Button btnVerPaciente;

    private ObservableList<Paciente> pacientesList = FXCollections.observableArrayList();
    private String tipoUsuario;
    private String nombreUsuario;
    private String rolUsuario;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colCondicion.setCellValueFactory(new PropertyValueFactory<>("condicion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configurar tabla
        tablaPacientes.setItems(pacientesList);

        // Listener para selección de paciente
        tablaPacientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean pacienteSeleccionado = newSelection != null;
            btnVerPaciente.setDisable(!pacienteSeleccionado);

            // Solo habilitar botones de modificar y dar de baja para ADMIN y PSICOPEDAGOGA
            if (tipoUsuario != null &&
                ("ADMIN".equalsIgnoreCase(tipoUsuario) || "PSICOPEDAGOGA".equalsIgnoreCase(tipoUsuario) ||
                 "Admin".equals(tipoUsuario) || "Psicopedagoga".equals(tipoUsuario))) {
                btnModificarPaciente.setDisable(!pacienteSeleccionado);
                btnDarDeBaja.setDisable(!pacienteSeleccionado);
            }
        });

        // Cargar pacientes
        cargarPacientes();
    }

    public void setUsuario(String usuario, String rol) {
        this.nombreUsuario = usuario;
        this.rolUsuario = rol;
        this.tipoUsuario = rol;

        System.out.println("DEBUG PacientesController - Usuario: " + usuario + ", Rol: '" + rol + "'");

        // Configurar permisos según tipo de usuario
        configurarPermisos();
    }

    private void configurarPermisos() {
        System.out.println("DEBUG: Configurando permisos para rol: " + tipoUsuario);
        // SECRETARIA: solo puede ver, no puede agregar, modificar ni dar de baja
        if ("SECRETARIA".equalsIgnoreCase(tipoUsuario) || "Secretaria".equals(tipoUsuario)) {
            btnAgregarPaciente.setDisable(true);
            btnModificarPaciente.setDisable(true);
            btnDarDeBaja.setDisable(true);
        }
    }

    private void cargarPacientes() {
        try {
            List<Paciente> pacientes = HttpClientUtil.getPacientesActivos();
            pacientesList.clear();
            pacientesList.addAll(pacientes);
            System.out.println("✅ Pacientes cargados: " + pacientes.size());
        } catch (Exception e) {
            mostrarError("Error al cargar pacientes", e.getMessage());
        }
    }

    @FXML
    private void handleBuscar() {
        String termino = txtBuscar.getText().trim();
        
        if (termino.isEmpty()) {
            cargarPacientes();
            return;
        }

        try {
            List<Paciente> pacientes = HttpClientUtil.buscarPacientes(termino);
            pacientesList.clear();
            pacientesList.addAll(pacientes);
            System.out.println("✅ Búsqueda completada: " + pacientes.size() + " resultados");
        } catch (Exception e) {
            mostrarError("Error en la búsqueda", e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        cargarPacientes();
    }

    @FXML
    private void handleAgregarPaciente() {
        try {
            System.out.println("🔵 Abriendo formulario de registro de paciente...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/registro-paciente.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            RegistroPacienteController controller = loader.getController();
            controller.setUsuario(nombreUsuario);
            controller.setModoEdicion(false);
            System.out.println("🔵 Modo edición configurado");

            Stage stage = new Stage();
            stage.setTitle("Registrar Nuevo Paciente");

            // Configurar como modal para bloquear la ventana padre
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(tablaPacientes.getScene().getWindow());

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
            stage.setOnHidden(e -> cargarPacientes()); // Recargar al cerrar
            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Ventana cerrada, recargando pacientes");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir formulario de registro:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de registro: " + e.getMessage());
        }
    }

    @FXML
    private void handleModificarPaciente() {
        Paciente pacienteSeleccionado = tablaPacientes.getSelectionModel().getSelectedItem();

        if (pacienteSeleccionado == null) {
            mostrarAdvertencia("Seleccione un paciente", "Debe seleccionar un paciente para modificar");
            return;
        }

        try {
            System.out.println("🔵 Abriendo formulario de modificación de paciente...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/registro-paciente.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            RegistroPacienteController controller = loader.getController();
            controller.setUsuario(nombreUsuario);
            controller.setModoEdicion(true);
            controller.cargarDatosPaciente(pacienteSeleccionado);
            System.out.println("🔵 Datos del paciente cargados");

            Stage stage = new Stage();
            stage.setTitle("Modificar Paciente");

            // Configurar como modal para bloquear la ventana padre
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(tablaPacientes.getScene().getWindow());

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
            stage.setOnHidden(e -> cargarPacientes()); // Recargar al cerrar
            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Ventana cerrada, recargando pacientes");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir formulario de modificación:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de modificación: " + e.getMessage());
        }
    }

    @FXML
    private void handleDarDeBaja() {
        Paciente pacienteSeleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
        
        if (pacienteSeleccionado == null) {
            mostrarAdvertencia("Seleccione un paciente", "Debe seleccionar un paciente para dar de baja");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Baja");
        confirmacion.setHeaderText("¿Está seguro de dar de baja a este paciente?");
        confirmacion.setContentText(pacienteSeleccionado.getNombreCompleto() + "\nDNI: " + pacienteSeleccionado.getDni());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = HttpClientUtil.darDeBajaPaciente(pacienteSeleccionado.getId(), nombreUsuario);

            if (exito) {
                mostrarExito("Paciente dado de baja", "El paciente ha sido dado de baja exitosamente");
                cargarPacientes();
            } else {
                mostrarError("Error", "No se pudo dar de baja al paciente");
            }
        }
    }

    @FXML
    private void handleVerPaciente() {
        Paciente pacienteSeleccionado = tablaPacientes.getSelectionModel().getSelectedItem();

        if (pacienteSeleccionado == null) {
            mostrarAdvertencia("Seleccione un paciente", "Debe seleccionar un paciente para ver su ficha");
            return;
        }

        try {
            System.out.println("🔵 Abriendo ficha del paciente...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/ficha-paciente.fxml"));
            System.out.println("🔵 Cargando FXML...");
            Parent root = loader.load();
            System.out.println("🔵 FXML cargado exitosamente");

            FichaPacienteController controller = loader.getController();
            controller.setTipoUsuario(tipoUsuario);
            controller.setNombreUsuario(nombreUsuario);
            System.out.println("🔵 Tipo de usuario configurado: " + tipoUsuario);
            controller.cargarDatosPaciente(pacienteSeleccionado);
            System.out.println("🔵 Datos del paciente cargados");

            // Obtener la ventana actual para heredar tamaño
            Stage ventanaActual = (Stage) tablaPacientes.getScene().getWindow();
            boolean estaMaximizada = ventanaActual.isMaximized();
            double anchoActual = ventanaActual.getWidth();
            double altoActual = ventanaActual.getHeight();

            Stage stage = new Stage();
            stage.setTitle("Ficha del Paciente - " + pacienteSeleccionado.getNombreCompleto());
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

            // Aplicar el estado de maximizado si corresponde
            if (estaMaximizada) {
                stage.setMaximized(true);
            }

            // Cerrar la ventana de pacientes y mostrar la ficha
            ventanaActual.close();
            stage.show();
            System.out.println("🔵 Ventana de pacientes cerrada, ficha mostrada (heredó tamaño: " + anchoActual + "x" + altoActual + ", maximizada: " + estaMaximizada + ")");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir ficha del paciente:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir la ficha del paciente: " + e.getMessage());
        }
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

    @FXML
    private void handleVolverDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();

            Stage currentStage = (Stage) tablaPacientes.getScene().getWindow();
            controller.setUsuario(nombreUsuario, rolUsuario, currentStage);

            Stage stage = new Stage();
            stage.setTitle("Cosmos - Dashboard");

            Scene scene = new Scene(root, 1200, 800);

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

            // Mantener el mismo estado de maximizado
            if (currentStage.isMaximized()) {
                stage.setMaximized(true);
            }

            stage.show();
            currentStage.close();

        } catch (IOException e) {
            mostrarError("Error", "No se pudo volver al Dashboard");
            e.printStackTrace();
        }
    }
}

