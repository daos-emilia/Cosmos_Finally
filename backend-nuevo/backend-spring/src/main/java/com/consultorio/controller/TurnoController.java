package com.consultorio.controller;

import com.consultorio.model.Turno;
import com.consultorio.repository.TurnoRepository;
import com.consultorio.service.TurnoService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private EntityManager entityManager;

    private String convertirDiaSemana(DayOfWeek dayOfWeek) {
        Map<DayOfWeek, String> dias = new HashMap<>();
        dias.put(DayOfWeek.MONDAY, "LUNES");
        dias.put(DayOfWeek.TUESDAY, "MARTES");
        dias.put(DayOfWeek.WEDNESDAY, "MIERCOLES");
        dias.put(DayOfWeek.THURSDAY, "JUEVES");
        dias.put(DayOfWeek.FRIDAY, "VIERNES");
        dias.put(DayOfWeek.SATURDAY, "SABADO");
        dias.put(DayOfWeek.SUNDAY, "DOMINGO");
        return dias.get(dayOfWeek);
    }

    @GetMapping
    public List<Turno> getAllTurnos() {
        return turnoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turno> getTurnoById(@PathVariable Long id) {
        Optional<Turno> turno = turnoService.findById(id);
        return turno.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Turno createTurno(@RequestBody Turno turno) {
        return turnoService.save(turno);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turno> updateTurno(@PathVariable Long id, @RequestBody Turno turno) {
        try {
            Turno turnoActualizado = turnoService.update(id, turno);
            return ResponseEntity.ok(turnoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurno(@PathVariable Long id) {
        try {
            turnoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/proximos")
    public ResponseEntity<List<Turno>> getProximosTurnos() {
        try {
            System.out.println("\n=== ENDPOINT /proximos INICIADO ===");
            List<Turno> proximos = turnoService.getProximosTurnos();
            System.out.println("📋 Devolviendo " + proximos.size() + " turnos próximos");
            return ResponseEntity.ok(proximos);
        } catch (Exception e) {
            System.out.println("❌ ERROR en endpoint /proximos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/fecha/{fecha}")
    public List<Turno> getTurnosPorFecha(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return turnoService.getTurnosPorFecha(fecha);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<Turno> getTurnosPorPaciente(@PathVariable Long pacienteId) {
        return turnoService.getTurnosPorPaciente(pacienteId);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Turno> cambiarEstadoTurno(@PathVariable Long id, @RequestParam Turno.EstadoTurno estado) {
        try {
            Turno turnoActualizado = turnoService.cambiarEstado(id, estado);
            return ResponseEntity.ok(turnoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibilidad/{fecha}")
    public ResponseEntity<List<String>> generarHorariosDisponibles(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            System.out.println("\n🔍 ===== GENERANDO HORARIOS DISPONIBLES =====");
            System.out.println("📅 Fecha solicitada: " + fecha);

            String diaSemanaEspanol = convertirDiaSemana(fecha.getDayOfWeek());
            System.out.println("🔍 Buscando configuración para: " + diaSemanaEspanol);

            List<Object[]> configuraciones = entityManager.createNativeQuery(
                    "SELECT inicio_manana, fin_manana, inicio_tarde, fin_tarde, " +
                            "duracion_sesion, tiempo_descanso, turno_manana, turno_tarde, activo " +
                            "FROM configuracion_horaria " +
                            "WHERE dia_semana = CAST(?1 AS dia_semana_enum) " +
                            "AND activo = true"
            ).setParameter(1, diaSemanaEspanol).getResultList();

            if (configuraciones.isEmpty()) {
                System.out.println("⚠️ No hay configuración activa para " + diaSemanaEspanol);
                return ResponseEntity.ok(new ArrayList<>());
            }

            Object[] config = configuraciones.get(0);

            LocalTime inicioManana = config[0] != null ? ((java.sql.Time) config[0]).toLocalTime() : null;
            LocalTime finManana = config[1] != null ? ((java.sql.Time) config[1]).toLocalTime() : null;
            LocalTime inicioTarde = config[2] != null ? ((java.sql.Time) config[2]).toLocalTime() : null;
            LocalTime finTarde = config[3] != null ? ((java.sql.Time) config[3]).toLocalTime() : null;
            Integer duracionSesion = (Integer) config[4];
            Integer tiempoDescanso = (Integer) config[5];
            Boolean turnoManana = (Boolean) config[6];
            Boolean turnoTarde = (Boolean) config[7];

            System.out.println("⏰ Configuración encontrada:");
            System.out.println("   Mañana: " + (turnoManana ? inicioManana + " - " + finManana : "No disponible"));
            System.out.println("   Tarde: " + (turnoTarde ? inicioTarde + " - " + finTarde : "No disponible"));

            List<Turno> turnosConfirmados = turnoRepository.findByFecha(fecha);
            List<LocalTime> horasOcupadas = new ArrayList<>();

            for (Turno turno : turnosConfirmados) {
                if (turno.getEstado() == Turno.EstadoTurno.CONFIRMADO) {
                    horasOcupadas.add(turno.getHoraInicio());
                }
            }

            List<String> horariosDisponibles = new ArrayList<>();
            int tiempoTotalSlot = duracionSesion + tiempoDescanso;

            if (Boolean.TRUE.equals(turnoManana) && inicioManana != null && finManana != null) {
                LocalTime horaActual = inicioManana;
                while (horaActual.plusMinutes(duracionSesion).isBefore(finManana) ||
                        horaActual.plusMinutes(duracionSesion).equals(finManana)) {
                    if (!horasOcupadas.contains(horaActual)) {
                        horariosDisponibles.add(horaActual.toString());
                    }
                    horaActual = horaActual.plusMinutes(tiempoTotalSlot);
                }
            }

            if (Boolean.TRUE.equals(turnoTarde) && inicioTarde != null && finTarde != null) {
                LocalTime horaActual = inicioTarde;
                while (horaActual.plusMinutes(duracionSesion).isBefore(finTarde) ||
                        horaActual.plusMinutes(duracionSesion).equals(finTarde)) {
                    if (!horasOcupadas.contains(horaActual)) {
                        horariosDisponibles.add(horaActual.toString());
                    }
                    horaActual = horaActual.plusMinutes(tiempoTotalSlot);
                }
            }

            System.out.println("✅ TOTAL HORARIOS DISPONIBLES: " + horariosDisponibles.size());
            return ResponseEntity.ok(horariosDisponibles);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/disponible")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
        boolean disponible = turnoService.estaDisponible(fecha, horaInicio, horaFin);
        return ResponseEntity.ok(disponible);
    }
}
