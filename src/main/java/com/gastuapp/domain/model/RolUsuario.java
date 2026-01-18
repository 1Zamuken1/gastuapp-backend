package com.gastuapp.domain.model;

/**
 * Roles de Usuario en el sistema
 * Niveles de acceso y permisos
 */
public enum RolUsuario {
    /**
     * Admin del sistema
     * Gestiona usuarios, configuraciones y notificaciones
     */
    ADMIN,

    /**
     * Usuario normal con acceso completo.
     * Puede crear presupuestos, ahorros, metas, etc.
     * Puede crear cuentas de hijos para supervisarlos.
     */
    USER,

    /**
     * Usuario hijo, supervisado por un tutor.
     * Acceso restringido, puede ser supervisado por un USER padre.
     */
    USER_HIJO
}
