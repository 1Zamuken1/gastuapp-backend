package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity: UsuarioEntity
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: UsuarioRepositoryAdapter (convierte Domain → Entity)
 * - ENVÍA DATOS A: PostgreSQL (tabla 'usuarios')
 * - CONVERTIDO DESDE: Usuario (Domain) via UsuarioEntityMapper
 * - CONVERTIDO HACIA: Usuario (Domain) via UsuarioEntityMapper
 *
 * RESPONSABILIDAD:
 * Mapea el modelo Usuario a la tabla 'usuarios' en PostgreSQL.
 * Contiene SOLO anotaciones JPA y estructura de BD.
 * NO contiene lógica de negocio.
 *
 * ESTRATEGIA DE IDs:
 * - id (BIGINT): ID interno auto-incremental para relaciones en BD
 * - publicId (UUID): ID público para APIs, URLs y respuestas JSON
 *
 * SEGURIDAD:
 * - NUNCA exponer 'id' en APIs públicas (predecible, enumerable)
 * - SIEMPRE usar 'publicId' en endpoints REST
 * - El 'id' SOLO se usa para joins y FKs internas
 *
 * TABLA EN BD:
 * CREATE TABLE usuarios (
 *   id BIGSERIAL PRIMARY KEY,
 *   public_id VARCHAR(36) UNIQUE NOT NULL,
 *   nombre VARCHAR(100) NOT NULL,
 *   apellido VARCHAR(100) NOT NULL,
 *   email VARCHAR(255) UNIQUE NOT NULL,
 *   telefono VARCHAR(20),
 *   password VARCHAR(255) NOT NULL,
 *   rol VARCHAR(20) NOT NULL,
 *   activo BOOLEAN DEFAULT true,
 *   fecha_creacion TIMESTAMP NOT NULL,
 *   tipologia VARCHAR(20),
 *   profesion VARCHAR(255),
 *   institucion VARCHAR(255),
 *   tutor_id BIGINT REFERENCES usuarios(id),
 *   google_id VARCHAR(255)
 * );
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_usuario_email", columnList = "email", unique = true),
        @Index(name = "idx_usuario_telefono", columnList = "telefono"),
        @Index(name = "idx_usuario_tutor", columnList = "tutor_id"),
        @Index(name = "idx_usuario_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

    // ==================== PRIMARY KEY (INTERNO) ====================

    /**
     * ID interno para relaciones de BD y FKs.
     * Auto-incremental (BIGSERIAL en PostgreSQL).
     *
     * ⚠️ NUNCA exponer este campo en APIs públicas.
     * Usar 'publicId' en su lugar para seguridad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== PUBLIC ID (EXTERNO) ====================

    /**
     * ID público para APIs externas y URLs.
     * Generado automáticamente como UUID v4.
     *
     * USOS:
     * - Endpoints REST: GET /api/usuarios/{publicId}
     * - JSON responses: { "id": "550e8400-e29b-41d4-a716-..." }
     * - URLs del frontend
     * - Compartir recursos entre usuarios
     *
     * VENTAJAS:
     * - No predecible (seguro contra enumeración)
     * - No expone cantidad de usuarios en el sistema
     * - Único globalmente (útil para integraciones futuras)
     *
     * Ejemplo: "550e8400-e29b-41d4-a716-446655440000"
     */
    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    // ==================== DATOS BÁSICOS ====================

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // BCrypt hash

    // ==================== ROLES Y PERMISOS ====================

    /**
     * Rol del usuario en el sistema.
     * Almacenado como String en BD: "ADMIN", "USER", "USER_HIJO"
     */
    @Column(name = "rol", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RolUsuarioEnum rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // ==================== INFORMACIÓN DEMOGRÁFICA ====================

    /**
     * Tipología del usuario (información demográfica).
     * Almacenado como String en BD: "ESTUDIANTE", "TRABAJADOR", "INDEPENDIENTE", "OTRO"
     */
    @Column(name = "tipologia", length = 20)
    @Enumerated(EnumType.STRING)
    private TipologiaUsuarioEnum tipologia;

    @Column(name = "profesion", length = 255)
    private String profesion;

    @Column(name = "institucion", length = 255)
    private String institucion;

    // ==================== RELACIÓN PADRE-HIJO ====================

    /**
     * FK a Usuario padre/tutor (id interno, no publicId).
     * Solo USER_HIJO tiene este campo lleno.
     *
     * NOTA: Usamos @Column en lugar de @ManyToOne para evitar carga innecesaria.
     * El Domain maneja esta relación a través del ID.
     */
    @Column(name = "tutor_id")
    private Long tutorId;

    // ==================== OAUTH (FUTURO) ====================

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    // ==================== LIFECYCLE CALLBACKS ====================

    /**
     * Se ejecuta ANTES de persistir por primera vez (INSERT).
     * Inicializa campos automáticos:
     * - publicId: genera UUID v4 si no existe
     * - fechaCreacion: timestamp actual
     * - activo: true por defecto
     */
    @PrePersist
    protected void onCreate() {
        if (publicId == null || publicId.trim().isEmpty()) {
            publicId = UUID.randomUUID().toString();
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }

    // ==================== ENUMS INTERNOS ====================

    /**
     * Enum interno para mapear RolUsuario de Domain.
     * Se guarda como String en BD para legibilidad.
     */
    public enum RolUsuarioEnum {
        ADMIN,
        USER,
        USER_HIJO
    }

    /**
     * Enum interno para mapear TipologiaUsuario de Domain.
     * Se guarda como String en BD para legibilidad.
     */
    public enum TipologiaUsuarioEnum {
        ESTUDIANTE,
        TRABAJADOR,
        INDEPENDIENTE,
        OTRO
    }
}