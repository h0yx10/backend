package com.events.infrastructure.security;

import com.events.application.port.out.*;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentOrganizadorAdapter implements CurrentOrganizadorPort {
    private final CurrentUsuarioPort current;
    private final OrganizadorRepositoryPort organizadores;
    public SecurityContextCurrentOrganizadorAdapter(CurrentUsuarioPort current, OrganizadorRepositoryPort organizadores) {
        this.current = current; this.organizadores = organizadores;
    }
    @Override
    public UUID currentOrganizadorId() {
        return organizadores.findByUsuarioId(current.currentUsuarioId()).filter(o -> o.isActivo())
                .orElseThrow(() -> new AccessDeniedException("Se requiere un perfil de organizador activo.")).getId();
    }
}
