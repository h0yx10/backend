package com.events.infrastructure.adapter.in.rest.controller;

import com.events.application.port.in.ListUsuariosPort;
import com.events.application.port.in.UsuariosPort;
import com.events.infrastructure.adapter.in.rest.dto.*;
import com.events.infrastructure.adapter.in.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Administracion", description = "CRUD de usuarios restringido al rol ADMIN")
public class AdminController {
    private final ListUsuariosPort listUsuariosUseCase;
    private final UsuariosPort usuarios;
    private final UsuarioRestMapper mapper;
    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ApiResponse<List<UsuarioResponse>> listUsers() {
        return ApiResponse.ok("Usuarios consultados correctamente.", listUsuariosUseCase.execute().stream().map(mapper::toResponse).toList());
    }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario")
    public ApiResponse<UsuarioResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Usuario consultado correctamente.", mapper.toResponse(usuarios.get(id)));
    }
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Roles por defecto: ORGANIZADOR. No emite token.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody CreateUsuarioRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Usuario creado correctamente.",
                mapper.toResponse(usuarios.create(body.nombre(), body.correo(), body.password(), body.roles()))));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "La contrasena propia exige passwordActual; activo requiere perfil.")
    public ApiResponse<UsuarioResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUsuarioRequest body) {
        return ApiResponse.ok("Usuario actualizado correctamente.", mapper.toResponse(usuarios.update(id,
                body.nombre(), body.correo(), body.password(), body.passwordActual(), body.roles(), body.activo())));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "409 si tiene datos de negocio o es el ultimo ADMIN habilitado.")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        usuarios.delete(id);
        return ApiResponse.ok("Usuario eliminado correctamente.", null);
    }
}
