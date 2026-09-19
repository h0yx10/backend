package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.CapacidadDiariaRepositoryPort;
import com.events.domain.entity.CapacidadDiaria;
import com.events.infrastructure.adapter.out.persistence.repository.JpaCapacidadDiariaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CapacidadDiariaPersistenceAdapter implements CapacidadDiariaRepositoryPort {

    private final JpaCapacidadDiariaRepository jpaCapacidadDiariaRepository;

    public CapacidadDiariaPersistenceAdapter(JpaCapacidadDiariaRepository jpaCapacidadDiariaRepository) {
        this.jpaCapacidadDiariaRepository = jpaCapacidadDiariaRepository;
    }

    @Override
    public CapacidadDiaria save(CapacidadDiaria capacidadDiaria) {
        return jpaCapacidadDiariaRepository.save(capacidadDiaria);
    }

    @Override
    public Optional<CapacidadDiaria> findCurrentByOrganizadorId(UUID organizadorId) {
        return jpaCapacidadDiariaRepository.findCurrentByOrganizadorId(organizadorId);
    }
}
