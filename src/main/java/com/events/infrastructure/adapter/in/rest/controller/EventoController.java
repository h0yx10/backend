package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_CREATED;
import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_DELETED;
import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_LIST_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_PROGRESS_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.EVENTO_UPDATED;

import com.events.application.port.in.CreateEventoPort;
import com.events.application.port.in.DeleteEventoPort;
import com.events.application.port.in.GetEventoPort;
import com.events.application.port.in.GetEventoProgressPort;
import com.events.application.port.in.ListEventosPort;
import com.events.application.port.in.NuevaSubtareaData;
import com.events.application.port.in.UpdateEventoPort;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.CreateEventoRequest;
import com.events.infrastructure.adapter.in.rest.dto.EventoResponse;
import com.events.infrastructure.adapter.in.rest.dto.ProgressResponse;
import com.events.infrastructure.adapter.in.rest.dto.UpdateEventoRequest;
import com.events.infrastructure.adapter.in.rest.mapper.EventoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestion de eventos y su plan de trabajo logistico (US-01, US-03)")
public class EventoController {

    private final CreateEventoPort createEventoUseCase;
    private final GetEventoPort getEventoUseCase;
    private final ListEventosPort listEventosUseCase;
    private final UpdateEventoPort updateEventoUseCase;
    private final DeleteEventoPort deleteEventoUseCase;
    private final GetEventoProgressPort getEventoProgressUseCase;
    private final EventoRestMapper mapper;

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Lista los eventos del organizador demo.")
    public ApiResponse<List<EventoResponse>> list() {
        List<EventoResponse> eventos = listEventosUseCase.execute().stream().map(mapper::toResponse).toList();
        return ApiResponse.ok(EVENTO_LIST_RETRIEVED, eventos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un evento", description = "Consulta un evento con sus subtareas por su identificador.")
    public ApiResponse<EventoResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(EVENTO_RETRIEVED, mapper.toResponse(getEventoUseCase.execute(id)));
    }

    @PostMapping
    @Operation(summary = "Crear un evento", description = "Crea un evento y, opcionalmente, su plan inicial de subtareas (US-01, US-02).")
    public ResponseEntity<ApiResponse<EventoResponse>> create(@Valid @RequestBody CreateEventoRequest request) {
        List<NuevaSubtareaData> subtareas = request.subtareas() == null
                ? List.of()
                : request.subtareas().stream()
                        .map(s -> new NuevaSubtareaData(s.nombre(), s.fechaObjetivo(), s.horasEstimadas()))
                        .toList();

        var evento = createEventoUseCase.execute(
                request.nombre(),
                request.tipo(),
                request.cliente(),
                request.contactoCliente(),
                request.fechaHora(),
                request.lugar(),
                request.plazoLimite(),
                subtareas
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(EVENTO_CREATED, mapper.toResponse(evento)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar un evento", description = "Actualiza los campos enviados de un evento existente (US-03).")
    public ApiResponse<EventoResponse> update(@PathVariable UUID id, @RequestBody UpdateEventoRequest request) {
        var evento = updateEventoUseCase.execute(
                id,
                request.nombre(),
                request.tipo(),
                request.cliente(),
                request.contactoCliente(),
                request.fechaHora(),
                request.lugar(),
                request.plazoLimite()
        );
        return ApiResponse.ok(EVENTO_UPDATED, mapper.toResponse(evento));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un evento", description = "Elimina un evento y sus subtareas asociadas (US-03).")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        deleteEventoUseCase.execute(id);
        return ApiResponse.ok(EVENTO_DELETED, null);
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Progreso del evento", description = "Devuelve el avance de preparacion del evento (US-10).")
    public ApiResponse<ProgressResponse> progress(@PathVariable UUID id) {
        var progress = getEventoProgressUseCase.execute(id);
        return ApiResponse.ok(EVENTO_PROGRESS_RETRIEVED,
                new ProgressResponse(progress.done(), progress.total(), progress.percentage()));
    }
}
