package com.events.application.usecase;

import com.events.application.port.in.GetCurrentUserPort;
import com.events.application.port.out.CurrentOrganizadorPort;
import com.events.application.port.out.UsuarioRepositoryPort;
import com.events.domain.entity.Usuario;
import com.events.domain.exception.OrganizadorNotFoundException;

public class GetCurrentUserUseCase implements GetCurrentUserPort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final CurrentOrganizadorPort currentOrganizador;

    public GetCurrentUserUseCase(UsuarioRepositoryPort usuarioRepository,
                                 CurrentOrganizadorPort currentOrganizador) {
        this.usuarioRepository = usuarioRepository;
        this.currentOrganizador = currentOrganizador;
    }

    @Override
    public Usuario execute() {
        return usuarioRepository.findById(currentOrganizador.currentOrganizadorId())
                .orElseThrow(() -> new OrganizadorNotFoundException("No encontramos el usuario autenticado."));
    }
}
