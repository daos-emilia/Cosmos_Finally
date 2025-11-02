package com.consultorio.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos")
public class Turno extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ✅ SOLUCIÓN: Usar solo el ID del paciente
    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTurno tipo;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "pago_confirmado")
    private Boolean pagoConfirmado = false;

    // Campos para turnos de invitados
    @Column(name = "invitado_nombre")
    private String invitadoNombre;

    @Column(name = "invitado_apellido")
    private String invitadoApellido;

    @Column(name = "invitado_dni")
    private String invitadoDni;

    @Column(name = "invitado_telefono")
    private String invitadoTelefono;

    @Column(name = "id_pago_mercado_pago")
    private String idPagoMercadoPago;

    // ✅ CAMPOS TRANSIENT para mostrar datos en frontend
    @Transient
    private String nombrePaciente;

    @Transient
    private String apellidoPaciente;

    @Transient
    private String dniPaciente;

    // Enums
    public enum EstadoTurno {
        CONFIRMADO, ASISTIDO, CANCELADO, NO_ASISTIO
    }

    public enum TipoTurno {
        NORMAL, INVITADO
    }

    // Constructores
    public Turno() {}

    // ✅ CONSTRUCTOR CORREGIDO
    public Turno(Long pacienteId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                 EstadoTurno estado, TipoTurno tipo, Double monto) {
        this.pacienteId = pacienteId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.tipo = tipo;
        this.monto = monto;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ✅ GETTER/SETTER para pacienteId
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    public TipoTurno getTipo() { return tipo; }
    public void setTipo(TipoTurno tipo) { this.tipo = tipo; }

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

    public String getIdPagoMercadoPago() { return idPagoMercadoPago; }
    public void setIdPagoMercadoPago(String idPagoMercadoPago) { this.idPagoMercadoPago = idPagoMercadoPago; }

    // ✅ GETTERS/SETTERS para campos transient
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public String getApellidoPaciente() { return apellidoPaciente; }
    public void setApellidoPaciente(String apellidoPaciente) { this.apellidoPaciente = apellidoPaciente; }

    public String getDniPaciente() { return dniPaciente; }
    public void setDniPaciente(String dniPaciente) { this.dniPaciente = dniPaciente; }

    // ✅ MÉTODO MEJORADO para nombre del paciente
    public String getNombreCompletoPaciente() {
        if (tipo == TipoTurno.INVITADO) {
            return (invitadoNombre != null ? invitadoNombre : "") + " " +
                    (invitadoApellido != null ? invitadoApellido : "") + " (Invitado)";
        }
        if (nombrePaciente != null && apellidoPaciente != null) {
            return nombrePaciente + " " + apellidoPaciente;
        }
        return "Paciente ID: " + pacienteId;
    }

    // ✅ MÉTODO para obtener DNI
    public String getDniCompleto() {
        if (tipo == TipoTurno.INVITADO) {
            return invitadoDni != null ? invitadoDni : "";
        }
        return dniPaciente != null ? dniPaciente : "";
    }
}
