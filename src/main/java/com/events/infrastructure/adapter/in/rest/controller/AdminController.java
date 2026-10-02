package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.USUARIO_LIST_RETRIEVED;

import com.events.application.port.in.ListUsuariosPort;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import com.events.infrastructure.adapter.in.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administracion", description = "Operaciones restringidas al rol ADMIN")
public class AdminController {

    private final ListUsuariosPort listUsuariosUseCase;
    private final UsuarioRestMapper mapper;

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios registrados. Requiere rol ADMIN.")
    public ApiResponse<List<UsuarioResponse>> listUsers() {
        return ApiResponse.ok(USUARIO_LIST_RETRIEVED, listUsuariosUseCase.execute().stream().map(mapper::toResponse).toList());
    }
}
