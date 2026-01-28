package com.gastuapp.application.dto.response;

import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;
import com.gastuapp.domain.model.planificacion.FrecuenciaPresupuesto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO Response: PresupuestoResponseDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: PresupuestoService (via PresupuestoMapper)
 * - ENVÍA DATOS A: Controller (Java → JSON)
 * - CONVERTIDO DESDE: Presupuesto (Domain) via PresupuestoMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos de un presupuesto que se envían al cliente
 * (Angular/Postman).
 * Incluye información de la categoría (nombre e ícono) y campos calculados
 * para mejorar la experiencia del usuario.
 *
 * EJEMPLO JSON DE RESPUESTA:
 * {
 * "id": 123,
 * "publicId": "550e8400-e29b-41d4-a716-446655440000",
 * "montoTope": 500000.00,
 * "montoGastado": 320000.00,
 * "montoRestante": 180000.00,
 * "porcentajeUtilizacion": 64.0,
 * "fechaInicio": "2026-01-01",
 * "fechaFin": "2026-01-31",
 * "frecuencia": "MENSUAL",
 * "estado": "ACTIVA",
 * "autoRenovar": true,
 * "fechaCreacion": "2026-01-01T08:00:00",
 * "categoriaId": 5,
 * "categoriaNombre": "Comida y bebidas",
 * "categoriaIcono": "🍔",
 * "estaVigente": true,
 * "estaExcedido": false,
 * "diasRestantes": 4
 * }
 *
 * NOTAS:
 * - categoriaNombre y categoriaIcono se incluyen para evitar múltiples requests
 * - Montos calculados se incluyen para facilitar visualizaciones en frontend
 * - Estado de vigencia y exceso se calculan dinámicamente
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoResponseDTO {

    // ==================== IDENTIFICACIÓN ====================

    private Long id;
    private String publicId;
    private Long usuarioId;

    // ==================== DATOS DEL PRESUPUESTO ====================

    private BigDecimal montoTope;
    private BigDecimal montoGastado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private FrecuenciaPresupuesto frecuencia;
    private EstadoPresupuesto estado;
    private Boolean autoRenovar;
    private LocalDateTime fechaCreacion;

    // ==================== CATEGORÍA (DENORMALIZADA PARA UX) ====================

    /**
     * ID de la categoría.
     */
    private Long categoriaId;

    /**
     * Nombre de la categoría (denormalizado).
     * Ejemplo: "Comida y bebidas"
     */
    private String categoriaNombre;

    /**
     * Ícono de la categoría (denormalizado).
     * Ejemplo: "🍔"
     */
    private String categoriaIcono;

    // ==================== CAMPOS CALCULADOS ====================

    /**
     * Monto restante disponible para gastar.
     * montoTope - montoGastado (nunca negativo)
     */
    private BigDecimal montoRestante;

    /**
     * Porcentaje de utilización del presupuesto (0.0 a 1.0+).
     * montoGastado / montoTope * 100
     */
    private Double porcentajeUtilizacion;

    /**
     * Indica si el presupuesto está vigente actualmente.
     * Vigente = estado ACTIVA y fecha actual dentro del rango
     */
    private Boolean estaVigente;

    /**
     * Indica si el presupuesto ha sido excedido.
     * true si montoGastado >= montoTope
     */
    private Boolean estaExcedido;

    /**
     * Días restantes hasta la fecha de fin.
     * Positivo: días que faltan
     * 0: vence hoy
     * Negativo: días de vencido
     */
    private Integer diasRestantes;

    // ==================== MÉTODOS DE CÁLCULO ====================

    /**
     * Calcula el porcentaje de utilización (redondeado a 1 decimal).
     */
    public Double calcularPorcentajeUtilizacion() {
        if (montoTope == null || montoTope.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (montoGastado == null) {
            return 0.0;
        }
        BigDecimal porcentaje = montoGastado
                .multiply(BigDecimal.valueOf(100))
                .divide(montoTope, 1, RoundingMode.HALF_UP);
        return porcentaje.doubleValue();
    }

    /**
     * Calcula el monto restante.
     */
    public BigDecimal calcularMontoRestante() {
        if (montoTope == null || montoGastado == null) {
            return montoTope != null ? montoTope : BigDecimal.ZERO;
        }
        BigDecimal restante = montoTope.subtract(montoGastado);
        return restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante;
    }

    /**
     * Calcula los días restantes hasta la fecha fin.
     */
    public Integer calcularDiasRestantes() {
        if (fechaFin == null) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaFin);
    }

    /**
     * Verifica si está vigente.
     */
    public Boolean calcularEstaVigente() {
        LocalDate hoy = LocalDate.now();
        return estado == EstadoPresupuesto.ACTIVA &&
                !hoy.isBefore(fechaInicio) &&
                !hoy.isAfter(fechaFin);
    }

    /**
     * Verifica si está excedido.
     */
    public Boolean calcularEstaExcedido() {
        if (montoGastado == null || montoTope == null) {
            return false;
        }
        return montoGastado.compareTo(montoTope) >= 0;
    }

    /**
     * Retorna una descripción del estado actual.
     */
    public String getDescripcionEstado() {
        if (!calcularEstaVigente()) {
            return "Vencido";
        }
        if (calcularEstaExcedido()) {
            return "Excedido";
        }
        return "Activo";
    }

    /**
     * Retorna el color para visualización según estado.
     */
    public String getColorEstado() {
        if (!calcularEstaVigente()) {
            return "secondary"; // Gris para vencidos
        }
        if (calcularEstaExcedido()) {
            return "danger"; // Rojo para excedidos
        }
        if (calcularPorcentajeUtilizacion() >= 80) {
            return "warning"; // Naranja para cerca del límite
        }
        return "success"; // Verde para todo bien
    }
}