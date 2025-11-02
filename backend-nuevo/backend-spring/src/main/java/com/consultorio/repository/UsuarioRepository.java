package com.consultorio.repository;

import com.consultorio.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por DNI
    Optional<Usuario> findByDni(String dni);

    // Buscar usuario por DNI y contraseña (para login)
    Optional<Usuario> findByDniAndPassword(String dni, String password);

    // Buscar usuarios por tipo
    List<Usuario> findByTipo(Usuario.TipoUsuario tipo);

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar usuarios inactivos
    List<Usuario> findByActivoFalse();

    // Buscar usuarios por tipo y estado activo
    List<Usuario> findByTipoAndActivoTrue(Usuario.TipoUsuario tipo);

    // Buscar psicopedagogas por matrícula
    Optional<Usuario> findByMatricula(String matricula);

    // Verificar si existe un usuario con un DNI
    boolean existsByDni(String dni);

    // Verificar si existe un usuario con un email
    boolean existsByEmail(String email);

    // Buscar usuarios por nombre o apellido (búsqueda parcial)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Usuario> buscarPorNombreOApellido(@Param("termino") String termino);

    // Contar usuarios por tipo
    long countByTipo(Usuario.TipoUsuario tipo);

    // Buscar usuarios por DNI ignorando mayúsculas/minúsculas
    Optional<Usuario> findByDniIgnoreCase(String dni);

    // Buscar usuarios con email no validado
    List<Usuario> findByEmailValidadoFalse();

    // Buscar usuarios por estado de validación de email
    List<Usuario> findByEmailValidado(Boolean emailValidado);

    // Contar usuarios con email validado/no validado
    long countByEmailValidado(Boolean emailValidado);

    // Buscar usuario por token de validación
    Optional<Usuario> findByTokenValidacion(String tokenValidacion);

}