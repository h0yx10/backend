package com.events.application.port.out;

import com.events.domain.entity.Evento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepositoryPort {
    Evento save(Evento evento);

    Optional<Evento> findById(UUID id);

    List<Evento> findByOrganizadorId(UUID organizadorId);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
