package com.gastuapp.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request: CrearHijoRequestDTO
 *
 * FLUJO DE DATOS:
 * - USADO EN: POST /api/usuarios/hijo (usuario autenticado crea hijo)
 * - ROL ASIGNADO: Siempre USER_HIJO (no seleccionable)
 * - TUTOR: Usuario autenticado (obtenido del JWT)
 * - CONVERTIDO A: Usuario (Domain) con rol = USER_HIJO
 *
 * RESPONSABILIDAD:
 * DTO para que un padre (USER) cree un usuario hijo supervisado.
 * El rol siempre será USER_HIJO.
 * El tutorId se obtiene del usuario autenticado (JWT).
 *
 * EJEMPLO JSON:
 * {
 *   "nombre": "María",
 *   "apellido": "Pérez",
 *   "email": "maria@example.com",
 *   "telefono": "3009876543",
 *   "password": "password123",
 *   "profesion": "Estudiante de secundaria",
 *   "institucion": "Colegio San José"
 * }
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearHijoRequestDTO {
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

    //  ============ Información del hijo ============
    @Size(max = 255, message = "La profesión no puede exceder 255 caracteres")
    private String profesion;

    @Size(max = 255, message = "La institución no puede exceder 255 caracteres")
    private String institucion;

    // ==================== CAMPOS NO INCLUIDOS ====================
    // - rol: Siempre será USER_HIJO (asignado por el Service)
    // - tipologia: Siempre será ESTUDIANTE (asignado por el Service)
    // - tutorId: Se obtiene del usuario autenticado (JWT)
    // - activo: Siempre true
}
