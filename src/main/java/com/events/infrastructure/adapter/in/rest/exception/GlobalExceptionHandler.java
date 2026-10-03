package com.events.infrastructure.adapter.in.rest.exception;

import static com.events.infrastructure.utils.constants.MessageConstants.INVALID_REQUEST;
import static com.events.infrastructure.utils.constants.MessageConstants.UNAUTHENTICATED;
import static com.events.infrastructure.utils.constants.MessageConstants.UNEXPECTED_ERROR;

import com.events.domain.exception.CapacityConflictException;
import com.events.domain.exception.CorreoYaRegistradoException;
import com.events.domain.exception.CredencialesInvalidasException;
import com.events.domain.exception.EventoNotFoundException;
import com.events.domain.exception.OrganizadorNotFoundException;
import com.events.domain.exception.SubtareaNotFoundException;
import com.events.domain.exception.UsuarioNotFoundException;
import com.events.domain.exception.UsuarioConflictException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EventoNotFoundException.class, SubtareaNotFoundException.class, OrganizadorNotFoundException.class, UsuarioNotFoundException.class})
    ResponseEntity<Map<String, Object>> notFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CapacityConflictException.class)
    ResponseEntity<Map<String, Object>> capacityConflict(CapacityConflictException exception) {
        Map<String, Object> body = Map.of(
                "success", false,
                "message", exception.getMessage(),
                "plannedHours", exception.getPlannedHours(),
                "limitHours", exception.getLimitHours(),
                "exceedsBy", exception.getExceedsBy(),
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UsuarioConflictException.class)
    ResponseEntity<Map<String, Object>> usuarioConflict(UsuarioConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso.");
    }

    @ExceptionHandler(CorreoYaRegistradoException.class)
    ResponseEntity<Map<String, Object>> correoYaRegistrado(CorreoYaRegistradoException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    ResponseEntity<Map<String, Object>> credencialesInvalidas(CredencialesInvalidasException exception) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> unauthenticated(AuthenticationException exception) {
        return error(HttpStatus.UNAUTHORIZED, UNAUTHENTICATED);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, Object>> invalidArgument(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> malformedBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, INVALID_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(INVALID_REQUEST);
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> generic(Exception exception) {
        log.error("Error inesperado al procesar la solicitud", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "success", false,
                "message", message,
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(status).body(body);
    }
}
