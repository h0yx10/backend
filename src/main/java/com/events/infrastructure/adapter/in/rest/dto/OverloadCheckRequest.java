package com.events.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Previsualiza una reprogramacion antes de confirmarla (US-07)")
public record OverloadCheckRequest(
        @JsonAlias("fechaObjetivo")
        LocalDate targetDate,
        @JsonAlias("horasEstimadas")
        BigDecimal estimatedHours
) {
}
