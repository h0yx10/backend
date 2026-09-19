package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.TODAY_RETRIEVED;

import com.events.application.port.in.GetTodayPort;
import com.events.application.port.in.TodayGroups;
import com.events.domain.entity.EstadoSubtarea;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.TodayResponse;
import com.events.infrastructure.adapter.in.rest.mapper.SubtareaRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Hoy", description = "Vista de gestiones urgentes del dia, con filtros (US-04, US-05)")
public class TodayController {

    private final GetTodayPort getTodayUseCase;
    private final SubtareaRestMapper mapper;

    @GetMapping("/api/today")
    @Operation(
            summary = "Vista Hoy",
            description = "Subtareas no DONE agrupadas en Vencidas/Para hoy/Proximas, con filtros opcionales por evento y estado."
    )
    public ApiResponse<TodayResponse> today(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) EstadoSubtarea status
    ) {
        TodayGroups groups = getTodayUseCase.execute(eventId, status);
        TodayResponse response = new TodayResponse(
                groups.vencidas().stream().map(mapper::toResponse).toList(),
                groups.paraHoy().stream().map(mapper::toResponse).toList(),
                groups.proximas().stream().map(mapper::toResponse).toList(),
                TodayGroups.REGLA
        );
        return ApiResponse.ok(TODAY_RETRIEVED, response);
    }
}
