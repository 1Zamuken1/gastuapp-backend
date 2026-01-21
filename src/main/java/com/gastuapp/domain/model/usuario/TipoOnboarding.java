package com.gastuapp.domain.model.usuario;

/**
 * Estado del Onboarding (Vigila el paso a paso del usuario durante el registro)
 */

public enum TipoOnboarding {
    /**
     * Usuario no ha completado el onboarding
     */
    NO_COMPLETADO,

    /**
     * Onboarding básico: solo registro (email + password).
     */
    BASICO,

    /**
     * Onboarding completo: vinculación de cuenta o primera transacción
     */
    COMPLETO
}
