package com.events.infrastructure.adapter.in.rest.dto;

import static com.events.infrastructure.utils.constants.MessageConstants.ESTADO_REQUIRED;

import com.events.domain.entity.EstadoSubtarea;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de ejecucion de una subtarea (US-09)")
public record ChangeSubtareaStatusRequest(
        @Schema(example = "DONE")
        @NotNull(message = ESTADO_REQUIRED)
        EstadoSubtarea estado,

        @Schema(example = "Esperando confirmacion de salon", description = "Nota opcional, usada sobre todo al posponer")
        String nota
) {
}
