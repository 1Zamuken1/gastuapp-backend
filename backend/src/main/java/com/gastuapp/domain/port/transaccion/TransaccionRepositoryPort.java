package com.gastuapp.domain.port.transaccion;

import com.gastuapp.domain.model.transaccion.Transaccion;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port: TransaccionRepositoryPort
 *
 * FLUJO DE DATOS:
 * - USADO POR: TransaccionService (Application Layer)
 * - IMPLEMENTADO POR: TransaccionRepositoryAdapter (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Define el contrato para operaciones de persistencia de Transaccion.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
public interface TransaccionRepositoryPort {

    /**
     * Guarda una transacción (create o update).
     */
    Transaccion save(Transaccion transaccion);

    /**
     * Busca una transacción por ID.
     */
    Optional<Transaccion> findById(Long id);

    /**
     * Lista todas las transacciones de un usuario.
     */
    List<Transaccion> findByUsuarioId(Long usuarioId);

    /**
     * Lista transacciones por usuario y tipo.
     */
    List<Transaccion> findByUsuarioIdAndTipo(Long usuarioId, TipoTransaccion tipo);

    /**
     * Lista transacciones por usuario y categoría.
     */
    List<Transaccion> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);

    /**
     * Lista transacciones por usuario y rango de fechas.
     */
    List<Transaccion> findByUsuarioIdAndFechaBetween(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    /**
     * Calcula la suma de transacciones por usuario y tipo.
     * Usado para cálculo de balance.
     */
    BigDecimal sumByUsuarioIdAndTipo(Long usuarioId, TipoTransaccion tipo);

    /**
     * Calcula el balance de un usuario.
     * Balance = Ingresos - Egresos
     */
    BigDecimal calcularBalance(Long usuarioId);

    /**
     * Elimina una transacción por ID.
     */
    void deleteById(Long id);

    /**
     * Cuenta transacciones por usuario.
     *
     * Query generada:
     * SELECT COUNT(*) FROM transacciones WHERE usuario_id = ?
     *
     * @param usuarioId ID del usuario
     * @return Cantidad de transacciones
     */
    long countByUsuarioId(Long usuarioId);

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
    List<Transaccion> findByUsuarioIdAndCategoriaIdAndFechaBetween(
            Long usuarioId, 
            Long categoriaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin);
}