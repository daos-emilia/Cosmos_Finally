package com.consultorio.service;

import com.consultorio.model.Usuario;
import com.consultorio.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    // Obtener todos los usuarios
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Buscar usuario por ID
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    // Buscar usuario por DNI
    public Optional<Usuario> findByDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    // Login de usuario
    public Optional<Usuario> login(String dni, String password) {
        return usuarioRepository.findByDniAndPassword(dni, password);
    }

    // Crear nuevo usuario
    public Usuario save(Usuario usuario) {
        // Verificar si el DNI ya existe
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new RuntimeException("Ya existe un usuario con el DNI: " + usuario.getDni());
        }

        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + usuario.getEmail());
        }

        // ESTABLECER VALORES POR DEFECTO Y AUDITORÍA
        usuario.generarTokenValidacion();  // Generar token único
        usuario.setEmailValidado(false);   // Email no validado por defecto
        usuario.setActivo(true);           // Usuario activo por defecto
        usuario.setCreadoPor("sistema");   // Auditoría

        // PRIMERO guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // LUEGO enviar el email (esto puede fallar pero el usuario ya está guardado)
        try {
            emailService.enviarEmailBienvenidaYValidacion(
                    usuarioGuardado.getEmail(),
                    usuarioGuardado.getNombre(),
                    usuarioGuardado.getApellido(),
                    usuarioGuardado.getTokenValidacion()
            );
            System.out.println("✅ Email de validación enviado a: " + usuarioGuardado.getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando email de validación: " + e.getMessage());
            // No interrumpimos el registro aunque falle el email
        }

        return usuarioGuardado;
    }

    // Actualizar usuario
    public Usuario update(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    // Actualizar campos permitidos
                    usuarioExistente.setNombre(usuarioActualizado.getNombre());
                    usuarioExistente.setApellido(usuarioActualizado.getApellido());
                    usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
                    usuarioExistente.setEmail(usuarioActualizado.getEmail());
                    usuarioExistente.setPassword(usuarioActualizado.getPassword());
                    usuarioExistente.setMatricula(usuarioActualizado.getMatricula());
                    usuarioExistente.setActivo(usuarioActualizado.getActivo());

                    // Auditoría
                    usuarioExistente.setActualizadoPor("sistema");

                    return usuarioRepository.save(usuarioExistente);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // Eliminar usuario (borrado lógico)
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuario.setActualizadoPor("sistema");
        usuarioRepository.save(usuario);
    }

    // Buscar usuarios por tipo
    public List<Usuario> findByTipo(Usuario.TipoUsuario tipo) {
        return usuarioRepository.findByTipo(tipo);
    }

    // Buscar usuarios activos
    public List<Usuario> findActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    // Buscar por nombre o apellido
    public List<Usuario> buscarPorNombreOApellido(String termino) {
        return usuarioRepository.buscarPorNombreOApellido(termino);
    }

    // Verificar si existe usuario por DNI
    public boolean existePorDni(String dni) {
        return usuarioRepository.existsByDni(dni);
    }

    // Contar usuarios por tipo
    public long contarPorTipo(Usuario.TipoUsuario tipo) {
        return usuarioRepository.countByTipo(tipo);
    }

    // Activar/Desactivar usuario
    public Usuario cambiarEstado(Long id, boolean activo) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setActivo(activo);
                    usuario.setActualizadoPor("sistema");
                    return usuarioRepository.save(usuario);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // Validar email (método viejo por ID - lo mantenemos por compatibilidad)
    public Usuario validarEmail(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setEmailValidado(true);
                    usuario.setActualizadoPor("sistema");
                    return usuarioRepository.save(usuario);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // Buscar usuarios con email no validado
    public List<Usuario> findByEmailValidadoFalse() {
        return usuarioRepository.findByEmailValidadoFalse();
    }

    // Buscar usuarios por email validado
    public List<Usuario> findByEmailValidado(Boolean validado) {
        return usuarioRepository.findByEmailValidado(validado);
    }

    // ====== NUEVOS MÉTODOS PARA VALIDACIÓN POR TOKEN ======

    /**
     * Validar email mediante token
     */
    public Usuario validarEmailConToken(String token) {
        Usuario usuario = usuarioRepository.findByTokenValidacion(token)
                .orElseThrow(() -> new RuntimeException("Token de validación inválido"));

        if (!usuario.isTokenValido(token)) {
            throw new RuntimeException("Token expirado o inválido");
        }

        usuario.setEmailValidado(true);
        usuario.limpiarToken(); // Limpiar token después de uso
        usuario.setActualizadoPor("sistema"); // Auditoría

        return usuarioRepository.save(usuario);
    }

    /**
     * Enviar recordatorio de contraseña - SOLO si email está validado
     */
    public void enviarRecordatorioPassword(String dni) {
        Usuario usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getEmailValidado()) {
            throw new RuntimeException("El email no está validado. No se puede enviar recordatorio.");
        }

        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Enviar email con la contraseña actual (NO la modificamos)
        emailService.enviarRecordatorioPassword(
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getPassword()
        );

        System.out.println("✅ Recordatorio de contraseña enviado a: " + usuario.getEmail());
    }
}