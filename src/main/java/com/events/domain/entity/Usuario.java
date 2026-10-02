package com.events.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Cuenta del sistema: es la entidad que registra credenciales e inicia sesion (nombre, correo
 * y hash de la contrasena). Un usuario con perfil de organizador tiene ademas una fila en
 * organizadores que comparte su id.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 180)
    private String correo;

    // Nullable a nivel JPA para poder migrar cuentas antiguas sin password (no pueden iniciar sesion).
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Lado inverso de organizadores.usuario_id; null si el usuario aun no tiene perfil de organizador.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Organizador organizador;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String passwordHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Crea el perfil de organizador (activo) de este usuario; se guarda junto con el usuario, que
     * siempre se inserta primero. Si ya lo tenia, devuelve el existente.
     */
    public Organizador habilitarComoOrganizador() {
        if (organizador == null) {
            organizador = new Organizador(this);
        }
        return organizador;
    }

    /** Un usuario sin perfil de organizador no esta desactivado: cuenta como activo. */
    public boolean isActivo() {
        return organizador == null || organizador.isActivo();
    }

    public boolean puedeIniciarSesion() {
        return isActivo() && passwordHash != null;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Los roles viven en el organizador (organizador_roles); sin perfil de organizador no hay roles. */
    public Set<Rol> getRoles() {
        return organizador == null ? Collections.emptySet() : organizador.getRoles();
    }

    public Organizador getOrganizador() {
        return organizador;
    }
}
