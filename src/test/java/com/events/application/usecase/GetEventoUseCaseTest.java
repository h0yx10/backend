package com.events.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.entity.Evento;
import com.events.domain.entity.Organizador;
import com.events.domain.exception.EventoNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetEventoUseCaseTest {

    private final EventoRepositoryPort eventoRepository = mock(EventoRepositoryPort.class);
    private final GetEventoUseCase useCase = new GetEventoUseCase(eventoRepository);

    @Test
    void devuelveElEventoCuandoExiste() {
        UUID eventoId = UUID.randomUUID();
        Evento evento = new Evento("Boda", "Social", null, null, null, null, null, new Organizador("Demo", "demo@x.com"));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(evento));

        assertThat(useCase.execute(eventoId)).isSameAs(evento);
    }

    @Test
    void fallaSiElEventoNoExiste() {
        UUID eventoId = UUID.randomUUID();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(eventoId)).isInstanceOf(EventoNotFoundException.class);
    }
}
