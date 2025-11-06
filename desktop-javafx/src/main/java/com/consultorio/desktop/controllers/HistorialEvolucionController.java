package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.InformePaciente;
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

public class HistorialEvolucionController {

    @FXML private Label lblPaciente;
    @FXML private Label lblTotalInformes;
    @FXML private TableView<InformePaciente> tablaInformes;
    @FXML private TableColumn<InformePaciente, String> colTitulo;
    @FXML private TableColumn<InformePaciente, String> colFecha;
    @FXML private TableColumn<InformePaciente, Void> colAcciones;
    @FXML private Button btnVerInforme;
    @FXML private Button btnNuevoInforme;

    private ObservableList<InformePaciente> informesList = FXCollections.observableArrayList();
    private Paciente pacienteActual;
    private String tipoUsuario;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colFecha.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaCreacionFormateada())
        );

        // Configurar columna de acciones con botón "Ver"
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");

            {
                // Aplicar estilos Cosmos (botón pequeño tipo link)
                btnVer.getStyleClass().add("cosmos-link-button");
                btnVer.setOnAction(event -> {
                    InformePaciente informe = getTableView().getItems().get(getIndex());
                    verInforme(informe);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                }
            }
        });

        // Configurar tabla
        tablaInformes.setItems(informesList);

        // Listener para selección de informe
        tablaInformes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnVerInforme.setDisable(newSelection == null);
        });
    }

    public void cargarHistorial(Paciente paciente) {
        this.pacienteActual = paciente;
        lblPaciente.setText(paciente.getNombreCompleto() + " (DNI: " + paciente.getDni() + ")");

        cargarInformes();
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
        configurarPermisos();
    }

    private void configurarPermisos() {
        // SECRETARIA: solo puede ver informes, no puede crear nuevos
        if ("SECRETARIA".equalsIgnoreCase(tipoUsuario) || "Secretaria".equals(tipoUsuario)) {
            if (btnNuevoInforme != null) {
                btnNuevoInforme.setDisable(true);
            }
        }
    }

    private void cargarInformes() {
        try {
            System.out.println("🔵 Cargando informes del paciente ID: " + pacienteActual.getId());
            List<InformePaciente> informes = HttpClientUtil.getInformesByPaciente(pacienteActual.getId());

            if (informes == null) {
                System.err.println("❌ La lista de informes es null");
                informes = new java.util.ArrayList<>();
            }

            informesList.clear();
            informesList.addAll(informes);
            lblTotalInformes.setText(String.valueOf(informes.size()));
            System.out.println("✅ Informes cargados: " + informes.size());

            if (informes.isEmpty()) {
                System.out.println("ℹ️ No hay informes para este paciente");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar informes:");
            e.printStackTrace();
            mostrarError("Error al cargar informes", "No se pudieron cargar los informes del paciente.\n\nError: " + e.getMessage());
        }
    }

    @FXML
    private void handleVerInforme() {
        InformePaciente informeSeleccionado = tablaInformes.getSelectionModel().getSelectedItem();
        
        if (informeSeleccionado == null) {
            mostrarAdvertencia("Seleccione un informe", "Debe seleccionar un informe para ver");
            return;
        }

        verInforme(informeSeleccionado);
    }

    private void verInforme(InformePaciente informe) {
        try {
            System.out.println("🔵 Abriendo visor de informe...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/ver-informe.fxml"));
            Parent root = loader.load();

            VerInformeController controller = loader.getController();
            controller.cargarInforme(informe, pacienteActual);

            // Obtener ventana actual para usar como padre del modal
            Stage ventanaActual = (Stage) lblPaciente.getScene().getWindow();

            Stage stage = new Stage();
            stage.setTitle("Informe - " + informe.getTitulo());
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
            stage.showAndWait();
            System.out.println("🔵 Visor de informe cerrado");
        } catch (IOException e) {
            System.err.println("❌ ERROR al abrir visor de informe:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el informe: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevoInforme() {
        try {
            System.out.println("🔵 Abriendo formulario de nuevo informe como modal...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/informe-paciente.fxml"));
            Parent root = loader.load();

            InformePacienteController controller = loader.getController();
            controller.setPaciente(pacienteActual);

            // Obtener ventana actual (NO cerrarla, solo usarla como padre del modal)
            Stage ventanaActual = (Stage) lblPaciente.getScene().getWindow();

            Stage stage = new Stage();
            stage.setTitle("Crear Informe - " + pacienteActual.getNombreCompleto());
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

            // Recargar informes al cerrar el formulario
            stage.setOnHidden(e -> cargarInformes());

            stage.showAndWait(); // Esperar a que se cierre antes de continuar
            System.out.println("🔵 Formulario de informe cerrado, volviendo a historial");
        } catch (Exception e) {
            System.err.println("❌ ERROR al abrir formulario de informe:");
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de informe: " + e.getMessage());
        }
    }

    @FXML
    private void handleCerrar() {
        cerrarVentana();
    }

    @FXML
    private void handleVolver() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblPaciente.getScene().getWindow();
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
}

