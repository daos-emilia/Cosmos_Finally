package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.Usuario;
import com.consultorio.desktop.utils.HttpClientUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FormularioUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private VBox vboxMatricula;
    @FXML private TextField txtMatricula;
    @FXML private Label lblInfoPassword;
    @FXML private Button btnGuardar;

    private Usuario usuarioActual;
    private String nombreUsuario;
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Configurar ComboBox de tipo de usuario - Solo Paciente y Secretaria
        cmbTipo.setItems(FXCollections.observableArrayList(
            "Paciente", "Secretaria"
        ));

        // Listener para mostrar/ocultar campo de matrícula (aunque ya no se usa)
        cmbTipo.setOnAction(e -> {
            String tipoSeleccionado = cmbTipo.getValue();
            if ("Psicopedagoga".equals(tipoSeleccionado)) {
                vboxMatricula.setVisible(true);
                vboxMatricula.setManaged(true);
            } else {
                vboxMatricula.setVisible(false);
                vboxMatricula.setManaged(false);
                txtMatricula.clear();
            }
        });
    }

    public void setModoCreacion(String usuario) {
        this.nombreUsuario = usuario;
        this.modoEdicion = false;
        lblTitulo.setText("Agregar Usuario");
        lblInfoPassword.setText("Solo el propio usuario puede cambiar su contraseña desde su perfil.");
    }

    public void setModoEdicion(Usuario usuario, String nombreUsuarioActual) {
        this.usuarioActual = usuario;
        this.nombreUsuario = nombreUsuarioActual;
        this.modoEdicion = true;

        lblTitulo.setText("Modificar Usuario");
        lblInfoPassword.setText("Solo el propio usuario puede cambiar su contraseña desde su perfil.");

        // Cargar datos del usuario
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtDni.setText(usuario.getDni());
        txtDni.setDisable(true); // No se puede modificar el DNI
        txtTelefono.setText(usuario.getTelefono());
        txtEmail.setText(usuario.getEmail());

        // Seleccionar tipo de usuario
        cmbTipo.setValue(usuario.getTipoFormateado());

        // Si es psicopedagoga, mostrar matrícula
        if ("PSICOPEDAGOGA".equalsIgnoreCase(usuario.getTipo())) {
            vboxMatricula.setVisible(true);
            vboxMatricula.setManaged(true);
            txtMatricula.setText(usuario.getMatricula());
        }
    }

    @FXML
    private void handleGuardar() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        if (modoEdicion) {
            modificarUsuario();
        } else {
            crearUsuario();
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

        // Validar tipo de usuario
        if (cmbTipo.getValue() == null) {
            errores.append("- Debe seleccionar un tipo de usuario\n");
        }

        // Validar matrícula para psicopedagoga
        if ("Psicopedagoga".equals(cmbTipo.getValue()) && txtMatricula.getText().trim().isEmpty()) {
            errores.append("- La matrícula es obligatoria para psicopedagogas\n");
        }

        if (errores.length() > 0) {
            mostrarError("Campos incompletos o inválidos", "Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean validarEmail(String email) {
        // Validar que tenga formato correcto y termine en .com
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$";
        return email.matches(regex);
    }

    private void crearUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(txtNombre.getText().trim());
        nuevoUsuario.setApellido(txtApellido.getText().trim());
        nuevoUsuario.setDni(txtDni.getText().trim());
        nuevoUsuario.setTelefono(txtTelefono.getText().trim());
        nuevoUsuario.setEmail(txtEmail.getText().trim());
        nuevoUsuario.setPassword("Primera_Vez"); // Contraseña por defecto
        nuevoUsuario.setEmailValidado(false);
        nuevoUsuario.setActivo(true);

        // Convertir tipo de usuario a formato del backend
        String tipoSeleccionado = cmbTipo.getValue();
        String tipoBackend = convertirTipoABackend(tipoSeleccionado);
        nuevoUsuario.setTipo(tipoBackend);

        // Si es psicopedagoga, agregar matrícula
        if ("PSICOPEDAGOGA".equals(tipoBackend)) {
            nuevoUsuario.setMatricula(txtMatricula.getText().trim());
        }

        // Crear usuario en el backend
        boolean exito = HttpClientUtil.crearUsuario(nuevoUsuario, nombreUsuario);

        if (exito) {
            String emailUsuario = nuevoUsuario.getEmail();
            mostrarExito("Usuario creado",
                "El usuario ha sido creado exitosamente.\n\n" +
                "Se ha enviado un email de bienvenida con la contraseña por defecto a:\n" +
                emailUsuario);
            cerrarVentana();
        } else {
            mostrarError("Error", "No se pudo crear el usuario. Verifique que el DNI y email no estén registrados.");
        }
    }

    private void modificarUsuario() {
        usuarioActual.setNombre(txtNombre.getText().trim());
        usuarioActual.setApellido(txtApellido.getText().trim());
        usuarioActual.setTelefono(txtTelefono.getText().trim());
        usuarioActual.setEmail(txtEmail.getText().trim());

        // Convertir tipo de usuario a formato del backend
        String tipoSeleccionado = cmbTipo.getValue();
        String tipoBackend = convertirTipoABackend(tipoSeleccionado);
        usuarioActual.setTipo(tipoBackend);

        // Si es psicopedagoga, actualizar matrícula
        if ("PSICOPEDAGOGA".equals(tipoBackend)) {
            usuarioActual.setMatricula(txtMatricula.getText().trim());
        } else {
            usuarioActual.setMatricula(null);
        }

        // Actualizar usuario en el backend
        boolean exito = HttpClientUtil.actualizarUsuario(usuarioActual, nombreUsuario);

        if (exito) {
            mostrarExito("Usuario actualizado", "Los datos del usuario han sido actualizados exitosamente.");
            cerrarVentana();
        } else {
            mostrarError("Error", "No se pudo actualizar el usuario.");
        }
    }

    private String convertirTipoABackend(String tipoFormateado) {
        switch (tipoFormateado) {
            case "Paciente":
                return "PACIENTE";
            case "Secretaria":
                return "SECRETARIA";
            case "Psicopedagoga":
                return "PSICOPEDAGOGA";
            default:
                return tipoFormateado.toUpperCase();
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
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

