package com.consultorio.controller;

import com.consultorio.model.InformePaciente;
import com.consultorio.service.InformePacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/informes")
@CrossOrigin(origins = "*")
public class InformePacienteController {

    @Autowired
    private InformePacienteService informeService;

    // GET: Obtener todos los informes
    @GetMapping
    public List<InformePaciente> getAllInformes() {
        return informeService.findAll();
    }

    // GET: Obtener informe por ID
    @GetMapping("/{id}")
    public ResponseEntity<InformePaciente> getInformeById(@PathVariable Long id) {
        Optional<InformePaciente> informe = informeService.findById(id);
        return informe.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Obtener todos los informes de un paciente
    @GetMapping("/paciente/{pacienteId}")
    public List<InformePaciente> getInformesByPaciente(@PathVariable Long pacienteId) {
        return informeService.findByPacienteId(pacienteId);
    }

    // POST: Crear nuevo informe
    @PostMapping
    public ResponseEntity<InformePaciente> createInforme(@RequestBody InformePaciente informe) {
        try {
            InformePaciente informeGuardado = informeService.save(informe);
            return ResponseEntity.ok(informeGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT: Actualizar informe
    @PutMapping("/{id}")
    public ResponseEntity<InformePaciente> updateInforme(@PathVariable Long id, @RequestBody InformePaciente informe) {
        try {
            InformePaciente informeActualizado = informeService.update(id, informe);
            return ResponseEntity.ok(informeActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar informe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInforme(@PathVariable Long id) {
        try {
            informeService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Contar informes de un paciente
    @GetMapping("/count/paciente/{pacienteId}")
    public ResponseEntity<Long> countInformesByPaciente(@PathVariable Long pacienteId) {
        long count = informeService.countByPacienteId(pacienteId);
        return ResponseEntity.ok(count);
    }

    // GET: Buscar informes por título
    @GetMapping("/buscar")
    public List<InformePaciente> buscarInformes(@RequestParam String termino) {
        return informeService.buscarPorTitulo(termino);
    }

    // GET: Buscar informes de un paciente por título
    @GetMapping("/paciente/{pacienteId}/buscar")
    public List<InformePaciente> buscarInformesPorPaciente(@PathVariable Long pacienteId, @RequestParam String termino) {
        return informeService.buscarPorPacienteYTitulo(pacienteId, termino);
    }
}

