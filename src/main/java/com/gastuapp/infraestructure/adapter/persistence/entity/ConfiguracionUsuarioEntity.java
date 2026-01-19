package com.gastuapp.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity: ConfiguracionUsuarioEntity
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: ConfiguracionRepositoryAdapter (convierte Domain → Entity)
 * - ENVÍA DATOS A: PostgreSQL (tabla 'configuracion_usuario')
 * - CONVERTIDO DESDE: ConfiguracionUsuario (Domain) via ConfiguracionEntityMapper
 * - CONVERTIDO HACIA: ConfiguracionUsuario (Domain) via ConfiguracionEntityMapper
 *
 * RESPONSABILIDAD:
 * Mapea el modelo ConfiguracionUsuario a la tabla 'configuracion_usuario' en PostgreSQL.
 * Almacena preferencias y configuración personalizada de cada usuario.
 *
 * RELACIÓN:
 * OneToOne con UsuarioEntity (cada usuario tiene una configuración única).
 * Usa el ID interno (usuario_id), no el publicId.
 *
 * TABLA EN BD:
 * CREATE TABLE configuracion_usuario (
 *   id BIGSERIAL PRIMARY KEY,
 *   usuario_id BIGINT UNIQUE NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
 *   notificaciones_activas BOOLEAN DEFAULT true,
 *   celebraciones_activas BOOLEAN DEFAULT true,
 *   onboarding_completado VARCHAR(20) DEFAULT 'NO_COMPLETADO',
 *   idioma_preferido VARCHAR(5) DEFAULT 'es',
 *   modo_oscuro BOOLEAN DEFAULT false
 * );
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Entity
@Table(name = "configuracion_usuario", indexes = {
        @Index(name = "idx_config_usuario", columnList = "usuario_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionUsuarioEntity {

    // ==================== PRIMARY KEY ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== FOREIGN KEY ====================

    /**
     * FK a Usuario (usa id interno, no publicId).
     * Relación OneToOne: cada usuario tiene UNA configuración.
     * ON DELETE CASCADE: si se elimina el usuario, se elimina su configuración.
     *
     * NOTA: Aunque UsuarioEntity tiene publicId para APIs,
     * las FKs internas usan el id (BIGINT) por performance.
     */
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    // ==================== PREFERENCIAS ====================

    @Column(name = "notificaciones_activas", nullable = false)
    private Boolean notificacionesActivas = true;

    /**
     * Activa/desactiva celebraciones (confetti, badges).
     * Parte del Calm Design Framework de GastuApp.
     */
    @Column(name = "celebraciones_activas", nullable = false)
    private Boolean celebracionesActivas = true;

    /**
     * Estado del onboarding del usuario.
     * Valores: NO_COMPLETADO, BASICO, COMPLETO
     */
    @Column(name = "onboarding_completado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoOnboardingEnum onboardingCompletado = TipoOnboardingEnum.NO_COMPLETADO;

    /**
     * Idioma preferido del usuario.
     * Valores soportados: "es" (español), "en" (inglés)
     */
    @Column(name = "idioma_preferido", nullable = false, length = 5)
    private String idiomaPreferido = "es";

    @Column(name = "modo_oscuro", nullable = false)
    private Boolean modoOscuro = false;

    // ==================== LIFECYCLE CALLBACKS ====================

    /**
     * Se ejecuta ANTES de persistir por primera vez (INSERT).
     * Inicializa valores por defecto si no están seteados.
     */
    @PrePersist
    protected void onCreate() {
        if (notificacionesActivas == null) {
            notificacionesActivas = true;
        }
        if (celebracionesActivas == null) {
            celebracionesActivas = true;
        }
        if (onboardingCompletado == null) {
            onboardingCompletado = TipoOnboardingEnum.NO_COMPLETADO;
        }
        if (idiomaPreferido == null || idiomaPreferido.trim().isEmpty()) {
            idiomaPreferido = "es";
        }
        if (modoOscuro == null) {
            modoOscuro = false;
        }
    }

    // ==================== ENUMS INTERNOS ====================

    /**
     * Enum interno para mapear TipoOnboarding de Domain.
     * Se guarda como String en BD para legibilidad.
     */
    public enum TipoOnboardingEnum {
        NO_COMPLETADO,
        BASICO,
        COMPLETO
    }
}