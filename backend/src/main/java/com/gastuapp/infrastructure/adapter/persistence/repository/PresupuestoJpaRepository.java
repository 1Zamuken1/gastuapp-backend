package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.PresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository: PresupuestoJpaRepository
 *
 * FLUJO DE DATOS:
 * - USADO POR: PresupuestoRepositoryAdapter (Infrastructure Layer)
 * - ACCEDE A: PostgreSQL tabla 'presupuestos_planificaciones'
 *
 * RESPONSABILIDAD:
 * Interface de Spring Data JPA para operaciones de persistencia de
 * presupuestos.
 * Spring genera automáticamente la implementación.
 *
 * QUERIES GENERADAS AUTOMÁTICAMENTE:
 * - save() → INSERT o UPDATE
 * - findById() → SELECT WHERE id = ?
 * - deleteById() → DELETE WHERE id = ?
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Repository
public interface PresupuestoJpaRepository extends JpaRepository<PresupuestoEntity, Long> {

    // ==================== BÚSQUEDAS POR USUARIO ====================

    /**
     * Lista todos los presupuestos de un usuario.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones WHERE usuario_id = ?
     *
     * @param usuarioId ID del usuario
     * @return Lista de presupuestos
     */
    List<PresupuestoEntity> findByUsuarioId(Long usuarioId);

    // ==================== BÚSQUEDAS POR ESTADO ====================

    /**
     * Lista presupuestos de un usuario por estado.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones WHERE usuario_id = ? AND estado = ?
     *
     * @param usuarioId ID del usuario
     * @param estado   ACTIVA, INACTIVA o EXCEDIDA
     * @return Lista de presupuestos del estado
     */
    List<PresupuestoEntity> findByUsuarioIdAndEstado(
            Long usuarioId, 
            PresupuestoEntity.EstadoPresupuestoEnum estado);

    // ==================== BÚSQUEDAS POR USUARIO + CATEGORÍA ====================

    /**
     * Busca presupuesto activo por usuario y categoría.
     * Solo debe existir uno por usuario+categoría en estado ACTIVA.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones 
     * WHERE usuario_id = ? AND categoria_id = ? AND estado = ?
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @param estado      Estado del presupuesto
     * @return Presupuesto encontrado o null
     */
    PresupuestoEntity findByUsuarioIdAndCategoriaIdAndEstado(
            Long usuarioId,
            Long categoriaId,
            PresupuestoEntity.EstadoPresupuestoEnum estado);

    /**
     * Lista presupuestos de un usuario por categoría.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones 
     * WHERE usuario_id = ? AND categoria_id = ?
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @return Lista de presupuestos de la categoría
     */
    List<PresupuestoEntity> findByUsuarioIdAndCategoriaId(
            Long usuarioId, 
            Long categoriaId);

    // ==================== BÚSQUEDAS POR FRECUENCIA ====================

    /**
     * Lista presupuestos de un usuario por frecuencia.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones 
     * WHERE usuario_id = ? AND frecuencia = ?
     *
     * @param usuarioId  ID del usuario
     * @param frecuencia MENSUAL, SEMANAL, etc.
     * @return Lista de presupuestos de la frecuencia
     */
    List<PresupuestoEntity> findByUsuarioIdAndFrecuencia(
            Long usuarioId,
            PresupuestoEntity.FrecuenciaPresupuestoEnum frecuencia);

    // ==================== BÚSQUEDAS POR FECHAS ====================

    /**
     * Lista presupuestos en un rango de fechas.
     *
     * Query generada:
     * SELECT * FROM presupuestos_planificaciones 
     * WHERE fecha_fin >= ? AND fecha_fin <= ?
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin    Fecha final
     * @return Lista de presupuestos en el rango
     */
    List<PresupuestoEntity> findByFechaFinBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Lista presupuestos que vencen después de una fecha.
     *
     * @param fecha Fecha de referencia
     * @return Lista de presupuestos que vencen después
     */
    List<PresupuestoEntity> findByFechaFinGreaterThanEqual(LocalDate fecha);

    // ==================== QUERIES PERSONALIZADAS (JPQL) ====================

