package com.gastuapp.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response: AuthResponseDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: AuthService (Application Layer)
 * - ENVÍA DATOS A: AuthController (Infrastructure Layer) → Cliente (JSON)
 * - CONVERTIDO DESDE: Usuario (Domain) + JWT token
 *
 * RESPONSABILIDAD:
 * Representa la respuesta de autenticación exitosa.
 * Contiene el token JWT y datos básicos del usuario autenticado.
 * Se retorna tanto en login como en register.
 *
 * EJEMPLO JSON DE RESPUESTA:
 * {
 * "token":
 * "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicHVibGljSWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJyb2wiOiJVU0VSIiwiaWF0IjoxNjc0NTY3ODkwLCJleHAiOjE2NzQ2NTQyOTAsImlzcyI6Ikdhc3R1QXBwIn0.signature",
 * "type": "Bearer",
 * "publicId": "550e8400-e29b-41d4-a716-446655440000",
 * "email": "user@example.com",
 * "rol": "USER"
 * }
 *
 * USO EN CLIENTE:
 * 1. Guardar token en localStorage/sessionStorage
 * 2. Incluir en header de requests: "Authorization: Bearer <token>"
 * 3. Mostrar datos del usuario (email, rol) en UI
 * 4. Redirigir según rol (ADMIN → dashboard, USER → home)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    /**
     * Token JWT firmado con HS256.
     * El cliente debe enviarlo en cada request protegido:
     * Header: Authorization: Bearer <token>
     * 
     * Estructura del token:
     * - Header: algoritmo HS256
     * - Payload: email, publicId, rol, iat, exp, iss
     * - Signature: HMAC-SHA256 con secret key
     * 
     * Expiración: 24 horas (configurable en application.properties)
     */
    private String token;

    /**
     * Tipo de token (siempre "Bearer" para JWT).
     * Usado para especificar el esquema de autenticación en el header.
     * 
     * Formato completo: "Bearer <token>"
     */
    private String type = "Bearer";

    /**
     * UUID público del usuario autenticado.
     * Se usa en lugar del id interno para referencias externas.
     * 
     * Ejemplo: "550e8400-e29b-41d4-a716-446655440000"
     */
    private String publicId;

    /**
     * Email del usuario autenticado.
     * Usado para mostrar en UI y como identificador único.
     */
    private String email;

    /**
     * Rol del usuario autenticado.
     * Valores posibles: "ADMIN", "USER", "USER_HIJO"
     * 
     * El cliente puede usar este campo para:
     * - Mostrar/ocultar opciones de menú
     * - Redirigir a diferentes dashboards
     * - Validaciones del lado del cliente (NO sustituye validación del servidor)
     */
    private String rol;
}