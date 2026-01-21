package com.gastuapp.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request: LoginRequestDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: AuthController (JSON → Java)
 * - ENVÍA DATOS A: AuthService
 *
 * RESPONSABILIDAD:
 * Representa las credenciales de login del usuario.
 * Solo contiene email y password.
 *
 * EJEMPLO JSON:
 * {
 *   "email": "juan@example.com",
 *   "password": "password123"
 * }
 *
 * VALIDACIONES:
 * - Email debe ser válido
 * - Password no puede estar vacío
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El password es obligatorio")
    private String password;
}
