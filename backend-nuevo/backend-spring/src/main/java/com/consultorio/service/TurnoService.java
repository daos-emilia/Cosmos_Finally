package com.consultorio.service;

import com.consultorio.model.Turno;
import com.consultorio.repository.TurnoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private EntityManager entityManager;

    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    public Optional<Turno> findById(Long id) {
        return turnoRepository.findById(id);
    }

    public Turno save(Turno turno) {
        return turnoRepository.save(turno);
    }

    public Turno update(Long id, Turno turnoActualizado) {
        return turnoRepository.findById(id)
                .map(turno -> {
                    turno.setFecha(turnoActualizado.getFecha());
                    turno.setHoraInicio(turnoActualizado.getHoraInicio());
                    turno.setHoraFin(turnoActualizado.getHoraFin());
                    turno.setEstado(turnoActualizado.getEstado());
                    turno.setTipo(turnoActualizado.getTipo());
                    turno.setMonto(turnoActualizado.getMonto());
                    turno.setPagoConfirmado(turnoActualizado.getPagoConfirmado());
                    return turnoRepository.save(turno);
                })
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con id: " + id));
    }

    public void delete(Long id) {
        if (!turnoRepository.existsById(id)) {
            throw new RuntimeException("Turno no encontrado con id: " + id);
        }
        turnoRepository.deleteById(id);
    }

    public List<Turno> getTurnosPorFecha(LocalDate fecha) {
        return turnoRepository.findByFecha(fecha);
    }

    public List<Turno> getTurnosPorPaciente(Long pacienteId) {
        return turnoRepository.findByPacienteId(pacienteId);
    }

    public Turno cambiarEstado(Long id, Turno.EstadoTurno nuevoEstado) {
        return turnoRepository.findById(id)
                .map(turno -> {
                    turno.setEstado(nuevoEstado);
                    return turnoRepository.save(turno);
                })
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con id: " + id));
    }

    public boolean estaDisponible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<Turno> turnosEnFecha = turnoRepository.findByFecha(fecha);

        for (Turno turno : turnosEnFecha) {
            if (turno.getEstado() == Turno.EstadoTurno.CONFIRMADO) {
                boolean horasSuperpuestas =
                        (horaInicio.isBefore(turno.getHoraFin()) && horaFin.isAfter(turno.getHoraInicio()));

                if (horasSuperpuestas) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Turno> getProximosTurnos() {
        System.out.println("=== GET PROXIMOS TURNOS INICIADO ===");

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        System.out.println("📅 Fecha actual: " + hoy);
        System.out.println("⏰ Hora actual: " + ahora);

        List<Turno> todosTurnos = turnoRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(hoy);

        System.out.println("🔍 Turnos desde hoy encontrados: " + todosTurnos.size());

        // Filtrar turnos: si es hoy, solo los que tengan hora >= ahora
        List<Turno> turnosFuturos = todosTurnos.stream()
                .filter(turno -> {
                    if (turno.getFecha().isEqual(hoy)) {
                        // Si es hoy, solo incluir si la hora es futura
                        boolean esFuturo = turno.getHoraInicio().isAfter(ahora);
                        if (!esFuturo) {
                            System.out.println("   ⏭️ Turno ID " + turno.getId() + " excluido (hora pasada: " + turno.getHoraInicio() + ")");
                        }
                        return esFuturo;
                    }
                    // Si es fecha futura, incluir
                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("🔍 Turnos futuros después de filtrar: " + turnosFuturos.size());

        for (Turno turno : turnosFuturos) {
            try {
                List<Object[]> resultados = entityManager.createNativeQuery(
                        "SELECT u.nombre, u.apellido, u.dni " +
                                "FROM turnos t " +
                                "JOIN pacientes p ON t.paciente_id = p.id " +
                                "JOIN usuarios u ON p.usuario_id = u.id " +
                                "WHERE t.id = ?1"
                ).setParameter(1, turno.getId()).getResultList();

                if (!resultados.isEmpty()) {
                    Object[] datos = resultados.get(0);
                    turno.setNombrePaciente((String) datos[0]);
                    turno.setApellidoPaciente((String) datos[1]);
                    turno.setDniPaciente((String) datos[2]);

                    System.out.println("   ✅ Turno ID: " + turno.getId() +
                            ", Paciente: " + turno.getNombreCompletoPaciente() +
                            ", Fecha: " + turno.getFecha() +
                            ", Hora: " + turno.getHoraInicio());
                }
            } catch (Exception e) {
                System.out.println("   ⚠️ Error obteniendo datos del turno " + turno.getId() + ": " + e.getMessage());
            }
        }

        return turnosFuturos;
    }
}
