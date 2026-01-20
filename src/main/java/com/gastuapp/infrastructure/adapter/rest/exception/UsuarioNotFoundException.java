package com.gastuapp.infrastructure.adapter.rest.exception;

    /**
     * Excepción Custom: UsuarioNotFoundException
     * 
     * FLUJO DE DATOS:
     * - LANZADA POR: Services cuando no se encuentra un usuario
     * - CAPTURADA POR: GlobalExceptionHandler
     * - CONVERTIDA A: HTTP 404 Not Found
     * 
     * RESPONSABILIDAD:
     * Representa un error específico: usuario no encontrado.
     * Se lanza cuando se busca un usuario que no existe.
     * 
     * @author Juan Esteban Barrios Portela
     * @version 1.0
     * @since 2025-01-20
     */
public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(String message) {
        super(message);
    }

    public UsuarioNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
