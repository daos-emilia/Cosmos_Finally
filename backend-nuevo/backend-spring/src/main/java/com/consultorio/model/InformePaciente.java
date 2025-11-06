package com.consultorio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "informes_paciente")
public class InformePaciente extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    // Constructores
    public InformePaciente() {}

    public InformePaciente(Paciente paciente, String titulo, String contenido) {
        this.paciente = paciente;
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

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
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

    @Override
    public String toString() {
        return "InformePaciente{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getNombreCompleto() : "null") +
                ", titulo='" + titulo + '\'' +
                ", fechaCreacion=" + getFechaCreacion() +
                '}';
    }
}

