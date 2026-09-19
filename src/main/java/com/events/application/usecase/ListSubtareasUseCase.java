package com.events.application.usecase;

import com.events.application.port.in.ListSubtareasPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.Subtarea;
import com.events.domain.exception.EventoNotFoundException;
import java.util.List;
import java.util.UUID;

public class ListSubtareasUseCase implements ListSubtareasPort {

    private final EventoRepositoryPort eventoRepository;
    private final SubtareaRepositoryPort subtareaRepository;

    public ListSubtareasUseCase(EventoRepositoryPort eventoRepository, SubtareaRepositoryPort subtareaRepository) {
        this.eventoRepository = eventoRepository;
        this.subtareaRepository = subtareaRepository;
    }

    @Override
    public List<Subtarea> execute(UUID eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new EventoNotFoundException("No encontramos el evento solicitado.");
        }
        return subtareaRepository.findByEventoId(eventoId);
    }
}
