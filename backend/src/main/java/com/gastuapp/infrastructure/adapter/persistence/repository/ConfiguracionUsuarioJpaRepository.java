package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.ConfiguracionUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository: ConfiguracionUsuarioJpaRepository
 *
 * FLUJO DE DATOS:
 * - USADO POR: ConfiguracionRepositoryAdapter (Infrastructure Layer)
 * - ACCEDE A: PostgreSQL tabla 'configuracion_usuario'
 * - RETORNA: ConfiguracionUsuarioEntity (JPA entities)
 *
 * RESPONSABILIDAD:
 * Interface de Spring Data JPA para operaciones de configuración de usuario.
 * Relación OneToOne con UsuarioEntity.
 * Spring genera AUTOMÁTICAMENTE la implementación.
 *
 * RELACIÓN:
 * Cada usuario tiene UNA configuración única.
 * La configuración se crea automáticamente al registrar un usuario.
 * Si se elimina el usuario, se elimina su configuración (CASCADE).
 *
 * QUERIES GENERADAS AUTOMÁTICAMENTE:
 * - save() → INSERT o UPDATE configuracion_usuario
 * - findByUsuarioId() → SELECT WHERE usuario_id = ?
 * - existsByUsuarioId() → SELECT COUNT WHERE usuario_id = ?
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Repository
public interface ConfiguracionUsuarioJpaRepository extends JpaRepository<ConfiguracionUsuarioEntity, Long> {
    // ==================== POR USUARIO ====================

    /**
     * Busca la configuración de un usuario por su ID interno.
     *
     * Query generada:
     * SELECT * FROM configuracion_usuario WHERE usuario_id = ?
     *
     * IMPORTANTE: Usa el ID interno del usuario, no el publicId.
     * El adapter se encarga de resolver publicId → id antes de llamar esto.
     *
     * @param usuarioId ID interno del usuario
     * @return Optional con la configuración si existe
     */
    Optional<ConfiguracionUsuarioEntity> findByUsuarioId(Long usuarioId);

    /**
     * Verifica si existe una configuración para un usuario.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM configuracion_usuario WHERE usuario_id = ?
     *
     * @param usuarioId ID interno del usuario
     * @return true si existe, false si no
     */
    boolean existsByUsuarioId(Long usuarioId);

    /**
     * Elimina la configuración de un usuario.
     *
     * Query generada:
     * DELETE FROM configuracion_usuario WHERE usuario_id = ?
     *
     * NOTA: Normalmente no se usa porque el CASCADE se encarga de esto
     * cuando se elimina el usuario, pero está disponible por si acaso.
     *
     * @param usuarioId ID interno del usuario
     */
    void deleteByUsuarioId(Long usuarioId);
}
