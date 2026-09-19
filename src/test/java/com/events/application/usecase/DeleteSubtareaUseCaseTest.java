package com.events.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.events.application.port.out.SubtareaRepositoryPort;
import com.events.domain.entity.Subtarea;
import com.events.domain.exception.SubtareaNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeleteSubtareaUseCaseTest {

    private final SubtareaRepositoryPort subtareaRepository = mock(SubtareaRepositoryPort.class);
    private final DeleteSubtareaUseCase useCase = new DeleteSubtareaUseCase(subtareaRepository);

    @Test
    void eliminaLaSubtareaCuandoExiste() {
        UUID subtareaId = UUID.randomUUID();
        when(subtareaRepository.findById(subtareaId))
                .thenReturn(Optional.of(new Subtarea("Reservar salon", LocalDate.now(), BigDecimal.ONE)));

        useCase.execute(subtareaId);

        verify(subtareaRepository).deleteById(subtareaId);
    }

    @Test
    void fallaSiLaSubtareaNoExiste() {
        UUID subtareaId = UUID.randomUUID();
        when(subtareaRepository.findById(subtareaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(subtareaId)).isInstanceOf(SubtareaNotFoundException.class);
    }
}
