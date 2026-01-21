package com.gastuapp.domain.model.transaccion;

/**
 * Tipo de Transacción
 * <p>
 * Define si la transacción es un ingreso o egreso.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
public enum TipoTransaccion {
    /**
     * Ingreso de dinero (salario, freelance, regalo)
     */
    INGRESO,

    /**
     * Egreso de dinero (compra, pago, gasto)
     */
    EGRESO
}
