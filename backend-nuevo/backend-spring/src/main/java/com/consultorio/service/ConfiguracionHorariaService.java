package com.consultorio.service;

import com.consultorio.model.ConfiguracionHoraria;
import com.consultorio.repository.ConfiguracionHorariaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConfiguracionHorariaService {

    @Autowired
    private ConfiguracionHorariaRepository configuracionHorariaRepository;

    @Autowired
    private EntityManager entityManager;

    public List<ConfiguracionHoraria> findAll() {
        return configuracionHorariaRepository.findAll();
    }

    public Optional<ConfiguracionHoraria> findById(Long id) {
        return configuracionHorariaRepository.findById(id);
    }

    public List<ConfiguracionHoraria> getConfiguracionesActivas() {
        return configuracionHorariaRepository.findByActivoTrue();
    }

    public ConfiguracionHoraria save(ConfiguracionHoraria configuracion) {
        return configuracionHorariaRepository.save(configuracion);
    }

    public ConfiguracionHoraria update(Long id, ConfiguracionHoraria configuracionActualizada, String usuario) {
        System.out.println("🔄 Actualizando configuración ID: " + id);
        System.out.println("   Día: " + configuracionActualizada.getDiaSemana());
        System.out.println("   Activo: " + configuracionActualizada.getActivo());

        return configuracionHorariaRepository.findById(id)
                .map(config -> {
                    config.setActivo(configuracionActualizada.getActivo());
                    config.setTurnoManana(configuracionActualizada.getTurnoManana());
                    config.setInicioManana(configuracionActualizada.getInicioManana());
                    config.setFinManana(configuracionActualizada.getFinManana());
                    config.setTurnoTarde(configuracionActualizada.getTurnoTarde());
                    config.setInicioTarde(configuracionActualizada.getInicioTarde());
                    config.setFinTarde(configuracionActualizada.getFinTarde());
                    config.setDuracionSesion(configuracionActualizada.getDuracionSesion());
                    config.setTiempoDescanso(configuracionActualizada.getTiempoDescanso());
                    config.setActualizadoPor(usuario);
                    config.setFechaActualizacion(LocalDateTime.now());

                    ConfiguracionHoraria guardada = configuracionHorariaRepository.save(config);
                    System.out.println("   ✅ Configuración actualizada: " + guardada.getDiaSemana());
                    return guardada;
                })
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con id: " + id));
    }

    public void inicializarConfiguracionPorDefecto() {
        if (configuracionHorariaRepository.count() == 0) {
            ConfiguracionHoraria.DiaSemana[] dias = ConfiguracionHoraria.DiaSemana.values();

            for (ConfiguracionHoraria.DiaSemana dia : dias) {
                ConfiguracionHoraria config = new ConfiguracionHoraria(dia);
                config.setActivo(dia != ConfiguracionHoraria.DiaSemana.SABADO &&
                        dia != ConfiguracionHoraria.DiaSemana.DOMINGO);
                config.setTurnoManana(true);
                config.setTurnoTarde(true);
                configuracionHorariaRepository.save(config);
            }
        }
    }
}
