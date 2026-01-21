package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Request: TransaccionRequestDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Controller (JSON → Java)
 * - ENVÍA DATOS A: TransaccionService
 * - CONVERTIDO A: Transaccion (Domain) via TransaccionMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos que llegan desde el cliente (Angular/Postman).
 * Contiene validaciones de entrada (Jakarta Validation).
 * Se usa para operaciones de creación y actualización de transacciones.
 *
 * VALIDACIONES:
 * - @NotNull: Campo obligatorio
 * - @Positive: Monto mayor a 0
 * - @NotBlank: String no vacío
 * - @Size: Longitud mínima/máxima
 * - @PastOrPresent: Fecha no futura (opcional)
 *
 * EJEMPLO JSON:
 * {
 * "monto": 45000.50,
 * "tipo": "EGRESO",
 * "descripcion": "Compra de mercado en Éxito",
 * "fecha": "2025-01-21",
 * "categoriaId": 1
 * }
 *
 * NOTAS:
 * - usuarioId NO se envía en el request (se obtiene del JWT)
 * - fechaCreacion se asigna automáticamente en el Service
 * - proyeccionId es opcional (solo para proyecciones automáticas - FUTURO)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionRequestDTO {

    // ==================== DATOS BÁSICOS ====================

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto no puede exceder 13 dígitos enteros y 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "El tipo es obligatorio")
    private TipoTransaccion tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 3, max = 500, message = "La descripción debe tener entre 3 y 500 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha es obligatoria")
    // Opcional: descomentar si no se permiten fechas futuras
    // @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    // ==================== RELACIONES ====================

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    // ==================== CAMPOS OPCIONALES (FUTURO) ====================

    /**
     * ID de proyección (solo para transacciones automáticas).
     * Por defecto null (transacción manual).
     */
    private Long proyeccionId;

    // ==================== CAMPOS NO INCLUIDOS ====================
    // - usuarioId: Se obtiene del JWT en el Controller
    // - fechaCreacion: Se asigna automáticamente en el Service
    // - esAutomatica: Se determina automáticamente (true si proyeccionId != null)
}