package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.planificacion.FrecuenciaPresupuesto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO Request: ActualizarPresupuestoRequestDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Controller (JSON → Java)
 * - ENVÍA DATOS A: PresupuestoService
 * - CONVERTIDO A: Presupuesto (Domain) via PresupuestoMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos para actualizar una planificación de presupuesto existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 *
 * VALIDACIONES:
 * - @Positive: Monto mayor a 0 (si se proporciona)
 * - Si se actualizan fechas, fechaFin > fechaInicio
 *
 * EJEMPLO JSON:
 * {
 *   "montoTope": 600000.00,
 *   "fechaFin": "2026-02-15",
 *   "autoRenovar": false
 * }
 *
 * NOTAS:
 * - usuarioId y categoriaId NO se pueden cambiar (no incluidos)
 * - Solo los campos proporcionados se actualizan
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPresupuestoRequestDTO {

    // ==================== DATOS BÁSICOS (OPCIONALES) ====================

    @Positive(message = "El monto tope debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto no puede exceder 13 dígitos enteros y 2 decimales")
    private Double montoTope;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private FrecuenciaPresupuesto frecuencia;

    private Boolean autoRenovar;

    // ==================== VALIDACIONES ADICIONALES ====================

    /**
     * Validación personalizada para asegurar que fechaFin > fechaInicio.
     * Esta validación se realiza en el Service layer.
     */
    public void validarFechas() {
        if (fechaFin != null && fechaInicio != null) {
            if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
            }
        }
    }

    /**
     * Verifica si hay datos para actualizar.
     */
    public boolean tieneDatosParaActualizar() {
        return montoTope != null || 
               fechaInicio != null || 
               fechaFin != null || 
               frecuencia != null || 
               autoRenovar != null;
    }
}