package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.OVERLOAD_CHECK_RETRIEVED;

import com.events.application.port.in.CheckOverloadConflictPort;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.OverloadCheckRequest;
import com.events.infrastructure.adapter.in.rest.dto.OverloadCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors-origin}")
@Tag(name = "Conflictos", description = "Deteccion de sobrecarga diaria antes de confirmar una reprogramacion (US-07, US-08)")
public class ConflictController {

    private final CheckOverloadConflictPort checkOverloadConflictUseCase;

    @PostMapping("/api/subtasks/{id}/conflicts/overload")
    @Operation(
            summary = "Previsualizar sobrecarga",
            description = "Calcula si reprogramar la subtarea a la fecha/horas indicadas superaria el limite diario, sin guardar el cambio."
    )
    public ApiResponse<OverloadCheckResponse> check(@PathVariable UUID id, @RequestBody OverloadCheckRequest request) {
        var result = checkOverloadConflictUseCase.execute(id, request.fechaObjetivo(), request.horasEstimadas());
        return ApiResponse.ok(OVERLOAD_CHECK_RETRIEVED,
                new OverloadCheckResponse(result.conflict(), result.plannedHours(), result.limitHours(), result.exceedsBy()));
    }
}
