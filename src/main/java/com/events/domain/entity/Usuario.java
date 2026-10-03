package com.events.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/** Cuenta de login. Los roles pertenecen al usuario, independientemente de su perfil. */
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, unique = true, length = 180)
    private String correo;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Organizador organizador;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() { }
    public Usuario(String nombre, String correo, String passwordHash) {
        actualizarDatos(nombre, correo);
        this.passwordHash = passwordHash;
    }
    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
    public Organizador habilitarComoOrganizador() {
        if (organizador == null) organizador = new Organizador(this);
        return organizador;
    }
    public void actualizarDatos(String nombre, String correo) {
        if (nombre != null) {
            String valor = nombre.trim();
            if (valor.isEmpty() || valor.length() > 120)
                throw new IllegalArgumentException("El nombre es obligatorio y admite hasta 120 caracteres.");
            this.nombre = valor;
        }
        if (correo != null) this.correo = normalizarCorreo(correo);
    }
    public static String normalizarCorreo(String correo) {
        String valor = correo.trim().toLowerCase(Locale.ROOT);
        if (valor.isEmpty() || valor.length() > 180)
            throw new IllegalArgumentException("El correo es obligatorio y admite hasta 180 caracteres.");
        return valor;
    }
    public void cambiarPasswordHash(String hash) { this.passwordHash = Objects.requireNonNull(hash); }
    public void asignarRol(Rol rol) { roles.add(Objects.requireNonNull(rol)); }
    public void reemplazarRoles(Set<Rol> nuevos) { roles.clear(); roles.addAll(nuevos); }
    public boolean tieneRol(NombreRol nombre) { return roles.stream().anyMatch(r -> r.getNombre() == nombre); }
    public boolean isActivo() { return organizador == null || organizador.isActivo(); }
    public boolean puedeIniciarSesion() { return isActivo() && passwordHash != null; }
    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Rol> getRoles() { return Collections.unmodifiableSet(roles); }
    public Organizador getOrganizador() { return organizador; }
}
