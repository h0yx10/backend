package com.events.infrastructure.adapter.in.rest.dto;

import static com.events.infrastructure.utils.constants.MessageConstants.HORAS_ESTIMADAS_POSITIVE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Campos a actualizar de una subtarea; solo se aplican los que se envien. "
        + "Enviar fechaObjetivo y/o horasEstimadas reprograma la subtarea (US-06) y valida sobrecarga (US-07).")
public record UpdateSubtareaRequest(
        String nombre,
        LocalDate fechaObjetivo,
        @DecimalMin(value = "0.0", inclusive = false, message = HORAS_ESTIMADAS_POSITIVE)
        BigDecimal horasEstimadas
) {
}
