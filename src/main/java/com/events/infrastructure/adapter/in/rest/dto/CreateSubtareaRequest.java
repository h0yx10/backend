package com.events.infrastructure.adapter.in.rest.dto;

import static com.events.infrastructure.utils.constants.MessageConstants.FECHA_OBJETIVO_REQUIRED;
import static com.events.infrastructure.utils.constants.MessageConstants.HORAS_ESTIMADAS_POSITIVE;
import static com.events.infrastructure.utils.constants.MessageConstants.HORAS_ESTIMADAS_REQUIRED;
import static com.events.infrastructure.utils.constants.MessageConstants.NOMBRE_REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para crear una subtarea logistica de un evento")
public record CreateSubtareaRequest(
        @Schema(example = "Confirmar catering")
        @NotBlank(message = NOMBRE_REQUIRED)
        String nombre,

        @Schema(example = "2026-05-01")
        @NotNull(message = FECHA_OBJETIVO_REQUIRED)
        LocalDate fechaObjetivo,

        @Schema(example = "4")
        @NotNull(message = HORAS_ESTIMADAS_REQUIRED)
        @DecimalMin(value = "0.0", inclusive = false, message = HORAS_ESTIMADAS_POSITIVE)
        BigDecimal horasEstimadas
) {
}
