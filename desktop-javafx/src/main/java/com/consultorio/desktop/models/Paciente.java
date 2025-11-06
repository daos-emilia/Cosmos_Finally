package com.consultorio.desktop.models;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Paciente {
    private Long id;
    private Long usuarioId;
    
    // Datos del usuario
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private boolean activo;
    
    // Datos del paciente
    private String fechaNacimiento;
    private String nivelEducativo;
    private String condicion;
    private String antecedentes;
    private String observaciones;
    private String fotoPath;
    private String fechaCreacion;

    // Constructores
    public Paciente() {}

    public Paciente(String nombre, String apellido, String dni, String telefono, String email,
                    String fechaNacimiento, String nivelEducativo, String condicion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.nivelEducativo = nivelEducativo;
        this.condicion = condicion;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNivelEducativo() {
        return nivelEducativo;
    }

    public void setNivelEducativo(String nivelEducativo) {
        this.nivelEducativo = nivelEducativo;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // Métodos útiles
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        try {
            if (fechaNacimiento == null || fechaNacimiento.isEmpty()) {
                return 0;
            }
            LocalDate nacimiento = LocalDate.parse(fechaNacimiento);
            return Period.between(nacimiento, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getEstado() {
        return activo ? "Activo" : "Inactivo";
    }

    public String getFechaCreacionFormateada() {
        try {
            if (fechaCreacion == null || fechaCreacion.isEmpty()) {
                return "-";
            }
            // Formato: 2024-11-03T10:30:00
            LocalDate fecha = LocalDate.parse(fechaCreacion.substring(0, 10));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return fecha.format(formatter);
        } catch (Exception e) {
            return fechaCreacion;
        }
    }

    public String getFechaNacimientoFormateada() {
        try {
            if (fechaNacimiento == null || fechaNacimiento.isEmpty()) {
                return "-";
            }
            LocalDate fecha = LocalDate.parse(fechaNacimiento);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return fecha.format(formatter);
        } catch (Exception e) {
            return fechaNacimiento;
        }
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", activo=" + activo +
                '}';
    }
}

