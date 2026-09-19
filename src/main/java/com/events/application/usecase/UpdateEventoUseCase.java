package com.events.application.usecase;

import com.events.application.port.in.UpdateEventoPort;
import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.entity.Evento;
import com.events.domain.exception.EventoNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateEventoUseCase implements UpdateEventoPort {

    private final EventoRepositoryPort eventoRepository;

    public UpdateEventoUseCase(EventoRepositoryPort eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Evento execute(UUID id, String nombre, String tipo, String cliente, String contactoCliente,
                           LocalDateTime fechaHora, String lugar, LocalDateTime plazoLimite) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException("No encontramos el evento solicitado."));
        evento.actualizar(nombre, tipo, cliente, contactoCliente, fechaHora, lugar, plazoLimite);
        return eventoRepository.save(evento);
    }
}
