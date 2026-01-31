package com.gastuapp.domain.model.proyeccion;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain Model: Proyeccion
 *
 * FLUJO DE DATOS:
 * - CREADO POR: ProyeccionService
 * - PERSISTIDO EN: ProyeccionEntity
 * - USADO POR: ProyeccionService para generar Transacciones
 *
 * RESPONSABILIDAD:
 * Representa una plantilla de ingreso o egreso recurrente.
 * Contiene la lógica para validar y calcular próximas ejecuciones.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proyeccion {

    private Long id;
    private String nombre;
    private BigDecimal monto;
    private TipoTransaccion tipo;
    private Long categoriaId;
    private Long usuarioId;
    private Frecuencia frecuencia;
    private LocalDate fechaInicio;
    private LocalDate ultimaEjecucion; // Null si nunca se ha ejecutado
    private Boolean activo; // Soft Delete

    /**
     * Valida reglas de negocio básicas.
     */
    public void validar() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
        if (categoriaId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (frecuencia == null) {
            throw new IllegalArgumentException("La frecuencia es obligatoria");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
    }

    /**
     * Calcula la próxima fecha de ejecución aproximada basada en la última
     * ejecución.
     * Esto es solo informativo, ya que la ejecución es manual.
     */
    public LocalDate calcularProximoCobro() {
        LocalDate base = ultimaEjecucion != null ? ultimaEjecucion : fechaInicio;
        // Si nunca se ha ejecutado y la fecha de inicio es futura, es la fecha de
        // inicio.
        // Si nunca se ha ejecutado y la fecha de inicio es pasada o hoy, ya debería
        // estar disponible (return base).
        if (ultimaEjecucion == null) {
            return base;
        }

        return switch (frecuencia) {
            case SEMANAL -> base.plusWeeks(1);
            case QUINCENAL -> base.plusWeeks(2);
            case MENSUAL -> base.plusMonths(1);
            case BIMESTRAL -> base.plusMonths(2);
            case SEMESTRAL -> base.plusMonths(6);
            case ANUAL -> base.plusYears(1);
            case UNICA -> null; // No se repite
        };
    }

    /**
     * Realiza un soft delete.
     */
    public void desactivar() {
        this.activo = false;
    }
}
