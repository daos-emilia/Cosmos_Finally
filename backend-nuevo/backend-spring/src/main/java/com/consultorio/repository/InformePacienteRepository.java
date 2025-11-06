package com.consultorio.repository;

import com.consultorio.model.InformePaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformePacienteRepository extends JpaRepository<InformePaciente, Long> {

    // Buscar todos los informes de un paciente ordenados por fecha de creación (más nuevos primero)
    @Query("SELECT i FROM InformePaciente i LEFT JOIN FETCH i.paciente WHERE i.paciente.id = :pacienteId ORDER BY i.fechaCreacion DESC")
    List<InformePaciente> findByPacienteIdOrderByFechaCreacionDesc(@Param("pacienteId") Long pacienteId);

    // Contar informes de un paciente
    long countByPacienteId(Long pacienteId);

    // Buscar informes por título (búsqueda parcial)
    @Query("SELECT i FROM InformePaciente i WHERE LOWER(i.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) ORDER BY i.fechaCreacion DESC")
    List<InformePaciente> buscarPorTitulo(@Param("termino") String termino);

    // Buscar informes de un paciente por título
    @Query("SELECT i FROM InformePaciente i WHERE i.paciente.id = :pacienteId AND LOWER(i.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) ORDER BY i.fechaCreacion DESC")
    List<InformePaciente> buscarPorPacienteYTitulo(@Param("pacienteId") Long pacienteId, @Param("termino") String termino);
}

