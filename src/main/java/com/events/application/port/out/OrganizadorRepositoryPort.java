package com.events.application.port.out;

import com.events.domain.entity.Organizador;
import java.util.Optional;
import java.util.UUID;

public interface OrganizadorRepositoryPort {
    Organizador save(Organizador organizador);

    Optional<Organizador> findById(UUID id);

    Optional<Organizador> findByCorreo(String correo);
}
