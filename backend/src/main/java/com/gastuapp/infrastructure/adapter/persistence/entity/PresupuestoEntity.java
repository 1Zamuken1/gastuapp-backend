package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.math.RoundingMode;

/**
 * JPA Entity: PresupuestoEntity
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: PresupuestoRepositoryAdapter
 * - ENVÍA DATOS A: PostgreSQL (tabla 'presupuestos_planificaciones')
 *
 * RESPONSABILIDAD:
 * Mapea el modelo Presupuesto a la tabla 'presupuestos_planificaciones' en
 * PostgreSQL.
 *
 * TABLA EN BD:
 * CREATE TABLE presupuestos_planificaciones (
 * id BIGSERIAL PRIMARY KEY,
 * public_id VARCHAR(36) UNIQUE NOT NULL,
 * usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
 * categoria_id BIGINT NOT NULL REFERENCES categorias(id),
 * monto_tope DECIMAL(15,2) NOT NULL,
 * monto_gastado DECIMAL(15,2) DEFAULT 0.00,
 * fecha_inicio DATE NOT NULL,
 * fecha_fin DATE NOT NULL,
 * frecuencia VARCHAR(20) NOT NULL,
 * estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
 * auto_renovar BOOLEAN DEFAULT false,
 * fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 * 
 * UNIQUE(usuario_id, categoria_id, estado)
 * );
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Entity
@Table(name = "presupuestos_planificaciones", indexes = {
        @Index(name = "idx_presupuesto_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_presupuesto_usuario", columnList = "usuario_id"),
        @Index(name = "idx_presupuesto_categoria", columnList = "categoria_id"),
        @Index(name = "idx_presupuesto_estado", columnList = "estado"),
        @Index(name = "idx_presupuesto_fechas", columnList = "fecha_inicio, fecha_fin"),
        @Index(name = "idx_presupuesto_frecuencia", columnList = "frecuencia"),
        @Index(name = "idx_presupuesto_usuario_categoria_estado", columnList = "usuario_id, categoria_id, estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    @Column(name = "monto_tope", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTope;

    @Column(name = "monto_gastado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoGastado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "frecuencia", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FrecuenciaPresupuestoEnum frecuencia;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoPresupuestoEnum estado;

    @Column(name = "auto_renovar", nullable = false)
    private Boolean autoRenovar = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // ==================== LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (autoRenovar == null) {
            autoRenovar = false;
        }
        if (montoGastado == null) {
            montoGastado = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = EstadoPresupuestoEnum.ACTIVA;
        }
    }

    // ==================== ENUMS ====================

    public enum FrecuenciaPresupuestoEnum {
        SEMANAL,
        QUINCENAL,
        MENSUAL,
        TRIMESTRAL,
        SEMESTRAL,
        ANUAL
    }

    public enum EstadoPresupuestoEnum {
        ACTIVA,
        INACTIVA,
        EXCEDIDA
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Verifica si el presupuesto está vigente.
     */
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return estado == EstadoPresupuestoEnum.ACTIVA &&
                !hoy.isBefore(fechaInicio) &&
                !hoy.isAfter(fechaFin);
    }

    /**
     * Verifica si el presupuesto ha sido excedido.
     */
    public boolean estaExcedido() {
        if (montoGastado == null || montoTope == null) {
            return false;
        }
        return montoGastado.compareTo(montoTope) >= 0;
    }

    /**
     * Calcula el porcentaje de utilización.
     */
    public double getPorcentajeUtilizacion() {
        if (montoTope == null || montoTope.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (montoGastado == null) {
            return 0.0;
        }
        return montoGastado.multiply(BigDecimal.valueOf(100))
                .divide(montoTope, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calcula el monto restante.
     */
    public BigDecimal getMontoRestante() {
        if (montoTope == null || montoGastado == null) {
            return montoTope != null ? montoTope : BigDecimal.ZERO;
        }
        BigDecimal restante = montoTope.subtract(montoGastado);
        return restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante;
    }
}