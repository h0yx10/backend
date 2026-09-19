package com.events.application.usecase;

import com.events.application.port.in.DeleteEventoPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.exception.EventoNotFoundException;
import java.util.UUID;

public class DeleteEventoUseCase implements DeleteEventoPort {

    private final EventoRepositoryPort eventoRepository;

    public DeleteEventoUseCase(EventoRepositoryPort eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public void execute(UUID id) {
        if (!eventoRepository.existsById(id)) {
            throw new EventoNotFoundException("No encontramos el evento solicitado.");
        }
        eventoRepository.deleteById(id);
    }
}
