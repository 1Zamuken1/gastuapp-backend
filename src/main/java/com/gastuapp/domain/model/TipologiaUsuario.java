package com.gastuapp.domain.model;

/**
 * Tipología del usuario, no afecta permisos
 */

public enum TipologiaUsuario {
    /**
     * Estudiante (cualquier nivel educativo)
     */
    ESTUDIANTE,

    /**
     * Trabajador con empleo formal
     */
    TRABAJADOR,

    /**
     * Trabajador independiente/freelance
     */
    INDEPENDIENTE,

    /**
     * Otra situación o prefiere no decirlo
     */
    OTRO
}
