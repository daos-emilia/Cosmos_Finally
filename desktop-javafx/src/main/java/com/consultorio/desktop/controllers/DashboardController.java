package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Turno;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblUsuario;
    @FXML private Label lblFechaActual; // NUEVO: Label para fecha en el header
    @FXML private ImageView imgLogo;
    @FXML private ImageView imgLogoFondo;
    @FXML private ListView<String> listTurnos;
    @FXML private Label lblNoTurnos;
    @FXML private Label lblProximosTurnos;

    // Botones que se deshabilitarán para secretarias
    @FXML private Button btnPagos;
    @FXML private Button btnEstadisticas;
    @FXML private Button btnGestionUsuarios;

    private String usuarioActual;
    private String rolActual;
    private Stage loginStage;

    @FXML
    public void initialize() {
        cargarLogo();
        mostrarFechaActualEnHeader(); // CAMBIADO: Nueva ubicación
        cargarProximosTurnos();
    }

    public void setUsuario(String usuario, String rol, Stage loginStage) {
        this.usuarioActual = usuario;
        this.rolActual = rol;
        this.loginStage = loginStage;

        lblUsuario.setText("Bienvenido/a, " + usuario + " (" + rol + ")");
        configurarSegunRol();
    }

    private void cargarLogo() {
        try {
            String logoPath = "/com/consultorio/desktop/images/logo.png";
            var logoResource = getClass().getResourceAsStream(logoPath);
            if (logoResource != null) {
                Image logo = new Image(logoResource);
                imgLogo.setImage(logo);
                imgLogoFondo.setImage(logo);
            } else {
                System.out.println("Logo no encontrado en: " + logoPath);
                crearLogoTemporal();
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el logo: " + e.getMessage());
            crearLogoTemporal();
        }
    }

    private void crearLogoTemporal() {
        System.out.println("Usando logo temporal - Coloca tu logo en: src/main/resources/com/consultorio/desktop/images/logo.png");
    }

    // ========== MÉTODO CAMBIADO: FECHA EN EL HEADER ==========
    private void mostrarFechaActualEnHeader() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaFormateada = LocalDate.now().format(formatter);
        // Capitalizar primera letra
        fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
        lblFechaActual.setText(fechaFormateada);
    }

    private void configurarSegunRol() {
        System.out.println("DEBUG: Configurando permisos para rol: " + rolActual);

        if ("Secretaria".equals(rolActual)) {
            btnPagos.setDisable(true);
            btnPagos.setStyle("-fx-opacity: 0.5;");
            btnPagos.setTooltip(new javafx.scene.control.Tooltip("No disponible para secretarias"));

            btnEstadisticas.setDisable(true);
            btnEstadisticas.setStyle("-fx-opacity: 0.5;");
            btnEstadisticas.setTooltip(new javafx.scene.control.Tooltip("No disponible para secretarias"));

            btnGestionUsuarios.setDisable(true);
            btnGestionUsuarios.setStyle("-fx-opacity: 0.5;");
            btnGestionUsuarios.setTooltip(new javafx.scene.control.Tooltip("No disponible para secretarias"));
        }
    }

    private void cargarProximosTurnos() {
        try {
            System.out.println("🔄 Cargando próximos turnos del backend...");
            System.out.println("🔗 URL: http://localhost:8080/api/turnos/proximos");

            // Obtener turnos reales del backend usando la nueva clase Turno
            List<Turno> turnos = HttpClientUtil.getProximosTurnos();

            System.out.println("📊 Turnos recibidos: " + (turnos != null ? turnos.size() : "null"));

            if (turnos != null && !turnos.isEmpty()) {
                System.out.println("🎯 Primer turno: " + turnos.get(0));
            }

            mostrarTurnosEnLista(turnos);

        } catch (Exception e) {
            System.out.println("❌ Error cargando próximos turnos del backend: " + e.getMessage());
            e.printStackTrace();

            listTurnos.setVisible(false);
            lblNoTurnos.setVisible(true);
            lblNoTurnos.setText("Error conectando con el servidor");
        }
    }

    // ========== MÉTODO MEJORADO: MOSTRAR TURNOS ==========
    private void mostrarTurnosEnLista(List<Turno> turnos) {
        listTurnos.getItems().clear();

        if (turnos == null || turnos.isEmpty()) {
            System.out.println("📭 No hay turnos para mostrar");
            listTurnos.setVisible(false);
            lblNoTurnos.setVisible(true);
            lblNoTurnos.setText("No hay próximos turnos programados");
            lblProximosTurnos.setText("Próximos Turnos (0)");
            return;
        }

        System.out.println("📋 Mostrando " + turnos.size() + " turnos en la lista");

        // Ordenar turnos por fecha y hora (más próximos primero)
        turnos.sort((t1, t2) -> {
            int fechaCompare = t1.getFecha().compareTo(t2.getFecha());
            if (fechaCompare != 0) return fechaCompare;
            return t1.getHoraInicio().compareTo(t2.getHoraInicio());
        });

        for (Turno turno : turnos) {
            try {
                String fechaFormateada = formatearFechaParaDisplay(turno.getFecha());
                String horaFormateada = formatearHoraParaDisplay(turno.getHoraInicio());
                String emojiEstado = obtenerEmojiEstado(turno.getEstado());
                String nombrePaciente = obtenerNombrePaciente(turno);

                // Formato mejorado para mejor legibilidad
                String item = String.format("📅 %s   %s %s   👤 %s",
                        fechaFormateada, emojiEstado, horaFormateada, nombrePaciente);

                listTurnos.getItems().add(item);
                System.out.println("✅ Turno agregado: " + item);

            } catch (Exception e) {
                System.out.println("⚠️ Error procesando turno: " + e.getMessage());
            }
        }

        listTurnos.setVisible(true);
        lblNoTurnos.setVisible(false);
        lblProximosTurnos.setText("Próximos Turnos (" + turnos.size() + ")");

        System.out.println("🎯 Lista de turnos actualizada correctamente");
    }

    // ========== MÉTODOS AUXILIARES ==========
    private String obtenerNombrePaciente(Turno turno) {
        try {
            // Si es turno de invitado
            if (turno.getInvitadoNombre() != null && !turno.getInvitadoNombre().isEmpty()) {
                return turno.getInvitadoNombre() + " " +
                        (turno.getInvitadoApellido() != null ? turno.getInvitadoApellido() : "") +
                        " (Invitado)";
            }

            // Si es turno normal, usar los datos del paciente
            if (turno.getNombreCompletoPaciente() != null && !turno.getNombreCompletoPaciente().isEmpty()) {
                return turno.getNombreCompletoPaciente();
            }

            if (turno.getNombrePaciente() != null && turno.getApellidoPaciente() != null) {
                return turno.getNombrePaciente() + " " + turno.getApellidoPaciente();
            }

            return "Paciente ID: " + turno.getPacienteId();

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo nombre del paciente: " + e.getMessage());
            return "Paciente no disponible";
        }
    }

    private String formatearFechaParaDisplay(LocalDate fecha) {
        try {
            if (fecha == null) return "Fecha no disponible";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return fecha.format(formatter);

        } catch (Exception e) {
            System.out.println("❌ Error formateando fecha: " + fecha);
            return "Fecha inválida";
        }
    }

    private String formatearHoraParaDisplay(LocalTime hora) {
        try {
            if (hora == null) return "Hora no disponible";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return hora.format(formatter);

        } catch (Exception e) {
            System.out.println("❌ Error formateando hora: " + hora);
            return "Hora inválida";
        }
    }

    private String obtenerEmojiEstado(String estado) {
        if (estado == null) return "⚪";

        switch (estado.toUpperCase()) {
            case "CONFIRMADO": return "🟢";
            case "ASISTIDO": return "✅";
            case "CANCELADO": return "🔴";
            case "NO_ASISTIO": return "❌";
            default: return "⚪";
        }
    }

    // ========== HANDLERS DE MENÚ (SE MANTIENEN IGUAL) ==========
    @FXML
    private void handleAgenda() {
        try {
            System.out.println("Navegando a Agenda...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/agenda.fxml"));
            Parent root = loader.load();

            AgendaController controller = loader.getController();

            // Pasar información del stage actual
            Stage currentStage = (Stage) lblUsuario.getScene().getWindow();
            controller.setUsuario(usuarioActual, rolActual, currentStage, currentStage.isMaximized());

            Stage stage = new Stage();
            stage.setTitle("Cosmos - Agenda");

            // ✅ SOLUCIÓN CORREGIDA: CREAR SCENE PRIMERO, LUEGO APLICAR CSS
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

            // Cerrar dashboard actual
            currentStage.close();

        } catch (IOException e) {
            mostrarAlertaError("Error", "No se pudo abrir la Agenda", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTurnos() {
        System.out.println("Navegando a Turnos...");
        mostrarMensajeFuncionalidad("Gestión de Turnos");
    }

    @FXML
    private void handlePacientes() {
        System.out.println("Navegando a Pacientes...");
        mostrarMensajeFuncionalidad("Gestión de Pacientes");
    }

    @FXML
    private void handleTareas() {
        mostrarMensajeFuncionalidad("Gestión de Tareas");
    }

    @FXML
    private void handlePagos() {
        if (!"Secretaria".equals(rolActual)) {
            mostrarMensajeFuncionalidad("Gestión de Pagos");
        }
    }

    @FXML
    private void handleEstadisticas() {
        if (!"Secretaria".equals(rolActual)) {
            mostrarMensajeFuncionalidad("Estadísticas y Reportes");
        }
    }

    @FXML
    private void handleGestionUsuarios() {
        if (!"Secretaria".equals(rolActual)) {
            mostrarMensajeFuncionalidad("Gestión de Usuarios");
        }
    }

    @FXML
    private void handleMiPerfil() {
        mostrarMensajeFuncionalidad("Mi Perfil");
    }

    @FXML
    private void handleVerTodosTurnos() {
        System.out.println("Navegando a todos los turnos...");
        mostrarMensajeFuncionalidad("Gestión Completa de Turnos");
    }

    @FXML
    private void handleCerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Cosmos - Consultorio Psicopedagógico");
            Scene scene = new Scene(root, 800, 600);

            try {
                scene.getStylesheets().add(getClass().getResource("/com/consultorio/desktop/styles/styles.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("No se pudo cargar CSS: " + e.getMessage());
            }

            loginStage.setScene(scene);

            Stage currentStage = (Stage) lblUsuario.getScene().getWindow();
            loginStage.setMaximized(currentStage.isMaximized());

            loginStage.show();
            currentStage.close();

        } catch (IOException e) {
            mostrarAlertaError("Error", "No se pudo cerrar sesión", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarMensajeFuncionalidad(String modulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funcionalidad en Desarrollo");
        alert.setHeaderText("Módulo: " + modulo);
        alert.setContentText("Esta funcionalidad estará disponible próximamente.\n\nBackend: ✅ Conectado\nFrontend: 🚧 En desarrollo");
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    // Getters para testing
    public String getUsuarioActual() {
        return usuarioActual;
    }

    public String getRolActual() {
        return rolActual;
    }
}