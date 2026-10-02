package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.entity.Evento;
import com.events.infrastructure.adapter.out.persistence.repository.JpaEventoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class EventoPersistenceAdapter implements EventoRepositoryPort {

    private final JpaEventoRepository jpaEventoRepository;

    public EventoPersistenceAdapter(JpaEventoRepository jpaEventoRepository) {
        this.jpaEventoRepository = jpaEventoRepository;
    }

    @Override
    public Evento save(Evento evento) {
        return jpaEventoRepository.save(evento);
    }

    @Override
    public Optional<Evento> findByIdAndOrganizadorId(UUID id, UUID organizadorId) {
        return jpaEventoRepository.findByIdAndOrganizadorIdWithSubtareas(id, organizadorId);
    }

    @Override
    public List<Evento> findByOrganizadorId(UUID organizadorId) {
        return jpaEventoRepository.findByOrganizadorIdWithSubtareas(organizadorId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaEventoRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndOrganizadorId(UUID id, UUID organizadorId) {
        return jpaEventoRepository.existsByIdAndOrganizadorId(id, organizadorId);
    }
}
