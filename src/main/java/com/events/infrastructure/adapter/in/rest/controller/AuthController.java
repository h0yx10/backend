package com.events.infrastructure.adapter.in.rest.controller;

import static com.events.infrastructure.utils.constants.MessageConstants.CURRENT_USER_RETRIEVED;
import static com.events.infrastructure.utils.constants.MessageConstants.LOGIN_SUCCESS;
import static com.events.infrastructure.utils.constants.MessageConstants.REGISTER_SUCCESS;

import com.events.application.port.in.GetCurrentUserPort;
import com.events.application.port.in.LoginPort;
import com.events.application.port.in.UsuariosPort;
import com.events.infrastructure.adapter.in.rest.dto.UpdatePerfilRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.events.application.port.in.RegisterPort;
import com.events.infrastructure.adapter.in.rest.dto.ApiResponse;
import com.events.infrastructure.adapter.in.rest.dto.AuthResponse;
import com.events.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.events.infrastructure.adapter.in.rest.dto.RegisterRequest;
import com.events.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import com.events.infrastructure.adapter.in.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Registro, inicio de sesion y usuario actual (US-11)")
public class AuthController {

    private final RegisterPort registerUseCase;
    private final LoginPort loginUseCase;
    private final GetCurrentUserPort getCurrentUserUseCase;
    private final UsuarioRestMapper mapper;
    private final UsuariosPort usuarios;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Registrarse", description = "Crea un organizador con rol ORGANIZADOR y devuelve su token de acceso.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUseCase.execute(request.nombre(), request.correo(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(REGISTER_SUCCESS, mapper.toResponse(result)));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesion", description = "Valida correo y contrasena y devuelve un token de acceso (JWT).")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(request.correo(), request.password());
        return ApiResponse.ok(LOGIN_SUCCESS, mapper.toResponse(result));
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Devuelve el usuario dueno del token enviado.")
    public ApiResponse<UsuarioResponse> me() {
        return ApiResponse.ok(CURRENT_USER_RETRIEVED, mapper.toResponse(getCurrentUserUseCase.execute()));
    }
    @PatchMapping("/me")
    @Operation(summary = "Editar perfil propio", description = "Cambiar password exige passwordActual. No permite roles ni activo.")
    public ApiResponse<UsuarioResponse> updateMe(@Valid @RequestBody UpdatePerfilRequest body) {
        return ApiResponse.ok("Perfil actualizado correctamente.", mapper.toResponse(usuarios.updateCurrent(
                body.nombre(), body.correo(), body.password(), body.passwordActual())));
    }
    @DeleteMapping("/me")
    @Operation(summary = "Eliminar cuenta propia", description = "409 si tiene datos de negocio o es el ultimo ADMIN habilitado.")
    public ApiResponse<Void> deleteMe() {
        usuarios.deleteCurrent();
        return ApiResponse.ok("Cuenta eliminada correctamente.", null);
    }
}
