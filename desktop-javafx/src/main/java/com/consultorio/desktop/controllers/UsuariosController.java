package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Usuario;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuariosController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroRol;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Label lblTotalUsuarios;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;

    @FXML private Button btnAgregarUsuario;
    @FXML private Button btnVerUsuario;
    @FXML private Button btnModificarUsuario;
    @FXML private Button btnCambiarEstado;

    private ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();
    private ObservableList<Usuario> usuariosListFiltrada = FXCollections.observableArrayList();
    private String tipoUsuario;
    private String nombreUsuario;
    private String rolUsuario;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        colNombre.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreCompleto())
        );
        colRol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipoFormateado())
        );
        colEstado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstadoTexto())
        );

        // Configurar tabla
        tablaUsuarios.setItems(usuariosListFiltrada);

        // Listener para selección de usuario
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean usuarioSeleccionado = newSelection != null;
            btnVerUsuario.setDisable(!usuarioSeleccionado);

            // Habilitar botones de modificar y cambiar estado para ADMIN y PSICOPEDAGOGA
            boolean tienePermisos = tipoUsuario != null &&
                                    ("ADMIN".equalsIgnoreCase(tipoUsuario) ||
                                     "PSICOPEDAGOGA".equalsIgnoreCase(tipoUsuario) ||
                                     "Psicopedagoga".equals(tipoUsuario));

            if (tienePermisos) {
                btnModificarUsuario.setDisable(!usuarioSeleccionado);
                btnCambiarEstado.setDisable(!usuarioSeleccionado);
            }
        });

        // Configurar ComboBox de filtro por rol
        cmbFiltroRol.setItems(FXCollections.observableArrayList(
            "Todos", "Psicopedagoga", "Secretaria", "Paciente"
        ));
        cmbFiltroRol.setValue("Todos");
        cmbFiltroRol.setOnAction(e -> aplicarFiltros());

        // Configurar ComboBox de filtro por estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "Todos", "Activo", "Inactivo"
        ));
        cmbFiltroEstado.setValue("Todos");
        cmbFiltroEstado.setOnAction(e -> aplicarFiltros());
    }

    public void setUsuario(String usuario, String rol) {
        this.nombreUsuario = usuario;
        this.rolUsuario = rol;
        this.tipoUsuario = rol;

        System.out.println("DEBUG UsuariosController - Usuario: " + usuario + ", Rol: '" + rol + "'");

        // Configurar permisos según tipo de usuario
        configurarPermisos();
        
        // Cargar usuarios
        cargarUsuarios();
    }

    private void configurarPermisos() {
        System.out.println("DEBUG: Configurando permisos para rol: " + tipoUsuario);

        // Solo ADMIN y PSICOPEDAGOGA pueden agregar, modificar y cambiar estado de usuarios
        boolean tienePermisos = "ADMIN".equalsIgnoreCase(tipoUsuario) ||
                                "PSICOPEDAGOGA".equalsIgnoreCase(tipoUsuario) ||
                                "Psicopedagoga".equals(tipoUsuario);

        if (!tienePermisos) {
            btnAgregarUsuario.setDisable(true);
            btnModificarUsuario.setDisable(true);
            btnCambiarEstado.setDisable(true);
        }
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = HttpClientUtil.obtenerUsuarios();

            // Filtrar el usuario ADMIN (no mostrarlo en la lista)
            usuarios = usuarios.stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getTipo()))
                .collect(Collectors.toList());

            // Ordenar por ID descendente (más reciente primero)
            usuarios.sort((u1, u2) -> {
                if (u1.getId() == null && u2.getId() == null) return 0;
                if (u1.getId() == null) return 1;
                if (u2.getId() == null) return -1;
                return u2.getId().compareTo(u1.getId());
            });

            usuariosList.clear();
            usuariosList.addAll(usuarios);

            aplicarFiltros();

            System.out.println("✅ Usuarios cargados: " + usuarios.size());
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aplicarFiltros() {
        String filtroRol = cmbFiltroRol.getValue();
        String filtroEstado = cmbFiltroEstado.getValue();
        String terminoBusqueda = txtBuscar.getText().toLowerCase().trim();

        List<Usuario> usuariosFiltrados = usuariosList.stream()
            .filter(u -> {
                // Filtro por rol
                boolean cumpleRol = "Todos".equals(filtroRol) || 
                    u.getTipoFormateado().equalsIgnoreCase(filtroRol);

                // Filtro por estado
                boolean cumpleEstado = "Todos".equals(filtroEstado) || 
                    u.getEstadoTexto().equalsIgnoreCase(filtroEstado);

                // Filtro por búsqueda
                boolean cumpleBusqueda = terminoBusqueda.isEmpty() ||
                    u.getNombreCompleto().toLowerCase().contains(terminoBusqueda);

                return cumpleRol && cumpleEstado && cumpleBusqueda;
            })
            .collect(Collectors.toList());

        usuariosListFiltrada.clear();
        usuariosListFiltrada.addAll(usuariosFiltrados);
        
        lblTotalUsuarios.setText(String.valueOf(usuariosFiltrados.size()));
    }

    @FXML
    private void handleBuscar() {
        aplicarFiltros();
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        cmbFiltroRol.setValue("Todos");
        cmbFiltroEstado.setValue("Todos");
        aplicarFiltros();
    }

    @FXML
    private void handleAgregarUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/formulario-usuario.fxml"));
            Parent root = loader.load();

            FormularioUsuarioController controller = loader.getController();
            controller.setModoCreacion(nombreUsuario);

            Stage stage = new Stage();
            stage.setTitle("Agregar Usuario - Consultorio Cosmos");

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

            // Hacer la ventana responsive
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setWidth(700);
            stage.setHeight(650);

            stage.showAndWait();

            // Recargar usuarios después de cerrar el formulario
            cargarUsuarios();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario de usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado == null) {
            mostrarAdvertencia("Seleccione un usuario", "Debe seleccionar un usuario para ver sus datos");
            return;
        }

        abrirFichaUsuario(usuarioSeleccionado);
    }

    @FXML
    private void handleModificarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado == null) {
            mostrarAdvertencia("Seleccione un usuario", "Debe seleccionar un usuario para modificar");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/formulario-usuario.fxml"));
            Parent root = loader.load();

            FormularioUsuarioController controller = loader.getController();
            controller.setModoEdicion(usuarioSeleccionado, nombreUsuario);

            Stage stage = new Stage();
            stage.setTitle("Modificar Usuario - Consultorio Cosmos");

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

            // Hacer la ventana responsive
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setWidth(700);
            stage.setHeight(650);

            stage.showAndWait();

            // Recargar usuarios después de cerrar el formulario
            cargarUsuarios();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario de modificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCambiarEstado() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado == null) {
            mostrarAdvertencia("Seleccione un usuario", "Debe seleccionar un usuario para cambiar su estado");
            return;
        }

        String accion = usuarioSeleccionado.getActivo() ? "dar de baja" : "activar";
        String nuevoEstado = usuarioSeleccionado.getActivo() ? "Inactivo" : "Activo";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambio de estado");
        confirmacion.setHeaderText("¿Está seguro de " + accion + " a este usuario?");
        confirmacion.setContentText(usuarioSeleccionado.getNombreCompleto() + "\nEstado actual: " +
            usuarioSeleccionado.getEstadoTexto() + "\nNuevo estado: " + nuevoEstado);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = HttpClientUtil.cambiarEstadoUsuario(
                usuarioSeleccionado.getId(), 
                !usuarioSeleccionado.getActivo(),
                nombreUsuario
            );

            if (exito) {
                mostrarExito("Estado cambiado", "El estado del usuario ha sido cambiado exitosamente");
                cargarUsuarios();
            } else {
                mostrarError("Error", "No se pudo cambiar el estado del usuario");
            }
        }
    }

    private void abrirFichaUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/ficha-usuario.fxml"));
            Parent root = loader.load();

            FichaUsuarioController controller = loader.getController();
            controller.setUsuario(usuario, tipoUsuario, nombreUsuario);

            Stage stage = new Stage();
            stage.setTitle("Ficha de Usuario - Consultorio Cosmos");

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
            stage.showAndWait();

            // Recargar usuarios después de cerrar la ficha
            cargarUsuarios();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir la ficha del usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();

            Stage currentStage = (Stage) tablaUsuarios.getScene().getWindow();
            controller.setUsuario(nombreUsuario, tipoUsuario, currentStage);

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

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

