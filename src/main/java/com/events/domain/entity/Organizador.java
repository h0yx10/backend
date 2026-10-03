package com.events.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

/** Perfil opcional 1:1; su UUID es independiente del UUID de la cuenta. */
@Entity
@Table(name = "organizadores")
public class Organizador {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
    @Column(nullable = false)
    private boolean activo = true;
    protected Organizador() { }
    public Organizador(Usuario usuario) { this.usuario = usuario; }
    public void desactivar() { activo = false; }
    public void cambiarActivo(boolean activo) { this.activo = activo; }
    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombre() { return usuario.getNombre(); }
    public String getCorreo() { return usuario.getCorreo(); }
    public boolean isActivo() { return activo; }
}
