package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.CAPACIDAD_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.CAPACIDAD_UPDATED;

import com.events.application.port.in.GetCapacidadPort;
import com.events.application.port.in.UpdateCapacidadPort;
import com.events.domain.entity.CapacidadDiaria;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.CapacidadRequest;
import com.events.infrastructure.adapter.in.rest.dto.CapacidadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capacity")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors-origin}")
@Tag(name = "Capacidad", description = "Configuracion del limite diario de horas del organizador (US-12)")
public class CapacidadController {

    private final GetCapacidadPort getCapacidadUseCase;
    private final UpdateCapacidadPort updateCapacidadUseCase;

    @GetMapping
    @Operation(summary = "Consultar limite diario", description = "Devuelve el limite configurado, o 6h por defecto si no existe.")
    public ApiResponse<CapacidadResponse> get() {
        CapacidadDiaria capacidad = getCapacidadUseCase.execute();
        return ApiResponse.ok(CAPACIDAD_RETRIEVED, toResponse(capacidad));
    }

    @PutMapping
    @Operation(summary = "Actualizar limite diario", description = "Actualiza el limite diario (rango permitido: 1..16 horas).")
    public ApiResponse<CapacidadResponse> update(@Valid @RequestBody CapacidadRequest request) {
        CapacidadDiaria capacidad = updateCapacidadUseCase.execute(request.limiteHoras());
        return ApiResponse.ok(CAPACIDAD_UPDATED, toResponse(capacidad));
    }

    private CapacidadResponse toResponse(CapacidadDiaria capacidad) {
        if (capacidad == null) {
            return new CapacidadResponse(CapacidadDiaria.LIMITE_POR_DEFECTO, true, null);
        }
        return new CapacidadResponse(capacidad.getLimiteHoras(), false, capacidad.getFecha());
    }
}
