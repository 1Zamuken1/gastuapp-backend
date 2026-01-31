package com.gastuapp.infrastructure.adapter.persistence.entity;

import com.gastuapp.domain.model.proyeccion.Frecuencia;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity: ProyeccionEntity
 *
 * RESPONSABILIDAD:
 * Representación JPA de la tabla 'proyecciones'.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@Entity
@Table(name = "proyecciones")
public class ProyeccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo; // INGRESO, EGRESO

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frecuencia frecuencia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "ultima_ejecucion")
    private LocalDate ultimaEjecucion;

    @Column(nullable = false)
    private Boolean activo = true;
}
