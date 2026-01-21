package com.gastuapp.domain.model.transaccion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Model: Transaccion
 *
 * FLUJO DE DATOS:
 * - CREADO POR: TransaccionService (Application Layer)
 * - USADO POR: Cálculo de balance, reportes, gráficos
 * - CONVERTIDO A: TransaccionEntity (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Modelo de dominio que representa un ingreso o egreso.
 * Contiene lógica de negocio y validaciones.
 *
 * REGLAS DE NEGOCIO:
 * - Monto siempre positivo (tipo define si suma o resta)
 * - Categoría obligatoria
 * - Fecha no puede ser futura (opcional - por definir)
 * - Descripción máximo 500 caracteres
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    // ==================== ATRIBUTOS ====================

    private Long id;
    private BigDecimal monto; // Siempre positivo
    private TipoTransaccion tipo; // INGRESO o EGRESO
    private String descripcion; // Max 500 chars
    private LocalDate fecha; // Fecha de la transacción
    private LocalDateTime fechaCreacion; // Timestamp de registro
    private Long categoriaId; // FK (OBLIGATORIO)
    private Long usuarioId; // FK

    // Para proyecciones (FUTURO - Fase 3)
    private Long proyeccionId; // FK (opcional)
    private Boolean esAutomatica; // true si fue creada por proyección

    // ==================== LÓGICA DE NEGOCIO ====================

    /**
     * Valida que la transacción cumpla con las reglas de negocio.
     *
     * VALIDACIONES:
     * - Monto > 0
     * - Tipo obligatorio
     * - Descripción no vacía y <= 500 caracteres
     * - Fecha obligatoria
     * - CategoriaId obligatorio
     * - UsuarioId obligatorio
     *
     * @throws IllegalArgumentException si validación falla
     */
    public void validar() {
        validarMonto();
        validarTipo();
        validarDescripcion();
        validarFecha();
        validarCategoriaId();
        validarUsuarioId();
    }

    private void validarMonto() {
        if (monto == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
    }

    private void validarTipo() {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
    }

    private void validarDescripcion() {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (descripcion.length() > 500) {
            throw new IllegalArgumentException(
                    "La descripción no puede exceder 500 caracteres");
        }
    }

    private void validarFecha() {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        // Opcional: validar que fecha no sea futura
        // if (fecha.isAfter(LocalDate.now())) {
        // throw new IllegalArgumentException("La fecha no puede ser futura");
        // }
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

    /**
     * Inicializa valores por defecto.
     */
    public void inicializarValoresPorDefecto() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.esAutomatica == null) {
            this.esAutomatica = false;
        }
    }

    /**
     * Verifica si es un ingreso.
     */
    public boolean esIngreso() {
        return tipo == TipoTransaccion.INGRESO;
    }

    /**
     * Verifica si es un egreso.
     */
    public boolean esEgreso() {
        return tipo == TipoTransaccion.EGRESO;
    }

    /**
     * Retorna el monto con signo según el tipo.
     * Ingreso: +monto
     * Egreso: -monto
     */
    public BigDecimal getMontoConSigno() {
        return esIngreso() ? monto : monto.negate();
    }
}