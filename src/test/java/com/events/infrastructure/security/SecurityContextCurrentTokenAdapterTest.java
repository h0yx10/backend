package com.events.infrastructure.security;

import java.time.Instant;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.assertj.core.api.Assertions.*;

class SecurityContextCurrentTokenAdapterTest {
    private final SecurityContextCurrentTokenAdapter adapter = new SecurityContextCurrentTokenAdapter();
    @AfterEach
    void cleanContext() { SecurityContextHolder.clearContext(); }
    @Test
    void extraeTokenYExpiracionDelJwtValidado() {
        Instant expires = Instant.now().plusSeconds(600);
        var jwt = Jwt.withTokenValue("jwt").header("alg", "HS256").subject("usuario").expiresAt(expires).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        assertThat(adapter.tokenValue()).isEqualTo("jwt");
        assertThat(adapter.expiresAt()).isEqualTo(expires);
    }
    @Test
    void rechazaContextoSinAutenticacion() {
        assertThatThrownBy(adapter::tokenValue).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
    @Test
    void rechazaPrincipalQueNoEsJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("usuario", "password", java.util.List.of()));
        assertThatThrownBy(adapter::expiresAt).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
    @Test
    void rechazaJwtSinExpiracion() {
        var jwt = Jwt.withTokenValue("jwt").header("alg", "HS256").subject("usuario").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        assertThatThrownBy(adapter::tokenValue).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
