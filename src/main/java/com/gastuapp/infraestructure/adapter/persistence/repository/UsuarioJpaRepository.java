package com.gastuapp.infraestructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository: UsuarioJpaRepository
 *
 * FLUJO DE DATOS:
 * - USADO POR: UsuarioRepositoryAdapter (Infrastructure Layer)
 * - ACCEDE A: PostgreSQL tabla 'usuarios'
 * - RETORNA: UsuarioEntity (JPA entities)
 *
 * RESPONSABILIDAD:
 * Interface de Spring Data JPA para operaciones de persistencia.
 * Spring genera AUTOMÁTICAMENTE la implementación de estos métodos.
 * NO requiere código de implementación manual.
 *
 * CONVENCIONES DE SPRING DATA JPA:
 * - findBy[Campo]: genera SELECT WHERE campo = ?
 * - existsBy[Campo]: genera SELECT COUNT WHERE campo = ?
 * - deleteBy[Campo]: genera DELETE WHERE campo = ?
 *
 * QUERIES GENERADAS AUTOMÁTICAMENTE:
 * - save() → INSERT o UPDATE
 * - findById() → SELECT WHERE id = ?
 * - findByEmail() → SELECT WHERE email = ?
 * - existsByEmail() → SELECT COUNT WHERE email = ?
 *
 * NOTA IMPORTANTE:
 * Esta interfaz trabaja con 'id' interno (Long), no con 'publicId'.
 * El RepositoryAdapter se encarga de convertir publicId → id cuando sea necesario.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    // ============ Identificadores ============
    /**
     * Busca un usuario por su publicId (UUID).
     * Este método SÍ usa el ID público para búsquedas desde APIs.
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE public_id = ?
     *
     * @param publicId UUID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<UsuarioEntity> findByPublicId(String publicId);

    /**
     * Busca un usuario por su email.
     * Usado para login y validación de duplicados.
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE email = ?
     *
     * @param email Email del usuario (único)
     * @return Optional con el usuario si existe
     */
    Optional<UsuarioEntity> findByEmail(String email);

    /**
     * Busca un usuario por su teléfono.
     * Usado para login alternativo (si se implementa).
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE telefono = ?
     *
     * @param telefono Teléfono del usuario
     * @return Optional con el usuario si existe
     */
    Optional<UsuarioEntity> findByTelefono(String telefono);

    // ============ Existencia ============
    /**
     * Verifica si existe un usuario con ese email.
     * Usado para validar duplicados antes de registrar.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existByEmail(String email);

    /**
     * Verifica si existe un usuario con ese teléfono.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM usuarios WHERE telefono = ?
     *
     * @param telefono Teléfono a verificar
     * @return true si existe, false si no
     */
    boolean existByTelefono(String telefono);

    /**
     * Verifica si existe un usuario con ese publicId.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM usuarios WHERE public_id = ?
     *
     * @param publicId UUID a verificar
     * @return true si existe, false si no
     */
    boolean existByPublicId(String publicId);

    // ============ Relaciones ============
    /**
     * Busca todos los usuarios hijos de un tutor.
     * Usado para que un padre (USER) vea la lista de sus hijos supervisados.
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE tutor_id = ?
     *
     * @param tutorId ID interno del tutor
     * @return Lista de usuarios hijos
     */
    List<UsuarioEntity> findByTutorId(Long tutorId);

    // ============ Estado ============
    /**
     * Lista todos los usuarios activos.
     * Usado por ADMIN para gestión de usuarios.
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE activo = true
     *
     * @return Lista de usuarios activos
     */
    List<UsuarioEntity> findByActivoTrue();

    /**
     * Lista todos los usuarios activos ordenados por fecha de creación (más recientes primero).
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE activo = true ORDER BY fecha_creacion DESC
     *
     * @return Lista de usuarios activos ordenados
     */
    List<UsuarioEntity> findByActivoTrueOrderByFechaCreacionDesc();

    // ============ Rol ============
    /**
     * Busca todos los usuarios con un rol específico.
     *
     * Query generada:
     * SELECT * FROM usuarios WHERE rol = ?
     *
     * @param rol Rol a buscar (ADMIN, USER, USER_HIJO)
     * @return Lista de usuarios con ese rol
     */
    List<UsuarioEntity> findByRol(UsuarioEntity.RolUsuarioEnum rol);

    /**
     * Cuenta cuántos usuarios hay con un rol específico.
     *
     * Query generada:
     * SELECT COUNT(*) FROM usuarios WHERE rol = ?
     *
     * @param rol Rol a contar
     * @return Cantidad de usuarios con ese rol
     */
    long countByRol(UsuarioEntity.RolUsuarioEnum rol);

    // ==================== CUSTOM QUERIES (JPQL) ====================

    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial, case-insensitive).
     * Usado para funcionalidad de búsqueda en el frontend.
     *
     * Query JPQL personalizada con LIKE.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UsuarioEntity> searchByNombreOrApellido(@Param("searchTerm") String searchTerm);

    /**
     * Busca usuarios activos de un tutor específico.
     * Combinación de filtros para padres viendo sus hijos activos.
     *
     * @param tutorId ID del tutor
     * @return Lista de usuarios hijos activos
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.tutorId = :tutorId AND u.activo = true")
    List<UsuarioEntity> findActivosByTutorId(@Param("tutorId") Long tutorId);

}
