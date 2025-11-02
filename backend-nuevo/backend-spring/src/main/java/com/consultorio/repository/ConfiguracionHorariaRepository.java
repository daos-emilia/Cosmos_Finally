package com.consultorio.repository;

import com.consultorio.model.ConfiguracionHoraria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfiguracionHorariaRepository extends JpaRepository<ConfiguracionHoraria, Long> {

    List<ConfiguracionHoraria> findByActivoTrue();

    ConfiguracionHoraria findByDiaSemana(ConfiguracionHoraria.DiaSemana diaSemana);
}

