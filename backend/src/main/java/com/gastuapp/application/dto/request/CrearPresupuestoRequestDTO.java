package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.planificacion.FrecuenciaPresupuesto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO Request: CrearPresupuestoRequestDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Controller (JSON → Java)
 * - ENVÍA DATOS A: PresupuestoService
 * - CONVERTIDO A: Presupuesto (Domain) via PresupuestoMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos para crear una nueva planificación de presupuesto.
 * Contiene validaciones de entrada (Jakarta Validation).
 *
 * VALIDACIONES:
 * - @NotNull: Campo obligatorio
 * - @Positive: Monto mayor a 0
 * - @Future: Fecha fin posterior a fecha inicio
 *
 * EJEMPLO JSON:
 * {
 *   "montoTope": 500000.00,
 *   "fechaInicio": "2026-01-01",
 *   "fechaFin": "2026-01-31",
 *   "frecuencia": "MENSUAL",
 *   "autoRenovar": true,
 *   "categoriaId": 5
 * }
 *
 * NOTAS:
 * - usuarioId NO se envía en el request (se obtiene del JWT)
 * - estado se asigna automáticamente como ACTIVA
 * - montoGastado se inicializa en 0
 * - fechaCreacion se asigna automáticamente
 * - publicId se genera automáticamente
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPresupuestoRequestDTO {

    // ==================== DATOS BÁSICOS ====================

    @NotNull(message = "El monto tope es obligatorio")
    @Positive(message = "El monto tope debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto no puede exceder 13 dígitos enteros y 2 decimales")
    private Double montoTope;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "La frecuencia es obligatoria")
    private FrecuenciaPresupuesto frecuencia;

    // ==================== OPCIONES ====================

    /**
     * Indica si el presupuesto se renueva automáticamente al vencer.
     * Por defecto es false.
     */
    private Boolean autoRenovar = false;

    // ==================== RELACIONES ====================

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

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

    // ==================== CAMPOS NO INCLUIDOS ====================
    // - usuarioId: Se obtiene del JWT en el Controller
    // - estado: Se asigna automáticamente como ACTIVA
    // - montoGastado: Se inicializa automáticamente en 0
    // - fechaCreacion: Se asigna automáticamente en el Service
    // - publicId: Se genera automáticamente
}