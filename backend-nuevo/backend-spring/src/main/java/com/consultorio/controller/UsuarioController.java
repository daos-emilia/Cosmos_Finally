package com.consultorio.controller;

import com.consultorio.model.Usuario;
import com.consultorio.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // GET: Obtener todos los usuarios
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.findAll();
    }

    // GET: Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Obtener usuario por DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Usuario> getUsuarioByDni(@PathVariable String dni) {
        Optional<Usuario> usuario = usuarioService.findByDni(dni);
        return usuario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Login de usuario
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuario = usuarioService.login(loginRequest.getDni(), loginRequest.getPassword());
        return usuario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    // POST: Crear nuevo usuario
    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        return usuarioService.save(usuario);
    }

    // PUT: Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            Usuario usuarioActualizado = usuarioService.update(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar usuario (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        try {
            usuarioService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Buscar usuarios por tipo
    @GetMapping("/tipo/{tipo}")
    public List<Usuario> getUsuariosByTipo(@PathVariable Usuario.TipoUsuario tipo) {
        return usuarioService.findByTipo(tipo);
    }

    // GET: Buscar usuarios activos
    @GetMapping("/activos")
    public List<Usuario> getUsuariosActivos() {
        return usuarioService.findActivos();
    }

    // GET: Buscar por nombre o apellido
    @GetMapping("/buscar")
    public List<Usuario> buscarUsuarios(@RequestParam String termino) {
        return usuarioService.buscarPorNombreOApellido(termino);
    }

    // PUT: Cambiar estado de usuario
    @PutMapping("/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            Usuario usuario = usuarioService.cambiarEstado(id, activo);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT: Validar email de usuario (por ID - método viejo)
    @PutMapping("/{id}/validar-email")
    public ResponseEntity<Usuario> validarEmail(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.validarEmail(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ====== NUEVOS ENDPOINTS PARA VALIDACIÓN POR TOKEN ======

    /**
     * GET: Validar email mediante token (para el enlace del email)
     * Este es el endpoint que recibe el usuario cuando hace clic en el email
     */
    @GetMapping("/validar-email")
    public ResponseEntity<Map<String, Object>> validarEmailConToken(@RequestParam String token) {
        try {
            Usuario usuario = usuarioService.validarEmailConToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email validado exitosamente");
            response.put("usuario", usuario.getNombre() + " " + usuario.getApellido());
            response.put("email", usuario.getEmail());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * POST: Enviar recordatorio de contraseña por email
     * Solo funciona si el email está validado
     */
    @PostMapping("/recordatorio-password")
    public ResponseEntity<Map<String, Object>> enviarRecordatorioPassword(@RequestBody RecordatorioRequest request) {
        try {
            usuarioService.enviarRecordatorioPassword(request.getDni());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recordatorio de contraseña enviado al email registrado");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // GET: Usuarios con email no validado
    @GetMapping("/email-no-validado")
    public List<Usuario> getUsuariosEmailNoValidado() {
        return usuarioService.findByEmailValidadoFalse();
    }

    // GET: Usuarios por estado de validación de email
    @GetMapping("/email-validado/{validado}")
    public List<Usuario> getUsuariosPorValidacionEmail(@PathVariable Boolean validado) {
        return usuarioService.findByEmailValidado(validado);
    }

    // GET: Estadísticas de validación de email
    @GetMapping("/estadisticas-email")
    public Map<String, Object> getEstadisticasEmail() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = usuarioService.findAll().size();
        long validados = usuarioService.findByEmailValidado(true).size();
        long noValidados = usuarioService.findByEmailValidado(false).size();

        estadisticas.put("totalUsuarios", total);
        estadisticas.put("emailsValidados", validados);
        estadisticas.put("emailsNoValidados", noValidados);
        estadisticas.put("porcentajeValidados", total > 0 ? (validados * 100.0 / total) : 0);

        return estadisticas;
    }

    // ====== CLASES INTERNAS PARA REQUESTS ======

    // Clase interna para login request
    public static class LoginRequest {
        private String dni;
        private String password;

        // Getters y Setters
        public String getDni() { return dni; }
        public void setDni(String dni) { this.dni = dni; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // Clase interna para recordatorio de contraseña
    public static class RecordatorioRequest {
        private String dni;

        // Getters y Setters
        public String getDni() { return dni; }
        public void setDni(String dni) { this.dni = dni; }
    }
}
