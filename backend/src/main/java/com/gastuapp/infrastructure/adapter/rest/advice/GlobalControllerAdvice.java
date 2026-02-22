package com.gastuapp.infrastructure.adapter.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Advisor: GlobalControllerAdvice
 * 
 * Centraliza el manejo de excepciones para todos los controladores.
 * 
 * RESPONSABILIDADES:
 * - Convertir IllegalArgumentException en 400 Bad Request.
 * - Convertir IllegalStateException (Auth) en 401 Unauthorized.
 * - Convertir IllegalStateException (Estado) en 409 Conflict.
 * - Convertir RuntimeException en 500 Internal Server Error.
 */
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        String message = ex.getMessage();

        // Distinguir entre errores de autenticación y conflictos de estado
        if (message != null && (message.contains("Usuario no autenticado") || message.contains("token JWT"))) {
            return createResponse(HttpStatus.UNAUTHORIZED, message);
        }

        return createResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> createResponse(HttpStatus status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("status", String.valueOf(status.value()));
        return new ResponseEntity<>(response, status);
    }
}
