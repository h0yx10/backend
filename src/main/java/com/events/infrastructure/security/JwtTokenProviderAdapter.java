package com.events.infrastructure.security;

import com.events.application.port.out.TokenProviderPort;
import com.events.domain.entity.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Emite JWT firmados con HS256. sub = id del usuario; claims extra: correo, nombre y roles.
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration expiration;

    public JwtTokenProviderAdapter(JwtEncoder jwtEncoder,
                                   @Value("${app.security.jwt.issuer}") String issuer,
                                   @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public String generate(Usuario usuario) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .subject(usuario.getId().toString())
                .claim("correo", usuario.getCorreo())
                .claim("nombre", usuario.getNombre())
                .claim(SecurityConfig.ROLES_CLAIM, usuario.getRoles().stream()
                        .map(rol -> rol.getNombre().name())
                        .sorted()
                        .toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long expirationSeconds() {
        return expiration.toSeconds();
    }
}
