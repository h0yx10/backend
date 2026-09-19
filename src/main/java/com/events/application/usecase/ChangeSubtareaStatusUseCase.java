package com.events.application.usecase;

import com.events.application.port.in.ChangeSubtareaStatusPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.EstadoSubtarea;
import com.events.domain.entity.Subtarea;
import com.events.domain.exception.SubtareaNotFoundException;
import java.util.UUID;

public class ChangeSubtareaStatusUseCase implements ChangeSubtareaStatusPort {

    private final SubtareaRepositoryPort subtareaRepository;

    public ChangeSubtareaStatusUseCase(SubtareaRepositoryPort subtareaRepository) {
        this.subtareaRepository = subtareaRepository;
    }

    @Override
    public Subtarea execute(UUID subtareaId, EstadoSubtarea estado, String nota) {
        Subtarea subtarea = subtareaRepository.findById(subtareaId)
                .orElseThrow(() -> new SubtareaNotFoundException("No encontramos la subtarea solicitada."));

        switch (estado) {
            case DONE -> subtarea.marcarHecha();
            case POSTPONED -> subtarea.posponer(nota);
            case PENDING -> subtarea.reabrir();
        }

        return subtareaRepository.save(subtarea);
    }
}
