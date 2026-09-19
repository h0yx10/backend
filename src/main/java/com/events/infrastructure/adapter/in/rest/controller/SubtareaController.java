package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.SUBTAREA_CREATED;
import static com.events.infrastructure.utils.constants.MessageConstants.SUBTAREA_DELETED;
import static com.events.infrastructure.utils.constants.MessageConstants.SUBTAREA_LIST_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.SUBTAREA_STATUS_UPDATED;
import static com.events.infrastructure.utils.constants.MessageConstants.SUBTAREA_UPDATED;

import com.events.application.port.in.ChangeSubtareaStatusPort;
import com.events.application.port.in.CreateSubtareaPort;
import com.events.application.port.in.DeleteSubtareaPort;
import com.events.application.port.in.ListSubtareasPort;
import com.events.application.port.in.UpdateSubtareaPort;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.ChangeSubtareaStatusRequest;
import com.events.infrastructure.adapter.in.rest.dto.CreateSubtareaRequest;
import com.events.infrastructure.adapter.in.rest.dto.SubtareaResponse;
import com.events.infrastructure.adapter.in.rest.dto.UpdateSubtareaRequest;
import com.events.infrastructure.adapter.in.rest.mapper.SubtareaRestMapper;
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
@RequiredArgsConstructor
@Tag(name = "Subtareas", description = "Plan de trabajo logistico, reprogramacion y ejecucion (US-02, US-03, US-06, US-09)")
public class SubtareaController {

    private final CreateSubtareaPort createSubtareaUseCase;
    private final ListSubtareasPort listSubtareasUseCase;
    private final UpdateSubtareaPort updateSubtareaUseCase;
    private final ChangeSubtareaStatusPort changeSubtareaStatusUseCase;
    private final DeleteSubtareaPort deleteSubtareaUseCase;
    private final SubtareaRestMapper mapper;

    @PostMapping("/api/events/{eventId}/subtasks")
    @Operation(summary = "Agregar subtarea", description = "Crea una subtarea logistica para un evento (US-02).")
    public ResponseEntity<ApiResponse<SubtareaResponse>> create(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateSubtareaRequest request
    ) {
        var subtarea = createSubtareaUseCase.execute(eventId, request.nombre(), request.fechaObjetivo(), request.horasEstimadas());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SUBTAREA_CREATED, mapper.toResponse(subtarea)));
    }

    @GetMapping("/api/events/{eventId}/subtasks")
    @Operation(summary = "Listar subtareas de un evento", description = "Lista las subtareas logisticas de un evento.")
    public ApiResponse<List<SubtareaResponse>> listByEvento(@PathVariable UUID eventId) {
        List<SubtareaResponse> subtareas = listSubtareasUseCase.execute(eventId).stream().map(mapper::toResponse).toList();
        return ApiResponse.ok(SUBTAREA_LIST_RETRIEVED, subtareas);
    }

    @PatchMapping("/api/subtasks/{id}")
    @Operation(
            summary = "Editar o reprogramar una subtarea",
            description = "Actualiza los campos enviados. Cambiar fechaObjetivo y/o horasEstimadas valida "
                    + "sobrecarga diaria (US-06, US-07) y devuelve 409 si se supera el limite."
    )
    public ApiResponse<SubtareaResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateSubtareaRequest request) {
        var subtarea = updateSubtareaUseCase.execute(id, request.nombre(), request.fechaObjetivo(), request.horasEstimadas());
        return ApiResponse.ok(SUBTAREA_UPDATED, mapper.toResponse(subtarea));
    }

    @PatchMapping("/api/subtasks/{id}/status")
    @Operation(summary = "Registrar ejecucion", description = "Marca una subtarea como hecha o pospuesta, con nota opcional (US-09).")
    public ApiResponse<SubtareaResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeSubtareaStatusRequest request) {
        var subtarea = changeSubtareaStatusUseCase.execute(id, request.estado(), request.nota());
        return ApiResponse.ok(SUBTAREA_STATUS_UPDATED, mapper.toResponse(subtarea));
    }

    @DeleteMapping("/api/subtasks/{id}")
    @Operation(summary = "Eliminar subtarea", description = "Elimina una subtarea logistica (US-03).")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        deleteSubtareaUseCase.execute(id);
        return ApiResponse.ok(SUBTAREA_DELETED, null);
    }
}
