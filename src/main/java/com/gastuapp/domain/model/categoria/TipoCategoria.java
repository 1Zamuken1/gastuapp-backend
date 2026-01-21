package com.gastuapp.domain.model.categoria;

/**
 * Tipo de Categoría
 * <p>
 * Define si la categoría se puede usar para ingresos, egresos o ambos.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */

public enum TipoCategoria {
    /**
     * Solo para ingresos (Salario, Freelance, etc.)
     */
    INGRESO,

    /**
     * Solo para egresos (Comida, Transporte, etc.)
     */
    EGRESO,

    /**
     * Para ingresos y egresos (Otros)
     */
    AMBOS
}