    /**
     * Lista presupuestos vigentes de un usuario.
     * Vigente = estado ACTIVA y fecha actual dentro del rango.
     *
     * @param usuarioId ID del usuario
     * @return Lista de presupuestos vigentes
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.estado = 'ACTIVA' " +
           "AND CURRENT_DATE BETWEEN p.fechaInicio AND p.fechaFin")
    List<PresupuestoEntity> findVigentesByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista presupuestos cercanos a vencer (ej: próximos 7 días).
     *
     * @param usuarioId ID del usuario
     * @param fecha     Fecha de referencia
     * @return Lista de presupuestos cercanos a vencer
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.estado = 'ACTIVA' " +
           "AND p.fechaFin BETWEEN :fecha AND :fechaPlus7Days")
    List<PresupuestoEntity> findCercanosAVencer(
            @Param("usuarioId") Long usuarioId,
            @Param("fecha") LocalDate fecha,
            @Param("fechaPlus7Days") LocalDate fechaPlus7Days);

    /**
     * Lista presupuestos excedidos de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de presupuestos excedidos
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.estado = 'EXCEDIDA'")
    List<PresupuestoEntity> findExcedidosByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista presupuestos que necesitan ser renovados.
     * Vencidos con autoRenovar = true.
     *
     * @param fecha Fecha actual
     * @return Lista de presupuestos pendientes de renovación
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.autoRenovar = true " +
           "AND p.fechaFin < :fecha " +
           "AND p.estado IN ('ACTIVA', 'EXCEDIDA')")
    List<PresupuestoEntity> findPendientesDeRenovacion(@Param("fecha") LocalDate fecha);

    /**
     * Lista presupuestos que están por excederse (ej: >80% de uso).
     *
     * @param usuarioId         ID del usuario
     * @param porcentajeUmbral Porcentaje umbral (ej: 80.0 para 80%)
     * @return Lista de presupuestos por exceder
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.estado = 'ACTIVA' " +
           "AND (p.montoGastado / p.montoTope * 100) >= :porcentajeUmbral")
    List<PresupuestoEntity> findPorExceder(
            @Param("usuarioId") Long usuarioId,
            @Param("porcentajeUmbral") Double porcentajeUmbral);

    /**
     * Busca presupuestos por usuario y categoría en un rango de fechas.
     * Útil para calcular montos gastados.
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @param fechaInicio Fecha inicio del período
     * @param fechaFin    Fecha fin del período
     * @return Lista de presupuestos en el período
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.categoriaId = :categoriaId " +
           "AND p.fechaInicio <= :fechaFin " +
           "AND p.fechaFin >= :fechaInicio")
    List<PresupuestoEntity> findByUsuarioIdAndCategoriaIdAndFechas(
            @Param("usuarioId") Long usuarioId,
            @Param("categoriaId") Long categoriaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca presupuesto por public ID.
     *
     * @param publicId UUID público
     * @return Presupuesto encontrado
     */
    PresupuestoEntity findByPublicId(String publicId);

    // ==================== CONTADORES ====================

    /**
     * Cuenta presupuestos de un usuario por estado.
     *
     * Query generada:
     * SELECT COUNT(*) FROM presupuestos_planificaciones 
     * WHERE usuario_id = ? AND estado = ?
     *
     * @param usuarioId ID del usuario
     * @param estado   Estado del presupuesto
     * @return Cantidad de presupuestos del estado
     */
    long countByUsuarioIdAndEstado(
            Long usuarioId,
            PresupuestoEntity.EstadoPresupuestoEnum estado);

    /**
     * Verifica si existe presupuesto activo para usuario y categoría.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM presupuestos_planificaciones 
     * WHERE usuario_id = ? AND categoria_id = ? AND estado = ?
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @param estado      Estado a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsuarioIdAndCategoriaIdAndEstado(
            Long usuarioId,
            Long categoriaId,
            PresupuestoEntity.EstadoPresupuestoEnum estado);

    // ==================== BÚSQUEDAS AVANZADAS ====================

    /**
     * Lista presupuestos con mayor porcentaje de uso.
     *
     * @param usuarioId ID del usuario
     * @param limite    Número máximo de resultados
     * @return Lista de presupuestos ordenados por porcentaje de uso descendente
     */
    @Query("SELECT p FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId " +
           "AND p.estado = 'ACTIVA' " +
           "AND p.montoTope > 0 " +
           "ORDER BY (p.montoGastado / p.montoTope) DESC")
    List<PresupuestoEntity> findTopPorPorcentajeUso(
            @Param("usuarioId") Long usuarioId);

    /**
     * Calcula el total de montos tope de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Suma de todos los montos tope del usuario
     */
    @Query("SELECT COALESCE(SUM(p.montoTope), 0) FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId AND p.estado = 'ACTIVA'")
    BigDecimal sumMontosTopeByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Calcula el total de montos gastados de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Suma de todos los montos gastados del usuario
     */
    @Query("SELECT COALESCE(SUM(p.montoGastado), 0) FROM PresupuestoEntity p " +
           "WHERE p.usuarioId = :usuarioId AND p.estado = 'ACTIVA'")
    BigDecimal sumMontosGastadosByUsuarioId(@Param("usuarioId") Long usuarioId);

    // ==================== MÉTODOS PARA SCHEDULER ====================

    /**
     * Cuenta presupuestos por estado.
     *
     * @param estado Estado del presupuesto
     * @return Cantidad de presupuestos con ese estado
     */
    long countByEstado(PresupuestoEntity.EstadoPresupuestoEnum estado);

    /**
     * Lista presupuestos con fecha fin anterior a una fecha.
     *
     * @param fecha Fecha límite
     * @return Lista de presupuestos vencidos
     */
    List<PresupuestoEntity> findByFechaFinBefore(LocalDate fecha);
}