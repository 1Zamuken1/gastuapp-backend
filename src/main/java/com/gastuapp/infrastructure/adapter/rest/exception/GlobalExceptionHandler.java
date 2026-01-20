package com.gastuapp.infrastructure.adapter.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * 
 * FLUJO DE DATOS:
 * - CAPTURA: Excepciones lanzadas por Controllers y Services
 * - CONVIERTE: Excepciones Java → ErrorResponse (JSON)
 * - RETORNA: HTTP Response con código apropiado
 * 
 * RESPONSABILIDAD:
 * Manejo centralizado de excepciones en toda la aplicación.
 * Convierte excepciones en respuestas HTTP consistentes.
 * Proporciona mensajes de error claros al cliente.
 * 
 * EXCEPCIONES MANEJADAS:
 * - IllegalArgumentException → 400 Bad Request
 * - UsuarioNotFoundException → 404 Not Found
 * - MethodArgumentNotValidException → 400 Bad Request (validaciones)
 * - Exception → 500 Internal Server Error (fallback)
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@RestController
public class GlobalExceptionHandler {
    /**
     * Maneja IllegalArgumentException.
     * Usado cuando hay errores de validación de negocio.
     * 
     * CASOS:
     * - Email duplicado
     * - Teléfono duplicado
     * - Usuario hijo sin tutor
     * - Validaciones del Domain
     * 
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja UsuarioNotFoundException.
     * Usado cuando se busca un usuario que no existe.
     * 
     * @return HTTP 404 Not Found
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNotFoundException(
            UsuarioNotFoundException ex,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja MethodArgumentNotValidException.
     * Usado cuando fallan las validaciones de Jakarta (@NotBlank, @Email, etc.).
     * 
     * EJEMPLO:
     * Request: { "nombre": "", "email": "invalid" }
     * Response: {
     * "message": "Errores de validación",
     * "errors": [
     * "El nombre es obligatorio",
     * "El email debe tener un formato válido"
     * ]
     * }
     * 
     * @return HTTP 400 Bad Request con lista de errores
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraer todos los errores de validación
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        return fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Errores de validación en los datos enviados",
                request.getRequestURI(),
                errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    };

    /**
     * Maneja UnsupportedOperationException.
     * Usado para métodos no implementados aún.
     * 
     * @return HTTP 501 Not Implemented
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(
            UnsupportedOperationException ex,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_IMPLEMENTED.value(),
                "Not Implemented",
                ex.getMessage(),
                request.getRequestURI()
            );
        return new ResponseEntity<>(error, HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Maneja cualquier otra excepción no capturada.
     * Fallback para errores inesperados.
     * 
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        // Log del error (en producción usar un logger apropiado)
        System.err.println("Error inesperado: " + ex.getMessage());
        ex.printStackTrace();
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Ocurrió un error inesperado en el servidor",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}