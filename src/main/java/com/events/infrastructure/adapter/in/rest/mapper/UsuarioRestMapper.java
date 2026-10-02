package com.events.infrastructure.adapter.in.rest.mapper;

import com.events.application.port.in.AuthResult;
import com.events.domain.entity.Usuario;
import com.events.infrastructure.adapter.in.rest.dto.AuthResponse;
import com.events.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRestMapper {

    private static final String TOKEN_TYPE = "Bearer";

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRoles().stream().map(rol -> rol.getNombre().name()).sorted().toList(),
                usuario.isActivo(),
                usuario.getCreatedAt()
        );
    }

    public AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.accessToken(), TOKEN_TYPE, result.expiresInSeconds(), toResponse(result.usuario()));
    }
}
