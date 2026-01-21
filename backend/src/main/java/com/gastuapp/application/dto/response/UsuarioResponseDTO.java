package com.gastuapp.application.dto.response;

import com.gastuapp.domain.model.usuario.RolUsuario;
import com.gastuapp.domain.model.usuario.TipologiaUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO Response: UsuarioResponseDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: UsuarioService (via UsuarioMapper)
 * - ENVÍA DATOS A: Controller (Java → JSON)
 * - CONVERTIDO DESDE: Usuario (Domain) via UsuarioMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos que se envían al cliente (Angular/Postman).
 * NO incluye información sensible (password).
 * Usa publicId en lugar de id interno.
 *
 * SEGURIDAD:
 * - NO expone el 'id' interno (BIGINT)
 * - SÍ expone el 'publicId' (UUID) - seguro
 * - NO expone el 'password' (nunca se envía al cliente)
 *
 * EJEMPLO JSON DE RESPUESTA:
 * {
 * "publicId": "550e8400-e29b-41d4-a716-446655440000",
 * "nombre": "Juan",
 * "apellido": "Pérez",
 * "email": "juan@example.com",
 * "telefono": "3001234567",
 * "rol": "USER",
 * "tipologia": "TRABAJADOR",
 * "activo": true,
 * "fechaCreacion": "2025-01-19T10:30:00",
 * "profesion": "Ingeniero",
 * "institucion": "Tech Corp",
 * "tutorId": null
 * }
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    // ============ Identificación pública ============
    /**
     * ID interno del usuario (BIGINT).
     * NOTA: Este campo NO debe exponerse en APIs públicas.
     * Solo se usa internamente para validaciones de permisos.
     */
    private Long id;

    /**
     * UUID público del usuario.
     * Este es el ID que se usa en URLs y referencias externas.
     * NO exponer el id interno (BIGINT) por seguridad.
     */
    private String publicId;

    // ============ Datos básicos ============
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;

    // ============ Roles y estado ============
    private RolUsuario rol;
    private TipologiaUsuario tipologia;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // ============ Información demográfica ============
    private String profesion;
    private String institucion;

    // ============ Relación Padre-Hijo ============
    /**
     * ID interno del tutor.
     * Nota: Aquí exponemos el id interno porque es una FK.
     * En el futuro podríamos cambiarlo a tutorPublicId si queremos más seguridad.
     */
    private Long tutorId;

    // ============ OAtuth ============
    private String googleId;
}
