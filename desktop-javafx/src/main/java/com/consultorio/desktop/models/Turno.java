package com.consultorio.desktop.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private Long id;
    private Long pacienteId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String tipo;
    private Double monto;
    private Boolean pagoConfirmado;
    private String invitadoNombre;
    private String invitadoApellido;
    private String invitadoDni;
    private String invitadoTelefono;
    private String nombrePaciente;
    private String apellidoPaciente;
    private String dniPaciente;
    private String nombreCompletoPaciente;

    // Constructor vacío
    public Turno() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public Boolean getPagoConfirmado() { return pagoConfirmado; }
    public void setPagoConfirmado(Boolean pagoConfirmado) { this.pagoConfirmado = pagoConfirmado; }

    public String getInvitadoNombre() { return invitadoNombre; }
    public void setInvitadoNombre(String invitadoNombre) { this.invitadoNombre = invitadoNombre; }

    public String getInvitadoApellido() { return invitadoApellido; }
    public void setInvitadoApellido(String invitadoApellido) { this.invitadoApellido = invitadoApellido; }

    public String getInvitadoDni() { return invitadoDni; }
    public void setInvitadoDni(String invitadoDni) { this.invitadoDni = invitadoDni; }

    public String getInvitadoTelefono() { return invitadoTelefono; }
    public void setInvitadoTelefono(String invitadoTelefono) { this.invitadoTelefono = invitadoTelefono; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public String getApellidoPaciente() { return apellidoPaciente; }
    public void setApellidoPaciente(String apellidoPaciente) { this.apellidoPaciente = apellidoPaciente; }

    public String getDniPaciente() { return dniPaciente; }
    public void setDniPaciente(String dniPaciente) { this.dniPaciente = dniPaciente; }

    public String getNombreCompletoPaciente() { return nombreCompletoPaciente; }
    public void setNombreCompletoPaciente(String nombreCompletoPaciente) { this.nombreCompletoPaciente = nombreCompletoPaciente; }

    @Override
    public String toString() {
        return "Turno{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", horaInicio=" + horaInicio +
                ", estado='" + estado + '\'' +
                ", nombreCompletoPaciente='" + nombreCompletoPaciente + '\'' +
                '}';
    }
}