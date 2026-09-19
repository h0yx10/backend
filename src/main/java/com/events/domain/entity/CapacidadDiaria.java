package com.events.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Guarda el limite diario de horas configurado por un organizador (US-12).
 * El backlog define un unico limite por organizador (no por fecha especifica), pero la tabla
 * conserva la fecha de vigencia para permitir un historial; el limite vigente es siempre el de
 * la fila mas reciente para ese organizador.
 */
@Entity
@Table(name = "capacidades_diarias", uniqueConstraints = @UniqueConstraint(columnNames = {"organizador_id", "fecha"}))
public class CapacidadDiaria {

    public static final BigDecimal LIMITE_MINIMO = BigDecimal.ONE;
    public static final BigDecimal LIMITE_MAXIMO = BigDecimal.valueOf(16);
    public static final BigDecimal LIMITE_POR_DEFECTO = BigDecimal.valueOf(6);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "limite_horas", nullable = false, precision = 8, scale = 2)
    private BigDecimal limiteHoras;

    protected CapacidadDiaria() {
    }

    public CapacidadDiaria(Organizador organizador, LocalDate fecha, BigDecimal limiteHoras) {
        requireRangoValido(limiteHoras);
        this.organizador = organizador;
        this.fecha = fecha;
        this.limiteHoras = limiteHoras;
    }

    public void actualizarLimite(BigDecimal limiteHoras, LocalDate fecha) {
        requireRangoValido(limiteHoras);
        this.limiteHoras = limiteHoras;
        this.fecha = fecha;
    }

    public static void requireRangoValido(BigDecimal limiteHoras) {
        if (limiteHoras == null
                || limiteHoras.compareTo(LIMITE_MINIMO) < 0
                || limiteHoras.compareTo(LIMITE_MAXIMO) > 0) {
            throw new IllegalArgumentException("El limite diario debe estar entre 1 y 16 horas.");
        }
    }

    public UUID getId() {
        return id;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getLimiteHoras() {
        return limiteHoras;
    }
}
