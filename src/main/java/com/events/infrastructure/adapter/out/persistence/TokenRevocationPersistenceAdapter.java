package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.TokenRevocationPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Guarda solo SHA-256 del JWT. PostgreSQL hace la revocacion compartida entre instancias. */
@Repository
public class TokenRevocationPersistenceAdapter implements TokenRevocationPort {
    private final JdbcTemplate jdbc;

    public TokenRevocationPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public void revoke(String token, Instant expiresAt) {
        jdbc.update("INSERT INTO tokens_revocados (token_hash, expires_at) VALUES (?, ?) "
                        + "ON CONFLICT (token_hash) DO NOTHING",
                hash(token), OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        // JwtValidators permite 60s de desfase: conservar revocaciones durante ese margen.
        jdbc.update("DELETE FROM tokens_revocados WHERE expires_at < ?",
                OffsetDateTime.ofInstant(Instant.now().minusSeconds(60), ZoneOffset.UTC));
    }

    @Override
    public boolean isRevoked(String token) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM tokens_revocados WHERE token_hash = ?)", Boolean.class, hash(token)));
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no esta disponible.", ex);
        }
    }
}
