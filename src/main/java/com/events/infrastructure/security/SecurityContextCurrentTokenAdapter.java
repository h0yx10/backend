package com.events.infrastructure.security;

import com.events.application.port.out.CurrentTokenPort;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentTokenAdapter implements CurrentTokenPort {
    private Jwt currentJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt) || jwt.getExpiresAt() == null) {
            throw new AuthenticationCredentialsNotFoundException("No hay un token autenticado con expiracion.");
        }
        return jwt;
    }

    @Override
    public String tokenValue() { return currentJwt().getTokenValue(); }

    @Override
    public Instant expiresAt() { return currentJwt().getExpiresAt(); }
}
