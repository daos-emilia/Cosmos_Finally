package com.consultorio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String email;

    @Column(name = "email_validado")
    private Boolean emailValidado = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    private String matricula; // Solo para psicopedagogas

    @Column(nullable = false)
    private Boolean activo = true;

    // Enums
    public enum TipoUsuario {
        ADMIN, PSICOPEDAGOGA, SECRETARIA, PACIENTE
    }

    @Column(name = "token_validacion", length = 100)
    private String tokenValidacion;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiracion;

    // Constructores
    public Usuario() {}

    public Usuario(String dni, String password, String nombre, String apellido,
                   String telefono, String email, TipoUsuario tipo, String matricula) {
        this.dni = dni;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.tipo = tipo;
        this.matricula = matricula;
    }

    // Getters y Setters COMPLETOS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailValidado() {
        return emailValidado;
    }

    public void setEmailValidado(Boolean emailValidado) {
        this.emailValidado = emailValidado;
    }


    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getTokenValidacion() {
        return tokenValidacion;
    }

    public void setTokenValidacion(String tokenValidacion) {
        this.tokenValidacion = tokenValidacion;
    }

    public LocalDateTime getTokenExpiracion() {
        return tokenExpiracion;
    }

    public void setTokenExpiracion(LocalDateTime tokenExpiracion) {
        this.tokenExpiracion = tokenExpiracion;
    }

    // ====== MÉTODOS PARA TOKENS ======
    public void generarTokenValidacion() {
        this.tokenValidacion = java.util.UUID.randomUUID().toString();
        this.tokenExpiracion = LocalDateTime.now().plusHours(24); // 24 horas
    }

    public boolean isTokenValido(String token) {
        return this.tokenValidacion != null &&
                this.tokenValidacion.equals(token) &&
                LocalDateTime.now().isBefore(this.tokenExpiracion);
    }

    public void limpiarToken() {
        this.tokenValidacion = null;
        this.tokenExpiracion = null;
    }


    // Métodos útiles
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean isAdmin() {
        return tipo == TipoUsuario.ADMIN;
    }

    public boolean isPsicopedagoga() {
        return tipo == TipoUsuario.PSICOPEDAGOGA;
    }

    public boolean isSecretaria() {
        return tipo == TipoUsuario.SECRETARIA;
    }

    public boolean isPaciente() {
        return tipo == TipoUsuario.PACIENTE;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", tipo=" + tipo +
                ", activo=" + activo +
                '}';
    }
}
