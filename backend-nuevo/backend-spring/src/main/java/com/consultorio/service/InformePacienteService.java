package com.consultorio.service;

import com.consultorio.model.InformePaciente;
import com.consultorio.model.Paciente;
import com.consultorio.repository.InformePacienteRepository;
import com.consultorio.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InformePacienteService {

    @Autowired
    private InformePacienteRepository informeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // Obtener todos los informes
    public List<InformePaciente> findAll() {
        return informeRepository.findAll();
    }

    // Buscar informe por ID
    public Optional<InformePaciente> findById(Long id) {
        return informeRepository.findById(id);
    }

    // Obtener todos los informes de un paciente (ordenados por fecha, más nuevos primero)
    public List<InformePaciente> findByPacienteId(Long pacienteId) {
        return informeRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId);
    }

    // Crear nuevo informe
    @Transactional
    public InformePaciente save(InformePaciente informe) {
        // Verificar que el informe tenga un paciente asociado
        if (informe.getPaciente() == null || informe.getPaciente().getId() == null) {
            throw new RuntimeException("El informe debe tener un paciente asociado");
        }

        // Verificar que el paciente existe
        Paciente paciente = pacienteRepository.findById(informe.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + informe.getPaciente().getId()));

        // Asociar el paciente completo al informe
        informe.setPaciente(paciente);

        // Establecer auditoría
        informe.setCreadoPor("psicopedagoga");

        // Guardar el informe
        InformePaciente informeGuardado = informeRepository.save(informe);

        System.out.println("✅ Informe creado: " + informeGuardado.getTitulo() + " para paciente: " + paciente.getNombreCompleto());

        return informeGuardado;
    }

    // Actualizar informe
    @Transactional
    public InformePaciente update(Long id, InformePaciente informeActualizado) {
        return informeRepository.findById(id)
                .map(informeExistente -> {
                    informeExistente.setTitulo(informeActualizado.getTitulo());
                    informeExistente.setContenido(informeActualizado.getContenido());
                    informeExistente.setActualizadoPor("psicopedagoga");

                    return informeRepository.save(informeExistente);
                })
                .orElseThrow(() -> new RuntimeException("Informe no encontrado con ID: " + id));
    }

    // Eliminar informe
    @Transactional
    public void delete(Long id) {
        InformePaciente informe = informeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Informe no encontrado con ID: " + id));

        informeRepository.delete(informe);

        System.out.println("✅ Informe eliminado: " + informe.getTitulo());
    }

    // Contar informes de un paciente
    public long countByPacienteId(Long pacienteId) {
        return informeRepository.countByPacienteId(pacienteId);
    }

    // Buscar informes por título
    public List<InformePaciente> buscarPorTitulo(String termino) {
        return informeRepository.buscarPorTitulo(termino);
    }

    // Buscar informes de un paciente por título
    public List<InformePaciente> buscarPorPacienteYTitulo(Long pacienteId, String termino) {
        return informeRepository.buscarPorPacienteYTitulo(pacienteId, termino);
    }
}

