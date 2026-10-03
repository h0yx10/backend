package com.events.infrastructure.security;

import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.domain.entity.NombreRol;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** La firma/expiracion se validan antes; las autoridades vienen de la BD en cada peticion. */
@Component
public class DatabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UsuarioRepositoryPort usuarios;
    public DatabaseJwtAuthenticationConverter(UsuarioRepositoryPort usuarios) { this.usuarios = usuarios; }
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID id;
        try { id = UUID.fromString(jwt.getSubject()); }
        catch (IllegalArgumentException | NullPointerException ex) { throw new InvalidBearerTokenException("Usuario invalido."); }
        var usuario = usuarios.findById(id).filter(u -> u.puedeIniciarSesion())
                .orElseThrow(() -> new InvalidBearerTokenException("La cuenta ya no esta disponible."));
        var authorities = usuario.getRoles().stream()
                .filter(r -> r.getNombre() != NombreRol.ORGANIZADOR || usuario.getOrganizador() != null)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre().name())).toList();
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
