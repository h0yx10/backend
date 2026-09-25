package com.events.application.usecase;

import com.events.application.port.in.CreateSubtareaPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.Evento;
import com.events.domain.entity.Subtarea;
import com.events.domain.exception.EventoNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreateSubtareaUseCase implements CreateSubtareaPort {

    private final EventoRepositoryPort eventoRepository;
    private final SubtareaRepositoryPort subtareaRepository;

    public CreateSubtareaUseCase(EventoRepositoryPort eventoRepository, SubtareaRepositoryPort subtareaRepository) {
        this.eventoRepository = eventoRepository;
        this.subtareaRepository = subtareaRepository;
    }

    @Override
    public Subtarea execute(UUID eventoId, String nombre, LocalDate fechaObjetivo, BigDecimal horasEstimadas, String descripcion) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException("No encontramos el evento solicitado."));
        Subtarea subtarea = new Subtarea(nombre, fechaObjetivo, horasEstimadas, descripcion);
        subtarea.asociarEvento(evento);
        return subtareaRepository.save(subtarea);
    }
}
