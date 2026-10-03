package com.events.infrastructure.security;

import com.events.application.port.out.CurrentUsuarioPort;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUsuarioAdapter implements CurrentUsuarioPort {
    @Override
    public UUID currentUsuarioId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt))
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado.");
        try { return UUID.fromString(jwt.getSubject()); }
        catch (IllegalArgumentException ex) { throw new AuthenticationCredentialsNotFoundException("Usuario invalido.", ex); }
    }
}
