package com.gastuapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    // Expresión regular para validar email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$"
    );

    // Atributos
    // Identificación
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String password;

    // Roles y Permisos
    private RolUsuario rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Info demográfica (opcional)
    private TipologiaUsuario tipologia;
    private String profesion;
    private String institucion;

    // Relación padre - hijo (FK de user_hijo hacia user_padre)
    private Long tutorId;

    // OAuth (A futuro por ahora)
    private String googleId;

    // Lógica
    public void validar(){
        validarNombre();
        validarEmail();
        validarPassword();
        validarRol();
        validarRelacionPadreHijo();
    }

    // Validación para que nombre y apellidos noe stén vacíos
    private void validarNombre(){
        if (nombre == null || nombre.trim().isEmpty()){
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

    // Validación para email
    private void validarEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()){
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }

    // Validación para password (debe estar hasheado)
    private void validarPassword(){
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("El password es obligatorio");
        }
    }

    // Validación para rol
    private void validarRol() {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    /**
     * Valida la relación padre-hijo.
     * Un USER_HIJO DEBE tener tutorId.
     * Un USER o ADMIN NO DEBE tener tutorId.
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
     */
    public boolean puedeTenerHijos() {
        return rol == RolUsuario.USER;
    }

    /**
     * Verifica si el usuario es hijo (supervisado).
     */
    public boolean esHijo() {
        return rol == RolUsuario.USER_HIJO;
    }

    /**
     * Verifica si el usuario es administrador.
     */
    public boolean esAdmin() {
        return rol == RolUsuario.ADMIN;
    }

    /**
     * Verifica si el usuario tiene un tutor asignado.
     */
    public boolean tieneTutor() {
        return tutorId != null;
    }

    /**
     * Inicializa valores por defecto después de la construcción.
     * Usado por mappers y builders.
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
