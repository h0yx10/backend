package com.events.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(length = 180)
    private String cliente;

    @Column(name = "contacto_cliente", length = 180)
    private String contactoCliente;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 240)
    private String lugar;

    @Column(name = "plazo_limite")
    private LocalDateTime plazoLimite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtarea> subtareas = new ArrayList<>();

    protected Evento() {
    }

    public Evento(String nombre, String tipo, String cliente, String contactoCliente,
                  LocalDateTime fechaHora, String lugar, LocalDateTime plazoLimite,
                  Organizador organizador) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.cliente = cliente;
        this.contactoCliente = contactoCliente;
        this.fechaHora = fechaHora;
        this.lugar = lugar;
        this.plazoLimite = plazoLimite;
        this.organizador = organizador;
    }

    public void agregarSubtarea(Subtarea subtarea) {
        subtareas.add(subtarea);
        subtarea.asociarEvento(this);
    }

    public void actualizar(String nombre, String tipo, String cliente, String contactoCliente,
                            LocalDateTime fechaHora, String lugar, LocalDateTime plazoLimite) {
        if (nombre != null) {
            this.nombre = nombre;
        }
        if (tipo != null) {
            this.tipo = tipo;
        }
        if (cliente != null) {
            this.cliente = cliente;
        }
        if (contactoCliente != null) {
            this.contactoCliente = contactoCliente;
        }
        if (fechaHora != null) {
            this.fechaHora = fechaHora;
        }
        if (lugar != null) {
            this.lugar = lugar;
        }
        if (plazoLimite != null) {
            this.plazoLimite = plazoLimite;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCliente() {
        return cliente;
    }

    public String getContactoCliente() {
        return contactoCliente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getLugar() {
        return lugar;
    }

    public LocalDateTime getPlazoLimite() {
        return plazoLimite;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public List<Subtarea> getSubtareas() {
        return subtareas;
    }
}
