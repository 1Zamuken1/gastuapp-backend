package com.gastuapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Domain Model: Usuario
 *
 * FLUJO DE DATOS:
 * - CREADO POR: UsuarioService (Application Layer)
 * - USADO POR: UsuarioService, UsuarioRepositoryPort
 * - CONVERTIDO A: UsuarioEntity (Infrastructure Layer) via UsuarioEntityMapper
 * - CONVERTIDO DESDE: UsuarioRequestDTO (Application Layer) via UsuarioMapper
 * - CONVERTIDO HACIA: UsuarioResponseDTO (Application Layer) via UsuarioMapper
 *
 * RESPONSABILIDAD:
 * Modelo de dominio puro que representa un usuario del sistema.
 * Contiene SOLO lógica de negocio (validaciones, reglas).
 * NO conoce detalles técnicos (JPA, HTTP, JSON).
 *
 * REGLAS DE NEGOCIO:
 * - Email debe ser único y válido
 * - USER_HIJO debe tener tutorId
 * - USER y ADMIN no pueden tener tutorId
 * - Solo USER puede tener hijos
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    // Expresión regular para validar email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // ==================== ATRIBUTOS ====================

    // Identificación
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String password; // Hasheado con BCrypt

    // Roles y permisos
    private RolUsuario rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Información demográfica (OPCIONAL)
    private TipologiaUsuario tipologia; // ESTUDIANTE, TRABAJADOR, etc.
    private String profesion; // Campo libre: "Estudiante de Ingeniería", "Diseñador", etc.
    private String institucion; // Campo libre: "Universidad Nacional", "Google", null

    // Relación padre-hijo (SOLO para USER_HIJO)
    private Long tutorId; // FK a Usuario padre (solo si rol = USER_HIJO)

    // OAuth (FUTURO)
    private String googleId;

    // ==================== LÓGICA DE NEGOCIO ====================

    /**
     * Valida que el usuario cumpla con todas las reglas de negocio.
     *
     * VALIDACIONES:
     * - Nombre y apellido: 2-100 caracteres
     * - Email: formato válido
     * - Password: no vacío (ya hasheado)
     * - Rol: no null
     * - Relación padre-hijo: USER_HIJO debe tener tutorId
     *
     * @throws IllegalArgumentException si alguna validación falla
     */
    public void validar() {
        validarNombre();
        validarEmail();
        validarPassword();
        validarRol();
        validarRelacionPadreHijo();
    }

    /**
     * Valida que el nombre y apellido no estén vacíos.
     */
    private void validarNombre() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 100 caracteres");
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (apellido.length() < 2 || apellido.length() > 100) {
            throw new IllegalArgumentException("El apellido debe tener entre 2 y 100 caracteres");
        }
    }

    /**
     * Valida formato de email.
     */
    private void validarEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }

    /**
     * Valida que el password exista (ya debe venir hasheado).
     */
    private void validarPassword() {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("El password es obligatorio");
        }
    }

    /**
     * Valida que el rol sea válido.
     */
    private void validarRol() {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    /**
     * Valida la relación padre-hijo.
     *
     * REGLAS:
     * - USER_HIJO DEBE tener tutorId
     * - USER y ADMIN NO DEBEN tener tutorId
     */
    private void validarRelacionPadreHijo() {
        if (rol == RolUsuario.USER_HIJO && tutorId == null) {
            throw new IllegalArgumentException(
                    "Un usuario hijo debe tener un tutor asignado"
            );
        }

        if ((rol == RolUsuario.USER || rol == RolUsuario.ADMIN) && tutorId != null) {
            throw new IllegalArgumentException(
                    "Un usuario normal o admin no puede tener tutor"
            );
        }
    }

    /**
     * Verifica si el usuario puede crear hijos (ser tutor).
     * Solo USER puede tener hijos.
     *
     * @return true si rol = USER, false en otro caso
     */
    public boolean puedeTenerHijos() {
        return rol == RolUsuario.USER;
    }

    /**
     * Verifica si el usuario es hijo (supervisado).
     *
     * @return true si rol = USER_HIJO, false en otro caso
     */
    public boolean esHijo() {
        return rol == RolUsuario.USER_HIJO;
    }

    /**
     * Verifica si el usuario es administrador.
     *
     * @return true si rol = ADMIN, false en otro caso
     */
    public boolean esAdmin() {
        return rol == RolUsuario.ADMIN;
    }

    /**
     * Verifica si el usuario tiene un tutor asignado.
     *
     * @return true si tutorId != null, false en otro caso
     */
    public boolean tieneTutor() {
        return tutorId != null;
    }

    /**
     * Inicializa valores por defecto después de la construcción.
     * Usado por mappers cuando crean instancias.
     */
    public void inicializarValoresPorDefecto() {
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}