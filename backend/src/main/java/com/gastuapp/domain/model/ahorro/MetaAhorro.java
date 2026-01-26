package com.gastuapp.domain.model.ahorro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de Dominio que representa una Meta de Ahorro.
 * 
 * <p>
 * Una meta de ahorro es un objetivo financiero que el usuario desea alcanzar en
 * un tiempo determinado.
 * Funciona como un "contenedor" al cual se le pueden realizar abonos
 * periódicos.
 * </p>
 * 
 * <p>
 * Reglas de negocio:
 * </p>
 * <ul>
 * <li>El monto objetivo debe ser mayor a 0.</li>
 * <li>El monto actual se calcula sumando los abonos (Domain Service).</li>
 * <li>El estado se actualiza automáticamente según el progreso.</li>
 * </ul>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaAhorro {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private BigDecimal montoObjetivo;
    private BigDecimal montoActual;
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaInicio; // Inicio del plan
    private FrecuenciaAhorro frecuencia; // Frecuencia de abonos
    private String color; // Hexadecimal para UI (branding Calm Design)
    private String icono; // Identificador de icono PrimeNG
    private EstadoMeta estado;
    private LocalDateTime fechaCreacion;

    public enum EstadoMeta {
        ACTIVA, // En proceso
        COMPLETADA, // Meta alcanzada (montoActual >= montoObjetivo)
        PAUSADA, // Usuario detuvo aportes temporalmente
        CANCELADA // Usuario desistió
    }

    /**
     * Calcula el porcentaje de progreso actual.
     * 
     * @return Valor entre 0 y 100.
     */
    public double calcularProgreso() {
        if (montoObjetivo == null || montoObjetivo.compareTo(BigDecimal.ZERO) == 0)
            return 0.0;
        if (montoActual == null)
            return 0.0;

        return montoActual.divide(montoObjetivo, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .doubleValue();
    }
}
