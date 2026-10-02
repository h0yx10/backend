package com.events.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil de organizador de eventos. Solo guarda la referencia a su usuario (usuario_id, que es
 * a la vez PK y FK hacia usuarios) y si esta activo; nombre, correo y credenciales viven en
 * {@link Usuario}. Cada organizador es dueno de sus eventos, subtareas (via evento) y capacidad
 * diaria.
 */
@Entity
@Table(name = "organizadores")
public class Organizador {

    @Id
    @Column(name = "usuario_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // El DEFAULT permite que ddl-auto=update agregue la columna aunque ya existan filas.
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean activo = true;

    // organizador_roles.usuario_id -> organizadores.usuario_id
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "organizador_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    protected Organizador() {
    }

    public Organizador(Usuario usuario) {
        this.usuario = usuario;
    }

    public void asignarRol(Rol rol) {
        roles.add(rol);
    }

    public void desactivar() {
        this.activo = false;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    /** Nombre y correo se leen del usuario (join con usuarios). */
    public String getNombre() {
        return usuario.getNombre();
    }

    public String getCorreo() {
        return usuario.getCorreo();
    }

    public Set<Rol> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean isActivo() {
        return activo;
    }
}
