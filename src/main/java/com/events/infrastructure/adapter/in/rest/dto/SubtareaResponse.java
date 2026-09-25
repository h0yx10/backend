package com.events.infrastructure.adapter.in.rest.dto;

import com.events.domain.entity.EstadoSubtarea;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubtareaResponse(
        UUID id,
        UUID eventoId,
        String nombre,
        LocalDate fechaObjetivo,
        BigDecimal horasEstimadas,
        EstadoSubtarea estado,
        String nota,
        LocalDateTime doneAt,
        LocalDateTime createdAt,
        String descripcion
) {
}
