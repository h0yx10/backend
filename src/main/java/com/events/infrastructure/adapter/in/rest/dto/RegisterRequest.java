package com.events.infrastructure.adapter.in.rest.dto;

import static com.events.infrastructure.utils.constants.MessageConstants.CORREO_INVALID;
import static com.events.infrastructure.utils.constants.MessageConstants.CORREO_REQUIRED;
import static com.events.infrastructure.utils.constants.MessageConstants.NOMBRE_USUARIO_MAX_LENGTH;
import static com.events.infrastructure.utils.constants.MessageConstants.NOMBRE_REQUIRED;
import static com.events.infrastructure.utils.constants.MessageConstants.PASSWORD_LENGTH;
import static com.events.infrastructure.utils.constants.MessageConstants.PASSWORD_REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un nuevo organizador (usuario)")
public record RegisterRequest(
        @Schema(example = "Camila Restrepo")
        @NotBlank(message = NOMBRE_REQUIRED)
        @Size(max = 120, message = NOMBRE_USUARIO_MAX_LENGTH)
        String nombre,

        @Schema(example = "camila@correo.com")
        @NotBlank(message = CORREO_REQUIRED)
        @Email(message = CORREO_INVALID)
        @Size(max = 180, message = CORREO_INVALID)
        String correo,

        @Schema(example = "Secreta123", description = "Entre 8 y 72 caracteres")
        @NotBlank(message = PASSWORD_REQUIRED)
        @Size(min = 8, max = 72, message = PASSWORD_LENGTH)
        String password
) {
}
