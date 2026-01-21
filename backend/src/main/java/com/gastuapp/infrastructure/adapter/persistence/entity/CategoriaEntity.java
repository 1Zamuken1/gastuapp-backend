package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity: CategoriaEntity
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: CategoriaRepositoryAdapter
 * - ENVÍA DATOS A: PostgreSQL (tabla 'categorias')
 *
 * RESPONSABILIDAD:
 * Mapea el modelo Categoria a la tabla 'categorias' en PostgreSQL.
 *
 * TABLA EN BD:
 * CREATE TABLE categorias (
 * id BIGSERIAL PRIMARY KEY,
 * nombre VARCHAR(50) NOT NULL,
 * icono VARCHAR(50),
 * tipo VARCHAR(20) NOT NULL,
 * predefinida BOOLEAN DEFAULT false,
 * usuario_id BIGINT REFERENCES usuarios(id)
 * );
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Entity
@Table(name = "categorias", indexes = {
        @Index(name = "idx_categoria_predefinida", columnList = "predefinida"),
        @Index(name = "idx_categoria_usuario", columnList = "usuario_id"),
        @Index(name = "idx_categoria_tipo", columnList = "tipo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "tipo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoCategoriaEnum tipo;

    @Column(name = "predefinida", nullable = false)
    private Boolean predefinida = false;

    @Column(name = "usuario_id")
    private Long usuarioId;

    // ============ Enums ============
    public enum TipoCategoriaEnum {
        INGRESO,
        EGRESO,
        AMBOS
    }
}
