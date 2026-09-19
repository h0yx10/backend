package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.Subtarea;
import com.events.infrastructure.adapter.out.persistence.repository.JpaSubtareaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SubtareaPersistenceAdapter implements SubtareaRepositoryPort {

    private final JpaSubtareaRepository jpaSubtareaRepository;

    public SubtareaPersistenceAdapter(JpaSubtareaRepository jpaSubtareaRepository) {
        this.jpaSubtareaRepository = jpaSubtareaRepository;
    }

    @Override
    public Subtarea save(Subtarea subtarea) {
        return jpaSubtareaRepository.save(subtarea);
    }

    @Override
    public Optional<Subtarea> findById(UUID id) {
        return jpaSubtareaRepository.findByIdWithEventoYOrganizador(id);
    }

    @Override
    public List<Subtarea> findByEventoId(UUID eventoId) {
        return jpaSubtareaRepository.findByEventoId(eventoId);
    }

    @Override
    public List<Subtarea> findByOrganizadorId(UUID organizadorId) {
        return jpaSubtareaRepository.findByOrganizadorId(organizadorId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaSubtareaRepository.deleteById(id);
    }

    @Override
    public BigDecimal sumHorasPlanificadas(UUID organizadorId, LocalDate fecha, UUID excludeSubtareaId) {
        return jpaSubtareaRepository.sumHorasPlanificadas(organizadorId, fecha, excludeSubtareaId);
    }
}
