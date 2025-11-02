package com.consultorio.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "configuracion_horaria")
public class ConfiguracionHoraria extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private Boolean activo = false;

    @Column(name = "turno_manana")
    private Boolean turnoManana = false;

    @Column(name = "inicio_manana")
    private LocalTime inicioManana;

    @Column(name = "fin_manana")
    private LocalTime finManana;

    @Column(name = "turno_tarde")
    private Boolean turnoTarde = false;

    @Column(name = "inicio_tarde")
    private LocalTime inicioTarde;

    @Column(name = "fin_tarde")
    private LocalTime finTarde;

    @Column(name = "duracion_sesion")
    private Integer duracionSesion = 40;

    @Column(name = "tiempo_descanso")
    private Integer tiempoDescanso = 10;

    public enum DiaSemana {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
    }

    // Constructores
    public ConfiguracionHoraria() {}

    public ConfiguracionHoraria(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Boolean getTurnoManana() { return turnoManana; }
    public void setTurnoManana(Boolean turnoManana) { this.turnoManana = turnoManana; }
    public LocalTime getInicioManana() { return inicioManana; }
    public void setInicioManana(LocalTime inicioManana) { this.inicioManana = inicioManana; }
    public LocalTime getFinManana() { return finManana; }
    public void setFinManana(LocalTime finManana) { this.finManana = finManana; }
    public Boolean getTurnoTarde() { return turnoTarde; }
    public void setTurnoTarde(Boolean turnoTarde) { this.turnoTarde = turnoTarde; }
    public LocalTime getInicioTarde() { return inicioTarde; }
    public void setInicioTarde(LocalTime inicioTarde) { this.inicioTarde = inicioTarde; }
    public LocalTime getFinTarde() { return finTarde; }
    public void setFinTarde(LocalTime finTarde) { this.finTarde = finTarde; }
    public Integer getDuracionSesion() { return duracionSesion; }
    public void setDuracionSesion(Integer duracionSesion) { this.duracionSesion = duracionSesion; }
    public Integer getTiempoDescanso() { return tiempoDescanso; }
    public void setTiempoDescanso(Integer tiempoDescanso) { this.tiempoDescanso = tiempoDescanso; }
}
