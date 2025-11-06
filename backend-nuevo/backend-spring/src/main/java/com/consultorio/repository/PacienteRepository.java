package com.consultorio.repository;

import com.consultorio.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // Buscar paciente por ID de usuario
    Optional<Paciente> findByUsuarioId(Long usuarioId);

    // Buscar paciente por DNI del usuario
    @Query("SELECT p FROM Paciente p WHERE p.usuario.dni = :dni")
    Optional<Paciente> findByUsuarioDni(@Param("dni") String dni);

    // Buscar pacientes activos (usuarios activos)
    @Query("SELECT p FROM Paciente p WHERE p.usuario.activo = true ORDER BY p.fechaCreacion DESC")
    List<Paciente> findPacientesActivos();

    // Buscar pacientes por nombre o apellido
    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.usuario.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.usuario.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "ORDER BY p.fechaCreacion DESC")
    List<Paciente> buscarPorNombreOApellido(@Param("termino") String termino);

    // Obtener todos los pacientes ordenados por fecha de creación (más nuevos primero)
    @Query("SELECT p FROM Paciente p ORDER BY p.fechaCreacion DESC")
    List<Paciente> findAllOrderByFechaCreacionDesc();

    // Buscar pacientes por nivel educativo
    List<Paciente> findByNivelEducativo(Paciente.NivelEducativo nivelEducativo);

    // Buscar pacientes por condición
    List<Paciente> findByCondicion(String condicion);

    // Contar pacientes activos
    @Query("SELECT COUNT(p) FROM Paciente p WHERE p.usuario.activo = true")
    long countPacientesActivos();

    // Verificar si existe paciente por usuario_id
    boolean existsByUsuarioId(Long usuarioId);
}

