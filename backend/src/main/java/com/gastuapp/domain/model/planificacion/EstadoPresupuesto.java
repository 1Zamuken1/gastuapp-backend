package com.gastuapp.domain.model.planificacion;

/**
 * Estado de Presupuesto
 * <p>
 * Define el estado actual de una planificación de presupuesto.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
public enum EstadoPresupuesto {
    /**
     * Presupuesto activo y vigente
     */
    ACTIVA,

    /**
     * Presupuesto desactivado por el usuario o por vencimiento
     */
    INACTIVA,

    /**
     * Presupuesto que ha excedido el monto tope permitido
     */
    EXCEDIDA
}