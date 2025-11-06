package com.consultorio.controller;

import com.consultorio.model.Paciente;
import com.consultorio.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = "*")
public class EstadisticasController {

    @Autowired
    private PacienteService pacienteService;

    /**
     * Obtener cantidad de nuevos pacientes en un periodo
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin Fecha de fin del periodo
     * @return Cantidad de pacientes registrados en ese periodo
     */
    @GetMapping("/nuevos-pacientes")
    public ResponseEntity<Map<String, Object>> getNuevosPacientes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        System.out.println("📊 [BACKEND] Endpoint /nuevos-pacientes llamado");
        System.out.println("   📅 Fecha inicio: " + fechaInicio);
        System.out.println("   📅 Fecha fin: " + fechaFin);
        
        List<Paciente> todosPacientes = pacienteService.findAll();
        System.out.println("   📋 Total pacientes en BD: " + todosPacientes.size());
        
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);
        
        long cantidad = todosPacientes.stream()
                .filter(p -> p.getFechaCreacion() != null)
                .filter(p -> !p.getFechaCreacion().isBefore(inicioDateTime) && 
                            !p.getFechaCreacion().isAfter(finDateTime))
                .count();
        
        System.out.println("   ✅ Pacientes en el periodo: " + cantidad);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cantidad", cantidad);
        resultado.put("fechaInicio", fechaInicio.toString());
        resultado.put("fechaFin", fechaFin.toString());
        resultado.put("periodo", calcularPeriodo(fechaInicio, fechaFin));
        
        System.out.println("   📤 Respuesta: " + resultado);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener distribución de pacientes activos por nivel educativo
     * @return Mapa con porcentajes por nivel educativo
     */
    @GetMapping("/nivel-educativo")
    public ResponseEntity<Map<String, Object>> getDistribucionNivelEducativo() {
        System.out.println("📊 [BACKEND] Endpoint /nivel-educativo llamado");
        
        List<Paciente> pacientesActivos = pacienteService.findPacientesActivos();
        System.out.println("   📋 Pacientes activos: " + pacientesActivos.size());
        
        long total = pacientesActivos.size();
        
        Map<String, Long> conteo = new HashMap<>();
        conteo.put("INICIAL", 0L);
        conteo.put("PRIMARIA", 0L);
        conteo.put("SECUNDARIA", 0L);
        conteo.put("UNIVERSIDAD", 0L);
        
        for (Paciente p : pacientesActivos) {
            String nivel = p.getNivelEducativo().name();
            conteo.put(nivel, conteo.get(nivel) + 1);
        }
        
        System.out.println("   📊 Conteo por nivel: " + conteo);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", total);
        resultado.put("conteo", conteo);
        
        Map<String, Double> porcentajes = new HashMap<>();
        for (Map.Entry<String, Long> entry : conteo.entrySet()) {
            double porcentaje = total > 0 ? (entry.getValue() * 100.0 / total) : 0.0;
            porcentajes.put(entry.getKey(), Math.round(porcentaje * 100.0) / 100.0);
        }
        resultado.put("porcentajes", porcentajes);
        
        System.out.println("   📤 Respuesta: " + resultado);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener distribución de pacientes activos por condición
     * @return Mapa con porcentajes por condición
     */
    @GetMapping("/condicion")
    public ResponseEntity<Map<String, Object>> getDistribucionCondicion() {
        System.out.println("📊 [BACKEND] Endpoint /condicion llamado");
        
        List<Paciente> pacientesActivos = pacienteService.findPacientesActivos();
        System.out.println("   📋 Pacientes activos: " + pacientesActivos.size());
        
        long total = pacientesActivos.size();
        
        Map<String, Long> conteo = new HashMap<>();
        
        for (Paciente p : pacientesActivos) {
            String condicion = p.getCondicion();
            if (condicion != null && !condicion.isEmpty()) {
                conteo.put(condicion, conteo.getOrDefault(condicion, 0L) + 1);
            }
        }
        
        System.out.println("   📊 Conteo por condición: " + conteo);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", total);
        resultado.put("conteo", conteo);
        
        Map<String, Double> porcentajes = new HashMap<>();
        for (Map.Entry<String, Long> entry : conteo.entrySet()) {
            double porcentaje = total > 0 ? (entry.getValue() * 100.0 / total) : 0.0;
            porcentajes.put(entry.getKey(), Math.round(porcentaje * 100.0) / 100.0);
        }
        resultado.put("porcentajes", porcentajes);
        
        System.out.println("   📤 Respuesta: " + resultado);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener demanda mensual de turnos del año actual
     * @return Mapa con porcentajes por mes
     */
    @GetMapping("/demanda-mensual")
    public ResponseEntity<Map<String, Object>> getDemandaMensual() {
        System.out.println("📊 [BACKEND] Endpoint /demanda-mensual llamado");
        
        // TODO: Implementar cuando el módulo de turnos esté completo
        // Por ahora devolvemos datos de ejemplo
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("anio", LocalDate.now().getYear());
        resultado.put("mensaje", "Módulo de turnos pendiente de implementación");
        
        Map<String, Integer> conteoMensual = new HashMap<>();
        Map<String, Double> porcentajesMensual = new HashMap<>();
        
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        
        for (String mes : meses) {
            conteoMensual.put(mes, 0);
            porcentajesMensual.put(mes, 0.0);
        }
        
        resultado.put("conteoMensual", conteoMensual);
        resultado.put("porcentajesMensual", porcentajesMensual);
        
        System.out.println("   📤 Respuesta: " + resultado);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Método auxiliar para calcular el tipo de periodo
     */
    private String calcularPeriodo(LocalDate inicio, LocalDate fin) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin);
        
        if (dias == 0) return "1 día";
        if (dias <= 7) return "1 semana";
        if (dias <= 31) return "1 mes";
        if (dias <= 186) return "1 semestre";
        if (dias <= 365) return "1 año";
        
        return dias + " días";
    }
}

