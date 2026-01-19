package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.RolUsuario;
import com.gastuapp.domain.model.TipologiaUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request: UsuarioRequestDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Controller (JSON → Java)
 * - ENVÍA DATOS A: UsuarioService
 * - CONVERTIDO A: Usuario (Domain) via UsuarioMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos que llegan desde el cliente (Angular/Postman).
 * Contiene validaciones de entrada (Jakarta Validation).
 * Se usa para operaciones de creación y actualización de usuarios.
 *
 * VALIDACIONES:
 * - @NotBlank: Campo obligatorio y no vacío
 * - @Email: Formato de email válido
 * - @Size: Longitud mínima/máxima
 * - @NotNull: No puede ser null
 *
 * EJEMPLO JSON:
 * {
 *   "nombre": "Juan",
 *   "apellido": "Pérez",
 *   "email": "juan@example.com",
 *   "telefono": "3001234567",
 *   "password": "password123",
 *   "rol": "USER",
 *   "tipologia": "TRABAJADOR",
 *   "profesion": "Ingeniero",
 *   "institucion": "Tech Corp",
 *   "tutorId": null
 * }
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    //  ============  Datos Básicos ============
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

    //  ============ Roles y Tipología ============
    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    private TipologiaUsuario tipologia;

    //  ============ Info demográfica (opcional) ============
    @Size(max = 255, message = "La profesión no puede exceder 255 caracteres")
    private String profesion;

    @Size(max = 255, message = "La institución no puede exceder 255 caracteres")
    private String institucion;

    //  ============ Relación Padre-Hijo ============
    /**
     * ID del tutor (padre).
     * Solo se usa si rol = USER_HIJO.
     * El Service validará que este ID exista en la BD.
     */
    private Long tutorId;

    //  ============ OAuth (Futuro) ============
    private String googleId;
}
