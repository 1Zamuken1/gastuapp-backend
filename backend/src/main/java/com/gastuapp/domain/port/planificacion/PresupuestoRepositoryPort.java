package com.gastuapp.domain.port.planificacion;

import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port: PresupuestoRepositoryPort
 *
 * FLUJO DE DATOS:
 * - USADO POR: PresupuestoService (Application Layer)
 * - IMPLEMENTADO POR: PresupuestoRepositoryAdapter (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Define el contrato para operaciones de persistencia de Presupuesto.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
public interface PresupuestoRepositoryPort {

    /**
     * Guarda un presupuesto (create o update).
     */
    Presupuesto save(Presupuesto presupuesto);

    /**
     * Busca un presupuesto por ID.
     */
    Optional<Presupuesto> findById(Long id);

    /**
     * Busca un presupuesto por public ID.
     */
    Optional<Presupuesto> findByPublicId(String publicId);

    /**
     * Lista todos los presupuestos de un usuario.
     */
    List<Presupuesto> findByUsuarioId(Long usuarioId);

    /**
     * Lista presupuestos activos de un usuario.
     */
    List<Presupuesto> findByUsuarioIdAndEstado(Long usuarioId, EstadoPresupuesto estado);

    /**
     * Busca presupuesto activo por usuario y categoría.
     * Solo debe existir uno por usuario+categoría en estado ACTIVA.
     */
    Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndEstado(Long usuarioId, Long categoriaId, EstadoPresupuesto estado);

    /**
     * Lista presupuestos vigentes de un usuario.
     * Vigente = ACTIVA y dentro del rango de fechas.
     */
    List<Presupuesto> findVigentesByUsuarioId(Long usuarioId);

    /**
     * Lista presupuestos cercanos a vencer (ej: próximos 7 días).
     */
    List<Presupuesto> findCercanosAVencer(Long usuarioId, LocalDate fecha);

    /**
     * Lista presupuestos excedidos de un usuario.
     */
    List<Presupuesto> findExcedidosByUsuarioId(Long usuarioId);

    /**
     * Lista presupuestos que necesitan ser renovados (vencidos con autoRenovar=true).
     */
    List<Presupuesto> findPendientesDeRenovacion(LocalDate fecha);

    /**
     * Busca presupuestos por usuario y categoría en un rango de fechas.
     * Útil para calcular montos gastados.
     */
    List<Presupuesto> findByUsuarioIdAndCategoriaIdAndFechas(
            Long usuarioId,
            Long categoriaId,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    /**
     * Lista presupuestos que están por excederse (ej: >80% de uso).
     */
    List<Presupuesto> findPorExceder(Long usuarioId, double porcentajeUmbral);

    /**
     * Elimina un presupuesto por ID.
     */
    void deleteById(Long id);

    /**
     * Cuenta presupuestos por usuario y estado.
     */
    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPresupuesto estado);

    /**
     * Verifica si existe un presupuesto activo para el usuario y categoría.
     */
    boolean existsByUsuarioIdAndCategoriaIdAndEstado(Long usuarioId, Long categoriaId, EstadoPresupuesto estado);

    /**
     * Lista presupuestos por frecuencia específica.
     */
    List<Presupuesto> findByUsuarioIdAndFrecuencia(Long usuarioId, String frecuencia);

    /**
     * Cuenta presupuestos por estado.
     *
     * @param estado Estado a contar
     * @return Cantidad de presupuestos con ese estado
     */
    long countByEstado(EstadoPresupuesto estado);

    /**
     * Lista presupuestos con fecha fin anterior a una fecha.
     *
     * @param fecha Fecha límite
     * @return Lista de presupuestos vencidos
     */
    List<Presupuesto> findByFechaFinBefore(LocalDate fecha);
}