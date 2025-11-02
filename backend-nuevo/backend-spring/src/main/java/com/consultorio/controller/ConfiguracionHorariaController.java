package com.consultorio.controller;

import com.consultorio.model.ConfiguracionHoraria;
import com.consultorio.service.ConfiguracionHorariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/configuracion-horaria")
@CrossOrigin(origins = "*")
public class ConfiguracionHorariaController {

    @Autowired
    private ConfiguracionHorariaService configuracionHorariaService;

    @GetMapping
    public List<ConfiguracionHoraria> getAllConfiguraciones() {
        return configuracionHorariaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionHoraria> getConfiguracionById(@PathVariable Long id) {
        Optional<ConfiguracionHoraria> configuracion = configuracionHorariaService.findById(id);
        return configuracion.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activas")
    public List<ConfiguracionHoraria> getConfiguracionesActivas() {
        return configuracionHorariaService.getConfiguracionesActivas();
    }

    // ✅ NUEVO ENDPOINT PARA ACTUALIZAR CONFIGURACIÓN COMPLETA
    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizarConfiguraciones(@RequestBody List<ConfiguracionHoraria> configuraciones,
                                                            @RequestParam String usuario) {
        try {
            System.out.println("💾 Actualizando configuración horaria por: " + usuario);

            for (ConfiguracionHoraria config : configuraciones) {
                // Actualizar cada configuración
                configuracionHorariaService.update(config.getId(), config, usuario);
            }

            return ResponseEntity.ok("Configuración horaria actualizada correctamente");

        } catch (Exception e) {
            System.out.println("❌ Error actualizando configuración: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionHoraria> updateConfiguracion(
            @PathVariable Long id,
            @RequestBody ConfiguracionHoraria configuracion) {
        try {
            ConfiguracionHoraria configuracionActualizada = configuracionHorariaService.update(id, configuracion, "sistema");
            return ResponseEntity.ok(configuracionActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/inicializar")
    public ResponseEntity<String> inicializarConfiguracion() {
        configuracionHorariaService.inicializarConfiguracionPorDefecto();
        return ResponseEntity.ok("Configuración horaria inicializada correctamente");
    }
}
