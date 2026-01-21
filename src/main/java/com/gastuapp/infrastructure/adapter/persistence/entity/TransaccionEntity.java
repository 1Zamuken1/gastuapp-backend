package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity: TransaccionEntity
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: TransaccionRepositoryAdapter
 * - ENVÍA DATOS A: PostgreSQL (tabla 'transacciones')
 *
 * RESPONSABILIDAD:
 * Mapea el modelo Transaccion a la tabla 'transacciones' en PostgreSQL.
 *
 * TABLA EN BD:
 * CREATE TABLE transacciones (
 *   id BIGSERIAL PRIMARY KEY,
 *   monto DECIMAL(15,2) NOT NULL,
 *   tipo VARCHAR(20) NOT NULL,
 *   descripcion VARCHAR(500) NOT NULL,
 *   fecha DATE NOT NULL,
 *   fecha_creacion TIMESTAMP NOT NULL,
 *   categoria_id BIGINT NOT NULL REFERENCES categorias(id),
 *   usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
 *   proyeccion_id BIGINT REFERENCES proyecciones(id),
 *   es_automatica BOOLEAN DEFAULT false
 * );
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Entity
@Table(name = "transacciones", indexes = {
    @Index(name = "idx_transaccion_usuario", columnList = "usuario_id"),
    @Index(name = "idx_transaccion_categoria", columnList = "categoria_id"),
    @Index(name = "idx_transaccion_fecha", columnList = "fecha"),
    @Index(name = "idx_transaccion_tipo", columnList = "tipo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "tipo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoTransaccionEnum tipo;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "proyeccion_id")
    private Long proyeccionId;

    @Column(name = "es_automatica", nullable = false)
    private Boolean esAutomatica = false;

    // ==================== LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (esAutomatica == null) {
            esAutomatica = false;
        }
    }

    // ==================== ENUMS ====================

    public enum TipoTransaccionEnum {
        INGRESO,
        EGRESO
    }
}