package com.gastuapp.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response: AuthResponseDTO
 *
 * FLUJO: AuthService → AuthController → Cliente
 * RESPONSABILIDAD: Respuesta de login exitoso con token JWT
 *
 * EJEMPLO JSON:
 * {
 * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 * "type": "Bearer",
 * "publicId": "550e8400-e29b-41d4-a716-446655440000",
 * "email": "user@example.com",
 * "rol": "USER"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private String publicId;
    private String email;
    private String rol;
}
