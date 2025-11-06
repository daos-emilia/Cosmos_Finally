package com.consultorio.service;

import com.consultorio.model.Paciente;
import com.consultorio.model.Usuario;
import com.consultorio.repository.PacienteRepository;
import com.consultorio.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Obtener todos los pacientes ordenados por fecha de creación (más nuevos primero)
    public List<Paciente> findAll() {
        return pacienteRepository.findAllOrderByFechaCreacionDesc();
    }

    // Buscar paciente por ID
    public Optional<Paciente> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    // Buscar paciente por ID de usuario
    public Optional<Paciente> findByUsuarioId(Long usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId);
    }

    // Buscar paciente por DNI
    public Optional<Paciente> findByDni(String dni) {
        return pacienteRepository.findByUsuarioDni(dni);
    }

    // Buscar pacientes activos
    public List<Paciente> findPacientesActivos() {
        return pacienteRepository.findPacientesActivos();
    }

    // Buscar pacientes por nombre o apellido
    public List<Paciente> buscarPorNombreOApellido(String termino) {
        return pacienteRepository.buscarPorNombreOApellido(termino);
    }

    // Crear nuevo paciente (con usuario asociado)
    @Transactional
    public Paciente save(Paciente paciente) {
        return save(paciente, null);
    }

    @Transactional
    public Paciente save(Paciente paciente, String usuarioCreador) {
        // Verificar que el paciente tenga un usuario asociado
        if (paciente.getUsuario() == null) {
            throw new RuntimeException("El paciente debe tener un usuario asociado");
        }

        Usuario usuario = paciente.getUsuario();

        // Verificar que el usuario sea de tipo PACIENTE
        if (usuario.getTipo() != Usuario.TipoUsuario.PACIENTE) {
            throw new RuntimeException("El usuario debe ser de tipo PACIENTE");
        }

        // Establecer contraseña por defecto si no tiene
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword("Primera_Vez");
        }

        // Establecer el creador del usuario
        String creador = (usuarioCreador != null && !usuarioCreador.isEmpty()) ? usuarioCreador : "Sistema";
        usuario.setCreadoPor(creador);

        // Guardar el usuario primero (esto también envía el email de bienvenida)
        Usuario usuarioGuardado = usuarioService.save(usuario);

        // Asociar el usuario guardado al paciente
        paciente.setUsuario(usuarioGuardado);

        // Establecer el creador del paciente
        paciente.setCreadoPor(creador);

        // Guardar el paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        System.out.println("✅ Paciente creado: " + pacienteGuardado.getNombreCompleto() + " por " + creador);

        return pacienteGuardado;
    }

    // Actualizar paciente
    @Transactional
    public Paciente update(Long id, Paciente pacienteActualizado) {
        return update(id, pacienteActualizado, null);
    }

    @Transactional
    public Paciente update(Long id, Paciente pacienteActualizado, String usuarioModificador) {
        return pacienteRepository.findById(id)
                .map(pacienteExistente -> {
                    String modificador = (usuarioModificador != null && !usuarioModificador.isEmpty()) ? usuarioModificador : "Sistema";

                    // Actualizar datos del usuario asociado
                    if (pacienteActualizado.getUsuario() != null) {
                        Usuario usuarioExistente = pacienteExistente.getUsuario();
                        Usuario usuarioActualizado = pacienteActualizado.getUsuario();

                        usuarioExistente.setNombre(usuarioActualizado.getNombre());
                        usuarioExistente.setApellido(usuarioActualizado.getApellido());
                        usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
                        usuarioExistente.setEmail(usuarioActualizado.getEmail());
                        usuarioExistente.setActualizadoPor(modificador);

                        usuarioRepository.save(usuarioExistente);
                    }

                    // Actualizar datos del paciente
                    pacienteExistente.setFechaNacimiento(pacienteActualizado.getFechaNacimiento());
                    pacienteExistente.setNivelEducativo(pacienteActualizado.getNivelEducativo());
                    pacienteExistente.setCondicion(pacienteActualizado.getCondicion());
                    pacienteExistente.setAntecedentes(pacienteActualizado.getAntecedentes());
                    pacienteExistente.setObservaciones(pacienteActualizado.getObservaciones());
                    pacienteExistente.setFotoPath(pacienteActualizado.getFotoPath());
                    pacienteExistente.setActualizadoPor(modificador);

                    return pacienteRepository.save(pacienteExistente);
                })
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
    }

    // Eliminar paciente (borrado lógico - desactiva el usuario)
    @Transactional
    public void delete(Long id) {
        delete(id, null);
    }

    @Transactional
    public void delete(Long id, String usuarioEliminador) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

        String eliminador = (usuarioEliminador != null && !usuarioEliminador.isEmpty()) ? usuarioEliminador : "Sistema";

        // Desactivar el usuario asociado
        Usuario usuario = paciente.getUsuario();
        usuario.setActivo(false);
        usuario.setActualizadoPor(eliminador);
        usuarioRepository.save(usuario);

        System.out.println("✅ Paciente dado de baja: " + paciente.getNombreCompleto() + " por " + eliminador);
    }

    // Cambiar estado del paciente (activar/desactivar)
    @Transactional
    public Paciente cambiarEstado(Long id, boolean activo) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

        Usuario usuario = paciente.getUsuario();
        usuario.setActivo(activo);
        usuario.setActualizadoPor("sistema");
        usuarioRepository.save(usuario);

        return paciente;
    }

    // Actualizar foto del paciente
    @Transactional
    public Paciente actualizarFoto(Long id, String fotoPath) {
        return pacienteRepository.findById(id)
                .map(paciente -> {
                    paciente.setFotoPath(fotoPath);
                    paciente.setActualizadoPor("sistema");
                    return pacienteRepository.save(paciente);
                })
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
    }

    // Contar pacientes activos
    public long countPacientesActivos() {
        return pacienteRepository.countPacientesActivos();
    }

    // Buscar pacientes por nivel educativo
    public List<Paciente> findByNivelEducativo(Paciente.NivelEducativo nivelEducativo) {
        return pacienteRepository.findByNivelEducativo(nivelEducativo);
    }

    // Buscar pacientes por condición
    public List<Paciente> findByCondicion(String condicion) {
        return pacienteRepository.findByCondicion(condicion);
    }
}

