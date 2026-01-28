package com.gastuapp.domain.model.planificacion;

/**
 * Frecuencia de Presupuesto
 * <p>
 * Define la periodicidad con que se renueva o aplica un presupuesto.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
public enum FrecuenciaPresupuesto {
    /**
     * Presupuesto semanal (se renueva cada 7 días)
     */
    SEMANAL,

    /**
     * Presupuesto quincenal (se renueva cada 15 días)
     */
    QUINCENAL,

    /**
     * Presupuesto mensual (se renueva cada mes)
     */
    MENSUAL,

    /**
     * Presupuesto trimestral (se renova cada 3 meses)
     */
    TRIMESTRAL,

    /**
     * Presupuesto semestral (se renueva cada 6 meses)
     */
    SEMESTRAL,

    /**
     * Presupuesto anual (se renueva cada año)
     */
    ANUAL
}