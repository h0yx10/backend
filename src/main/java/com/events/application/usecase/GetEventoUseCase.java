package com.events.application.usecase;

import com.events.application.port.in.GetEventoPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.entity.Evento;
import com.events.domain.exception.EventoNotFoundException;
import java.util.UUID;

public class GetEventoUseCase implements GetEventoPort {

    private final EventoRepositoryPort eventoRepository;

    public GetEventoUseCase(EventoRepositoryPort eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Evento execute(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("No encontramos el evento solicitado."));
    }
}
