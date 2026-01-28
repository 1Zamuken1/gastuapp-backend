package com.gastuapp.domain.model.planificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Model: Presupuesto
 *
 * FLUJO DE DATOS:
 * - CREADO POR: PresupuestoService (Application Layer)
 * - USADO POR: Dashboard, reportes, control de gastos
 * - CONVERTIDO A: PresupuestoEntity (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Modelo de dominio que representa una planificación de tope de gastos
 * para una categoría específica en un período determinado.
 * Contiene lógica de negocio y validaciones.
 *
 * REGLAS DE NEGOCIO:
 * - Monto tope siempre positivo
 * - Fecha fin debe ser posterior a fecha inicio
 * - Solo se permite un presupuesto activo por (usuario + categoría)
 * - Categoría debe ser de tipo EGRESO
 * - El monto gastado se calcula automáticamente desde transacciones
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    // ==================== ATRIBUTOS ====================

    private Long id;
    private String publicId;
    private BigDecimal montoTope; // Límite máximo de gasto permitido
    private BigDecimal montoGastado; // Monto acumulado gastado (calculado)
    private LocalDate fechaInicio; // Inicio del período
    private LocalDate fechaFin; // Fin del período
    private FrecuenciaPresupuesto frecuencia; // Periodicidad
    private EstadoPresupuesto estado; // Estado actual
    private Boolean autoRenovar; // Si se renueva automáticamente
    private LocalDateTime fechaCreacion; // Timestamp de registro
    private Long categoriaId; // FK a categoría (obligatorio)
    private Long usuarioId; // FK a usuario

    // ==================== LÓGICA DE NEGOCIO ====================

    /**
     * Valida que el presupuesto cumpla con las reglas de negocio.
     *
     * VALIDACIONES:
     * - Monto tope > 0
     * - Fecha fin > fecha inicio
     * - Frecuencia obligatoria
     * - Estado obligatorio
     * - CategoriaId obligatorio
     * - UsuarioId obligatorio
     *
     * @throws IllegalArgumentException si validación falla
     */
    public void validar() {
        validarMontoTope();
        validarFechas();
        validarFrecuencia();
        validarEstado();
        validarCategoriaId();
        validarUsuarioId();
        validarAutoRenovar();
    }

    private void validarMontoTope() {
        if (montoTope == null) {
            throw new IllegalArgumentException("El monto tope es obligatorio");
        }
        if (montoTope.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto tope debe ser mayor a 0");
        }
    }

    private void validarFechas() {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (fechaFin == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private void validarFrecuencia() {
        if (frecuencia == null) {
            throw new IllegalArgumentException("La frecuencia es obligatoria");
        }
    }

    private void validarEstado() {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
    }

    private void validarCategoriaId() {
        if (categoriaId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    private void validarUsuarioId() {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
    }

    private void validarAutoRenovar() {
        if (autoRenovar == null) {
            autoRenovar = false;
        }
    }

    /**
     * Inicializa valores por defecto.
     */
    public void inicializarValoresPorDefecto() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPresupuesto.ACTIVA;
        }
        if (this.autoRenovar == null) {
            this.autoRenovar = false;
        }
        if (this.montoGastado == null) {
            this.montoGastado = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si el presupuesto está vigente según fechas y estado.
     *
     * @return true si está vigente, false en caso contrario
     */
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return estado == EstadoPresupuesto.ACTIVA &&
                !hoy.isBefore(fechaInicio) &&
                !hoy.isAfter(fechaFin);
    }

    /**
     * Verifica si el presupuesto ha sido excedido.
     *
     * @return true si montoGastado >= montoTope, false en caso contrario
     */
    public boolean estaExcedido() {
        if (montoGastado == null || montoTope == null) {
            return false;
        }
        return montoGastado.compareTo(montoTope) >= 0;
    }

    /**
     * Calcula el porcentaje de utilización del presupuesto.
     *
     * @return porcentaje de uso (0.0 a 1.0)
     */
    public double getPorcentajeUtilizacion() {
        if (montoTope == null || montoTope.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (montoGastado == null) {
            return 0.0;
        }
        return montoGastado.divide(montoTope, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calcula el monto restante disponible.
     *
     * @return monto disponible para gastar
     */
    public BigDecimal getMontoRestante() {
        if (montoTope == null || montoGastado == null) {
            return montoTope != null ? montoTope : BigDecimal.ZERO;
        }
        BigDecimal restante = montoTope.subtract(montoGastado);
        return restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante;
    }

    /**
     * Agrega un monto gastado al presupuesto.
     *
     * @param monto monto a agregar (debe ser positivo)
     */
    public void agregarGasto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de gasto debe ser mayor a 0");
        }

        if (this.montoGastado == null) {
            this.montoGastado = BigDecimal.ZERO;
        }

        this.montoGastado = this.montoGastado.add(monto);

        // Actualizar estado si se excede
        if (estaExcedido() && estado == EstadoPresupuesto.ACTIVA) {
            this.estado = EstadoPresupuesto.EXCEDIDA;
        }
    }

    /**
     * Resta un monto gastado del presupuesto (para correcciones).
     *
     * @param monto monto a restar (debe ser positivo)
     */
    public void restarGasto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a restar debe ser mayor a 0");
        }

        if (this.montoGastado == null) {
            this.montoGastado = BigDecimal.ZERO;
        }

        this.montoGastado = this.montoGastado.subtract(monto);

        // Asegurar que no sea negativo
        if (this.montoGastado.compareTo(BigDecimal.ZERO) < 0) {
            this.montoGastado = BigDecimal.ZERO;
        }

        // Reactivar si estaba excedido y ya no lo está
        if (estado == EstadoPresupuesto.EXCEDIDA && !estaExcedido()) {
            this.estado = EstadoPresupuesto.ACTIVA;
        }
    }

    /**
     * Desactiva el presupuesto.
     */
    public void desactivar() {
        this.estado = EstadoPresupuesto.INACTIVA;
    }

    /**
     * Verifica si el presupuesto es auto-renovable.
     */
    public boolean esAutoRenovable() {
        return Boolean.TRUE.equals(autoRenovar);
    }

    /**
     * Genera un nuevo período basado en la frecuencia actual.
     *
     * @return nuevo rango de fechas
     */
    public PeriodoPresupuesto generarNuevoPeriodo() {
        LocalDate nuevaFechaInicio = fechaFin.plusDays(1);
        LocalDate nuevaFechaFin;

        switch (frecuencia) {
            case SEMANAL:
                nuevaFechaFin = nuevaFechaInicio.plusWeeks(1).minusDays(1);
                break;
            case QUINCENAL:
                nuevaFechaFin = nuevaFechaInicio.plusWeeks(2).minusDays(1);
                break;
            case MENSUAL:
                nuevaFechaFin = nuevaFechaInicio.plusMonths(1).minusDays(1);
                break;
            case TRIMESTRAL:
                nuevaFechaFin = nuevaFechaInicio.plusMonths(3).minusDays(1);
                break;
            case SEMESTRAL:
                nuevaFechaFin = nuevaFechaInicio.plusMonths(6).minusDays(1);
                break;
            case ANUAL:
                nuevaFechaFin = nuevaFechaInicio.plusYears(1).minusDays(1);
                break;
            default:
                throw new IllegalStateException("Frecuencia no soportada: " + frecuencia);
        }

        return new PeriodoPresupuesto(nuevaFechaInicio, nuevaFechaFin);
    }

    /**
     * Clase interna para representar un período.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodoPresupuesto {
        private LocalDate inicio;
        private LocalDate fin;
    }
}