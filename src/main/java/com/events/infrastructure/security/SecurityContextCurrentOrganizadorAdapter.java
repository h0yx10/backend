package com.events.infrastructure.security;

import com.events.application.port.out.CurrentOrganizadorPort;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Reemplaza al antiguo organizador demo: el organizador actual es el "sub" del JWT que
 * Spring Security ya valido y dejo en el SecurityContext.
 */
@Component
public class SecurityContextCurrentOrganizadorAdapter implements CurrentOrganizadorPort {

    @Override
    public UUID currentOrganizadorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado.");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
