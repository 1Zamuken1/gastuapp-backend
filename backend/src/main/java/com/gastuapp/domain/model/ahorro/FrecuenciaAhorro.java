package com.gastuapp.domain.model.ahorro;

/**
 * Enumeración que define las frecuencias de ahorro disponibles.
 * 
 * <p>
 * Se utiliza para calcular los intervalos de las cuotas automáticas
 * en una Meta de Ahorro.
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
public enum FrecuenciaAhorro {
    DIARIO,
    SEMANAL,
    QUINCENAL,
    MENSUAL,
    TRIMESTRAL,
    SEMESTRAL,
    ANUAL;

    /**
     * Obtiene los días aproximados que representa esta frecuencia.
     * Útil para cálculos rápidos de proyección.
     */
    public int getDiasAproximados() {
        return switch (this) {
            case DIARIO -> 1;
            case SEMANAL -> 7;
            case QUINCENAL -> 15;
            case MENSUAL -> 30;
            case TRIMESTRAL -> 90;
            case SEMESTRAL -> 180;
            case ANUAL -> 365;
        };
    }
}
