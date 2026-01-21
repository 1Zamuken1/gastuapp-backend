package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.usuario.RolUsuario;
import com.gastuapp.domain.model.usuario.TipologiaUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request: AdminCrearUsuarioRequestDTO
 *
 * FLUJO DE DATOS:
 * - USADO EN: POST /api/admin/usuarios (solo ADMIN autenticado)
 * - ROL ASIGNADO: Seleccionado por el ADMIN
 * - CONVERTIDO A: Usuario (Domain)
 *
 * RESPONSABILIDAD:
 * DTO para que un ADMIN cree usuarios con cualquier rol.
 * Solo accesible por usuarios con rol ADMIN.
 * Permite elegir el rol del nuevo usuario.
 *
 * EJEMPLO JSON:
 * {
 *   "nombre": "Carlos",
 *   "apellido": "Admin",
 *   "email": "carlos@gastuapp.com",
 *   "telefono": "3001234567",
 *   "password": "admin123",
 *   "rol": "ADMIN",
 *   "tipologia": "TRABAJADOR",
 *   "profesion": "Administrador del Sistema",
 *   "institucion": "GastuApp",
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
public class AdminCrearUsuarioRequestDTO {
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

    //  ============ Rol Seleccionable (Solo Admin) ============
    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol; // ADMIN puede elegir: ADMIN, USER, USER_HIJO

    private TipologiaUsuario tipologia;

    @Size(max = 255, message = "La profesión no puede exceder 255 caracteres")
    private String profesion;

    @Size(max = 255, message = "La institución no puede exceder 255 caracteres")
    private String institucion;

    //  ============ Relación Padre-Hijo (Opcional) ============
    /**
     * ID del tutor (solo si rol = USER_HIJO).
     * El ADMIN puede asignar un tutor manualmente.
     */
    private Long tutorId;
}
