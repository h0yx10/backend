package com.events.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.events.application.port.out.EventoRepositoryPort;
import com.events.domain.exception.EventoNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeleteEventoUseCaseTest {

    private final EventoRepositoryPort eventoRepository = mock(EventoRepositoryPort.class);
    private final DeleteEventoUseCase useCase = new DeleteEventoUseCase(eventoRepository);

    @Test
    void eliminaElEventoCuandoExiste() {
        UUID eventoId = UUID.randomUUID();
        when(eventoRepository.existsById(eventoId)).thenReturn(true);

        useCase.execute(eventoId);

        verify(eventoRepository).deleteById(eventoId);
    }

    @Test
    void fallaSiElEventoNoExiste() {
        UUID eventoId = UUID.randomUUID();
        when(eventoRepository.existsById(eventoId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(eventoId)).isInstanceOf(EventoNotFoundException.class);
    }
}
