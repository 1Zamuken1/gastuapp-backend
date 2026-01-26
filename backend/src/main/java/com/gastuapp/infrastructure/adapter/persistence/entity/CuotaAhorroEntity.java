package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para la tabla 'cuotas_ahorro'.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
@Entity
@Table(name = "cuotas_ahorro")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaAhorroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meta_ahorro_id", nullable = false)
    private Long metaAhorroId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "monto_esperado", nullable = false)
    private BigDecimal montoEsperado;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoCuotaEnum estado;

    @Column(name = "ahorro_id")
    private Long ahorroId; // ID del abono real si se pagó

    public enum EstadoCuotaEnum {
        PENDIENTE,
        PAGADA,
        VENCIDA,
        CANCELADA
    }
}
