package com.events.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subtareas")
public class Subtarea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;

    @Column(name = "horas_estimadas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasEstimadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSubtarea estado;

    @Column(length = 1000)
    private String nota;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "done_at")
    private LocalDateTime doneAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    protected Subtarea() {
    }

    public Subtarea(String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas) {
        this(nombre, fechaObjetivo, horasEstimadas, null);
    }

    public Subtarea(String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas, String descripcion) {
        requireHorasPositivas(horasEstimadas);
        this.nombre = nombre;
        this.fechaObjetivo = fechaObjetivo;
        this.horasEstimadas = horasEstimadas;
        this.estado = EstadoSubtarea.PENDING;
        this.descripcion = descripcion;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void asociarEvento(Evento evento) {
        this.evento = evento;
    }

    public void actualizar(String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas, String descripcion) {
        if (horasEstimadas != null) {
            requireHorasPositivas(horasEstimadas);
            this.horasEstimadas = horasEstimadas;
        }
        if (nombre != null) {
            this.nombre = nombre;
        }
        if (fechaObjetivo != null) {
            this.fechaObjetivo = fechaObjetivo;
        }
        if (descripcion != null) {
            this.descripcion = descripcion;
        }
    }

    public void reprogramar(LocalDate nuevaFecha) {
        this.fechaObjetivo = nuevaFecha;
    }

    public void marcarHecha() {
        this.estado = EstadoSubtarea.DONE;
        this.doneAt = LocalDateTime.now();
    }

    public void posponer(String nota) {
        this.estado = EstadoSubtarea.POSTPONED;
        this.nota = nota;
        this.doneAt = null;
    }

    public void reabrir() {
        this.estado = EstadoSubtarea.PENDING;
        this.doneAt = null;
    }

    private static void requireHorasPositivas(BigDecimal horas) {
        if (horas == null || horas.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Las horas estimadas deben ser mayores a 0.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaObjetivo() {
        return fechaObjetivo;
    }

    public BigDecimal getHorasEstimadas() {
        return horasEstimadas;
    }

    public EstadoSubtarea getEstado() {
        return estado;
    }

    public String getNota() {
        return nota;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getDoneAt() {
        return doneAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Evento getEvento() {
        return evento;
    }
}
