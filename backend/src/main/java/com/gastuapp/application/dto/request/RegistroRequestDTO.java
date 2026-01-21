package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.usuario.TipologiaUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request: RegistroRequestDTO
 *
 * FLUJO DE DATOS:
 * - USADO EN: POST /api/auth/register (registro público)
 * - ROL ASIGNADO: Siempre USER (no seleccionable)
 * - CONVERTIDO A: Usuario (Domain) con rol = USER automático
 *
 * RESPONSABILIDAD:
 * DTO para registro público de nuevos usuarios.
 * El usuario NO puede elegir su rol (siempre será USER).
 * El Service asignará automáticamente rol = USER.
 *
 * EJEMPLO JSON:
 * {
 *   "nombre": "Juan",
 *   "apellido": "Pérez",
 *   "email": "juan@example.com",
 *   "telefono": "3001234567",
 *   "password": "password123",
 *   "tipologia": "TRABAJADOR",
 *   "profesion": "Ingeniero",
 *   "institucion": "Tech Corp"
 * }
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequestDTO {
    //  ============ Datos básicos ============
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    private String password;

    //  ============ Información opcional ============
    private TipologiaUsuario tipologia;

    @Size(max = 255, message = "La profesión no puede exceder 255 caracteres")
    private String profesion;

    @Size(max = 255, message = "La institución no puede exceder 255 caracteres")
    private String institucion;

    // ==================== CAMPOS NO INCLUIDOS ====================
    // - rol: Siempre será USER (asignado por el Service)
    // - tutorId: No aplica para registro público
    // - activo: Siempre true (asignado por el Service)
}
