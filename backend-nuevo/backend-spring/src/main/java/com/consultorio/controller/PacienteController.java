package com.consultorio.controller;

import com.consultorio.model.Paciente;
import com.consultorio.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    // GET: Obtener todos los pacientes (ordenados por fecha de creación, más nuevos primero)
    @GetMapping
    public List<Paciente> getAllPacientes() {
        return pacienteService.findAll();
    }

    // GET: Obtener paciente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Paciente> getPacienteById(@PathVariable Long id) {
        Optional<Paciente> paciente = pacienteService.findById(id);
        return paciente.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Obtener paciente por DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Paciente> getPacienteByDni(@PathVariable String dni) {
        Optional<Paciente> paciente = pacienteService.findByDni(dni);
        return paciente.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Obtener pacientes activos
    @GetMapping("/activos")
    public List<Paciente> getPacientesActivos() {
        return pacienteService.findPacientesActivos();
    }

    // GET: Buscar pacientes por nombre o apellido
    @GetMapping("/buscar")
    public List<Paciente> buscarPacientes(@RequestParam String termino) {
        return pacienteService.buscarPorNombreOApellido(termino);
    }

    // POST: Crear nuevo paciente
    @PostMapping
    public ResponseEntity<Paciente> createPaciente(
            @RequestBody Paciente paciente,
            @RequestHeader(value = "X-Usuario", required = false) String usuarioCreador) {
        try {
            Paciente pacienteGuardado = pacienteService.save(paciente, usuarioCreador);
            return ResponseEntity.ok(pacienteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT: Actualizar paciente
    @PutMapping("/{id}")
    public ResponseEntity<Paciente> updatePaciente(
            @PathVariable Long id,
            @RequestBody Paciente paciente,
            @RequestHeader(value = "X-Usuario", required = false) String usuarioModificador) {
        try {
            Paciente pacienteActualizado = pacienteService.update(id, paciente, usuarioModificador);
            return ResponseEntity.ok(pacienteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar paciente (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaciente(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario", required = false) String usuarioEliminador) {
        try {
            pacienteService.delete(id, usuarioEliminador);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT: Cambiar estado del paciente (activar/desactivar)
    @PutMapping("/{id}/estado")
    public ResponseEntity<Paciente> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            Paciente paciente = pacienteService.cambiarEstado(id, activo);
            return ResponseEntity.ok(paciente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT: Actualizar foto del paciente
    @PutMapping("/{id}/foto")
    public ResponseEntity<Paciente> actualizarFoto(@PathVariable Long id, @RequestParam String fotoPath) {
        try {
            Paciente paciente = pacienteService.actualizarFoto(id, fotoPath);
            return ResponseEntity.ok(paciente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Contar pacientes activos
    @GetMapping("/count/activos")
    public ResponseEntity<Long> countPacientesActivos() {
        long count = pacienteService.countPacientesActivos();
        return ResponseEntity.ok(count);
    }

    // GET: Buscar pacientes por nivel educativo
    @GetMapping("/nivel-educativo/{nivelEducativo}")
    public List<Paciente> getPacientesByNivelEducativo(@PathVariable Paciente.NivelEducativo nivelEducativo) {
        return pacienteService.findByNivelEducativo(nivelEducativo);
    }

    // GET: Buscar pacientes por condición
    @GetMapping("/condicion/{condicion}")
    public List<Paciente> getPacientesByCondicion(@PathVariable String condicion) {
        return pacienteService.findByCondicion(condicion);
    }
}

