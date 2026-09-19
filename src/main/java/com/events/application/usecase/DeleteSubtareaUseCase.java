package com.events.application.usecase;

import com.events.application.port.in.DeleteSubtareaPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.exception.SubtareaNotFoundException;
import java.util.UUID;

public class DeleteSubtareaUseCase implements DeleteSubtareaPort {

    private final SubtareaRepositoryPort subtareaRepository;

    public DeleteSubtareaUseCase(SubtareaRepositoryPort subtareaRepository) {
        this.subtareaRepository = subtareaRepository;
    }

    @Override
    public void execute(UUID subtareaId) {
        subtareaRepository.findById(subtareaId)
                .orElseThrow(() -> new SubtareaNotFoundException("No encontramos la subtarea solicitada."));
        subtareaRepository.deleteById(subtareaId);
    }
}
