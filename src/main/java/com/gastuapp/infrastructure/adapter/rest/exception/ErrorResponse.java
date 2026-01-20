package com.gastuapp.infrastructure.adapter.rest.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ErrorResponse
 * 
 * FLUJO DE DATOS:
 * - CREADO POR: GlobalExceptionHandler
 * - ENVIADO A: Cliente (Java → JSON)
 * 
 * RESPONSABILIDAD:
 * Estructura estándar para errores HTTP.
 * Proporciona información detallada del error al cliente.
 * 
 * EJEMPLO JSON:
 * {
 *   "timestamp": "2025-01-20T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "El email ya está registrado",
 *   "path": "/api/auth/register",
 *   "errors": ["El email ya está registrado en el sistema"]
 * }
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /**
     * Timestamp del error (cuándo ocurrió).
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP (400, 404, 500, etc).
     */
    private int status;

    /**
     * Descripción breve del error (Bad Request, Not Found, etc).
     */
    private String error;

    /**
     * Mensaje detallado del error.
     */
    private String message;

    /**
     * Ruta del endpoint donde ocurrió el error.
     */
    private String path;

    /**
     * Lista de errores detallados (para validaciones).
     * Ejemplo: ["El nombre es obligatorio", "El email debe ser válido"]
     */
    private List<String> errors;

    /**
     * Constructor simplificado sin lista de errores.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error,
        String message, String path){
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }
}
