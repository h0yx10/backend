package com.events.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Campos a actualizar de un evento; solo se aplican los que se envien")
public record UpdateEventoRequest(
        String nombre,
        String tipo,
        String cliente,
        String contactoCliente,
        LocalDateTime fechaHora,
        String lugar,
        LocalDateTime plazoLimite
) {
}
