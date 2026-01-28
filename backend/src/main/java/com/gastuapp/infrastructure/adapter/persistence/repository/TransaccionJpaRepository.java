package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository: TransaccionJpaRepository
 *
 * FLUJO DE DATOS:
 * - USADO POR: TransaccionRepositoryAdapter (Infrastructure Layer)
 * - ACCEDE A: PostgreSQL tabla 'transacciones'
 *
 * RESPONSABILIDAD:
 * Interface de Spring Data JPA para operaciones de persistencia de
 * transacciones.
 * Spring genera automáticamente la implementación.
 *
 * QUERIES GENERADAS AUTOMÁTICAMENTE:
 * - save() → INSERT o UPDATE
 * - findById() → SELECT WHERE id = ?
 * - findByUsuarioId() → SELECT WHERE usuario_id = ?
 * - deleteById() → DELETE WHERE id = ?
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Repository
public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {

    // ==================== BÚSQUEDAS POR USUARIO ====================

    /**
     * Lista todas las transacciones de un usuario.
     *
     * Query generada:
     * SELECT * FROM transacciones WHERE usuario_id = ?
     *
     * @param usuarioId ID del usuario
     * @return Lista de transacciones
     */
    List<TransaccionEntity> findByUsuarioId(Long usuarioId);

    /**
     * Lista transacciones de un usuario ordenadas por fecha descendente.
     *
     * Query generada:
     * SELECT * FROM transacciones WHERE usuario_id = ? ORDER BY fecha DESC
     *
     * @param usuarioId ID del usuario
     * @return Lista de transacciones ordenadas
     */
    List<TransaccionEntity> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // ==================== BÚSQUEDAS POR TIPO ====================

    /**
     * Lista transacciones de un usuario por tipo.
     *
     * Query generada:
     * SELECT * FROM transacciones WHERE usuario_id = ? AND tipo = ?
     *
     * @param usuarioId ID del usuario
     * @param tipo      INGRESO o EGRESO
     * @return Lista de transacciones del tipo
     */
    List<TransaccionEntity> findByUsuarioIdAndTipo(
            Long usuarioId,
            TransaccionEntity.TipoTransaccionEnum tipo);

    // ==================== BÚSQUEDAS POR CATEGORÍA ====================

    /**
     * Lista transacciones de un usuario por categoría.
     *
     * Query generada:
     * SELECT * FROM transacciones WHERE usuario_id = ? AND categoria_id = ?
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @return Lista de transacciones de la categoría
     */
    List<TransaccionEntity> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);

    // ==================== BÚSQUEDAS POR FECHA ====================

    /**
     * Lista transacciones de un usuario en un rango de fechas.
     *
     * Query generada:
     * SELECT * FROM transacciones
     * WHERE usuario_id = ? AND fecha BETWEEN ? AND ?
     *
     * @param usuarioId   ID del usuario
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin    Fecha final (inclusive)
     * @return Lista de transacciones en el rango
     */
    List<TransaccionEntity> findByUsuarioIdAndFechaBetween(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    /**
     * Lista transacciones de un usuario desde una fecha.
     *
     * Query generada:
     * SELECT * FROM transacciones WHERE usuario_id = ? AND fecha >= ?
     *
     * @param usuarioId ID del usuario
     * @param fecha     Fecha inicial
     * @return Lista de transacciones desde la fecha
     */
    List<TransaccionEntity> findByUsuarioIdAndFechaGreaterThanEqual(
            Long usuarioId,
            LocalDate fecha);

    // ==================== AGREGACIONES (QUERIES CUSTOM) ====================

    /**
     * Calcula la suma de montos por usuario y tipo.
     * Usado para calcular total de ingresos o egresos.
     *
     * Query JPQL personalizada.
     *
     * @param usuarioId ID del usuario
     * @param tipo      INGRESO o EGRESO
     * @return Suma de montos (null si no hay transacciones)
     */
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM TransaccionEntity t " +
            "WHERE t.usuarioId = :usuarioId AND t.tipo = :tipo")
    BigDecimal sumByUsuarioIdAndTipo(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TransaccionEntity.TipoTransaccionEnum tipo);

    /**
     * Calcula el balance de un usuario.
     * Balance = Total Ingresos - Total Egresos
     *
     * Query JPQL personalizada que suma con signo según tipo.
     *
     * @param usuarioId ID del usuario
     * @return Balance del usuario
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipo = 'INGRESO' THEN t.monto ELSE -t.monto END), 0) " +
            "FROM TransaccionEntity t WHERE t.usuarioId = :usuarioId")
    BigDecimal calcularBalance(@Param("usuarioId") Long usuarioId);

    /**
     * Calcula el balance de un usuario en un rango de fechas.
     *
     * @param usuarioId   ID del usuario
     * @param fechaInicio Fecha inicial
     * @param fechaFin    Fecha final
     * @return Balance del usuario en el período
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipo = 'INGRESO' THEN t.monto ELSE -t.monto END), 0) " +
            "FROM TransaccionEntity t " +
            "WHERE t.usuarioId = :usuarioId AND t.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularBalanceEnPeriodo(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // ==================== CONTADORES ====================

    /**
     * Cuenta las transacciones de un usuario.
     *
     * Query generada:
     * SELECT COUNT(*) FROM transacciones WHERE usuario_id = ?
     *
     * @param usuarioId ID del usuario
     * @return Cantidad de transacciones
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Cuenta transacciones de un usuario por tipo.
     *
     * @param usuarioId ID del usuario
     * @param tipo      INGRESO o EGRESO
     * @return Cantidad de transacciones del tipo
     */
    long countByUsuarioIdAndTipo(Long usuarioId, TransaccionEntity.TipoTransaccionEnum tipo);

    // ==================== BÚSQUEDAS AVANZADAS ====================

    /**
     * Lista las últimas N transacciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de las últimas 10 transacciones
     */
    @Query("SELECT t FROM TransaccionEntity t " +
            "WHERE t.usuarioId = :usuarioId " +
            "ORDER BY t.fechaCreacion DESC " +
            "LIMIT 10")
    List<TransaccionEntity> findTop10ByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Long usuarioId);

    /**
     * Busca transacciones por descripción (búsqueda parcial).
     *
     * @param usuarioId  ID del usuario
     * @param searchTerm Término de búsqueda
     * @return Lista de transacciones que coinciden
     */
    @Query("SELECT t FROM TransaccionEntity t " +
           "WHERE t.usuarioId = :usuarioId " +
           "AND LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TransaccionEntity> searchByDescripcion(
            @Param("usuarioId") Long usuarioId,
            @Param("searchTerm") String searchTerm);

    /**
     * Lista transacciones por usuario, categoría y rango de fechas.
     * Usado para calcular montos gastados en presupuestos.
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @param fechaInicio Fecha inicio del período (inclusive)
     * @param fechaFin    Fecha fin del período (inclusive)
     * @return Lista de transacciones en el período y categoría
     */
    List<TransaccionEntity> findByUsuarioIdAndCategoriaIdAndFechaBetween(
            Long usuarioId,
            Long categoriaId,
            LocalDate fechaInicio,
            LocalDate fechaFin);
}