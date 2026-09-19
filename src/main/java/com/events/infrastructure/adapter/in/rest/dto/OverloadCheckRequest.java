package com.events.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Previsualiza una reprogramacion antes de confirmarla (US-07)")
public record OverloadCheckRequest(
        LocalDate fechaObjetivo,
        BigDecimal horasEstimadas
) {
}
