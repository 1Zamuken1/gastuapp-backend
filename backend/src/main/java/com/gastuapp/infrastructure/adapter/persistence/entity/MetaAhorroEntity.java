package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'metas_ahorro'.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Entity
@Table(name = "metas_ahorro")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaAhorroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "monto_objetivo", nullable = false)
    private BigDecimal montoObjetivo;

    @Column(name = "monto_actual")
    private BigDecimal montoActual;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(length = 20)
    private String frecuencia; // Se mapea desde FrecuenciaAhorro

    private String color;
    private String icono;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoMetaEnum estado;

    public enum EstadoMetaEnum {
        ACTIVA,
        COMPLETADA,
        PAUSADA,
        CANCELADA
    }

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (montoActual == null) {
            montoActual = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = EstadoMetaEnum.ACTIVA;
        }
    }
}
