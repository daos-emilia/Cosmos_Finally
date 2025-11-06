package com.consultorio.desktop.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InformePaciente {
    private Long id;
    private Long pacienteId;
    private String titulo;
    private String contenido;
    private String fechaCreacion;

    // Constructores
    public InformePaciente() {}

    public InformePaciente(Long pacienteId, String titulo, String contenido) {
        this.pacienteId = pacienteId;
        this.titulo = titulo;
        this.contenido = contenido;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // Métodos útiles
    public String getFechaCreacionFormateada() {
        try {
            if (fechaCreacion == null || fechaCreacion.isEmpty()) {
                return "-";
            }
            // Formato: 2024-11-03T10:30:00
            LocalDateTime dateTime = LocalDateTime.parse(fechaCreacion);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return fechaCreacion;
        }
    }

    @Override
    public String toString() {
        return "InformePaciente{" +
                "id=" + id +
                ", pacienteId=" + pacienteId +
                ", titulo='" + titulo + '\'' +
                ", fechaCreacion='" + fechaCreacion + '\'' +
                '}';
    }
}

