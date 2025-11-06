package com.consultorio.desktop.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Usuario {
    
    private Long id;
    private String dni;
    private String password;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private Boolean emailValidado;
    private String tipo; // ADMIN, PSICOPEDAGOGA, SECRETARIA, PACIENTE
    private String matricula;
    private Boolean activo;
    
    // Campos de auditoría
    private String fechaCreacion;
    private String fechaActualizacion;
    private String creadoPor;
    private String actualizadoPor;
    
    // Constructor vacío
    public Usuario() {}
    
    // Constructor completo
    public Usuario(Long id, String dni, String password, String nombre, String apellido,
                   String telefono, String email, Boolean emailValidado, String tipo,
                   String matricula, Boolean activo, String fechaCreacion, String fechaActualizacion,
                   String creadoPor, String actualizadoPor) {
        this.id = id;
        this.dni = dni;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.emailValidado = emailValidado;
        this.tipo = tipo;
        this.matricula = matricula;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.creadoPor = creadoPor;
        this.actualizadoPor = actualizadoPor;
    }
    
    // Getters y Setters
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
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
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
    
    public String getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public String getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public String getActualizadoPor() {
        return actualizadoPor;
    }
    
    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }
    
    // Métodos útiles
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
    
    public String getEmailValidadoTexto() {
        return emailValidado ? "Sí" : "No";
    }
    
    public String getTipoFormateado() {
        if (tipo == null) return "";
        
        switch (tipo.toUpperCase()) {
            case "ADMIN":
                return "Administrador";
            case "PSICOPEDAGOGA":
                return "Psicopedagoga";
            case "SECRETARIA":
                return "Secretaria";
            case "PACIENTE":
                return "Paciente";
            default:
                return tipo;
        }
    }
    
    public String getFechaCreacionFormateada() {
        if (fechaCreacion == null || fechaCreacion.isEmpty()) {
            return "N/A";
        }
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(fechaCreacion, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            return fechaCreacion;
        }
    }
    
    public String getFechaActualizacionFormateada() {
        if (fechaActualizacion == null || fechaActualizacion.isEmpty()) {
            return "N/A";
        }
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(fechaActualizacion, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            return fechaActualizacion;
        }
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

