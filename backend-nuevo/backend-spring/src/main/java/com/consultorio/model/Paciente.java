package com.consultorio.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Arrays;

@Entity
@Table(name = "pacientes")
public class Paciente extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_educativo", nullable = false)
    private NivelEducativo nivelEducativo;

    // CAMBIAR: Usar String directamente y manejar la conversión manualmente
    @Column(name = "condicion", nullable = false)
    private String condicion;

    @Column(columnDefinition = "TEXT")
    private String antecedentes;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "foto_path")
    private String fotoPath;

    @Column(name = "contrato_aceptado")
    private Boolean contratoAceptado = false;

    // Enums
    public enum NivelEducativo {
        INICIAL, PRIMARIA, SECUNDARIA, UNIVERSIDAD
    }

    // Mantener el enum para uso interno
    public enum Condicion {
        DISLEXIA("Dislexia"),
        DISCALCULIA("Discalculia"),
        DISGRAFIA("Disgrafía"),
        TDAH("Trastorno del Déficit de Atención e Hiperactividad (TDAH)"),
        TRASTORNO_PROCESAMIENTO_AUDITIVO("Trastorno del Procesamiento Auditivo (APD)"),
        TRASTORNO_PROCESAMIENTO_VISUAL("Trastorno del Procesamiento Visual (VPD)"),
        TRASTORNO_LENGUAJE_RECEPTIVO_EXPRESIVO("Trastorno del Lenguaje Receptivo-Expresivo"),
        TEA("Trastorno del Espectro Autista (TEA)"),
        TRASTORNO_APRENDIZAJE_NO_VERBAL("Trastorno del Aprendizaje No Verbal (NLD)"),
        TRASTORNO_PROCESAMIENTO_SENSORIAL("Trastorno del Procesamiento Sensorial"),
        TRASTORNO_ANSIEDAD_ESCOLAR("Trastorno de Ansiedad Escolar"),
        OTRO("Otro"),
        NINGUNO("Ninguno");

        private final String descripcion;

        Condicion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public static Condicion fromDescripcion(String descripcion) {
            for (Condicion condicion : values()) {
                if (condicion.descripcion.equalsIgnoreCase(descripcion)) {
                    return condicion;
                }
            }
            throw new IllegalArgumentException("Condición no encontrada: " + descripcion);
        }

        public static String[] getTodasDescripciones() {
            return Arrays.stream(values())
                    .map(Condicion::getDescripcion)
                    .toArray(String[]::new);
        }
    }

    // Constructores
    public Paciente() {}

    public Paciente(Usuario usuario, LocalDate fechaNacimiento,
                    NivelEducativo nivelEducativo, Condicion condicion) {
        this.usuario = usuario;
        this.fechaNacimiento = fechaNacimiento;
        this.nivelEducativo = nivelEducativo;
        this.condicion = condicion.getDescripcion(); // Guardar la descripción
    }

    // Getters y Setters MODIFICADOS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public NivelEducativo getNivelEducativo() {
        return nivelEducativo;
    }

    public void setNivelEducativo(NivelEducativo nivelEducativo) {
        this.nivelEducativo = nivelEducativo;
    }

    // GETTER para la condición como String (para la BD)
    public String getCondicion() {
        return condicion;
    }

    // SETTER para la condición como String (desde la BD)
    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    // GETTER adicional para obtener la condición como Enum
    public Condicion getCondicionEnum() {
        if (condicion == null) {
            return null;
        }
        try {
            return Condicion.fromDescripcion(condicion);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // SETTER adicional para establecer la condición desde Enum
    public void setCondicionEnum(Condicion condicion) {
        this.condicion = condicion != null ? condicion.getDescripcion() : null;
    }

    // ... el resto de getters y setters normales
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

    public Boolean getContratoAceptado() {
        return contratoAceptado;
    }

    public void setContratoAceptado(Boolean contratoAceptado) {
        this.contratoAceptado = contratoAceptado;
    }

    // Métodos útiles
    public int getEdad() {
        return java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }

    public String getDni() {
        return usuario != null ? usuario.getDni() : "";
    }

    public String getTelefono() {
        return usuario != null ? usuario.getTelefono() : "";
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNombreCompleto() : "null") +
                ", fechaNacimiento=" + fechaNacimiento +
                ", nivelEducativo=" + nivelEducativo +
                ", condicion='" + condicion + '\'' +
                ", contratoAceptado=" + contratoAceptado +
                '}';
    }
}
