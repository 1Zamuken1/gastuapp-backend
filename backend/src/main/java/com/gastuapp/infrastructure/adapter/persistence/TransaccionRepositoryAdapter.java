package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import com.gastuapp.domain.model.transaccion.Transaccion;
import com.gastuapp.domain.port.transaccion.TransaccionRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.TransaccionEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.TransaccionEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.TransaccionJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter: TransaccionRepositoryAdapter
 *
 * FLUJO DE DATOS:
 * - IMPLEMENTA: TransaccionRepositoryPort (Domain Layer)
 * - RECIBE DATOS DE: TransaccionService (Application Layer) a través del Port
 * - USA: TransaccionJpaRepository (Spring Data JPA)
 * - USA: TransaccionEntityMapper (para convertir Domain ↔ Entity)
 * - ENVÍA DATOS A: PostgreSQL (a través de JpaRepository)
 *
 * RESPONSABILIDAD:
 * Implementación concreta del TransaccionRepositoryPort.
 * Conecta el Domain con la infraestructura de persistencia (JPA/PostgreSQL).
 * Convierte entre modelos de Domain y entidades de BD.
 *
 * ARQUITECTURA HEXAGONAL:
 * Este es un "Adapter de salida" (Output Adapter) que implementa un Port del
 * Domain.
 * Permite que el Domain persista datos SIN conocer detalles de JPA o
 * PostgreSQL.
 *
 * CONVERSIONES:
 * - Domain (Transaccion) → Entity (TransaccionEntity) → PostgreSQL
 * - PostgreSQL → Entity (TransaccionEntity) → Domain (Transaccion)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final TransaccionJpaRepository jpaRepository;
    private final TransaccionEntityMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente el repository y el mapper.
     */
    public TransaccionRepositoryAdapter(
            TransaccionJpaRepository jpaRepository,
            TransaccionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    // ==================== OPERACIONES CRUD ====================

    /**
     * Guarda una transacción (create o update).
     *
     * FLUJO:
     * Transaccion (Domain) → TransaccionEntity → JpaRepository.save() → PostgreSQL
     * PostgreSQL → TransaccionEntity (con id generado) → Transaccion (Domain)
     *
     * Si la transacción tiene id null, es un INSERT (JPA genera el id).
     * Si la transacción tiene id, es un UPDATE.
     *
     * @param transaccion Transaccion del Domain
     * @return Transaccion guardada con id asignado
     */
    @Override
    public Transaccion save(Transaccion transaccion) {
        // 1. Convertir Domain → Entity
        TransaccionEntity entity = mapper.toEntity(transaccion);

        // 2. Guardar en BD (JPA genera id si es nuevo)
        TransaccionEntity savedEntity = jpaRepository.save(entity);

        // 3. Convertir Entity → Domain y retornar
        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca una transacción por su ID.
     *
     * @param id ID de la transacción
     * @return Optional con la transacción si existe
     */
    @Override
    public Optional<Transaccion> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    // ==================== BÚSQUEDAS POR USUARIO ====================

    /**
     * Lista todas las transacciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de transacciones
     */
    @Override
    public List<Transaccion> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR TIPO ====================

    /**
     * Lista transacciones de un usuario por tipo.
     *
     * @param usuarioId ID del usuario
     * @param tipo      INGRESO o EGRESO
     * @return Lista de transacciones del tipo
     */
    @Override
    public List<Transaccion> findByUsuarioIdAndTipo(Long usuarioId, TipoTransaccion tipo) {
        TransaccionEntity.TipoTransaccionEnum tipoEnum = mapTipoToEntity(tipo);
        return jpaRepository.findByUsuarioIdAndTipo(usuarioId, tipoEnum).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR CATEGORÍA ====================

    /**
     * Lista transacciones de un usuario por categoría.
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @return Lista de transacciones de la categoría
     */
    @Override
    public List<Transaccion> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId) {
        return jpaRepository.findByUsuarioIdAndCategoriaId(usuarioId, categoriaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR FECHA ====================

    /**
     * Lista transacciones de un usuario en un rango de fechas.
     *
     * @param usuarioId   ID del usuario
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin    Fecha final (inclusive)
     * @return Lista de transacciones en el rango
     */
    @Override
    public List<Transaccion> findByUsuarioIdAndFechaBetween(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        return jpaRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== AGREGACIONES ====================

    /**
     * Calcula la suma de transacciones por usuario y tipo.
     * Usado para cálculo de balance.
     *
     * @param usuarioId ID del usuario
     * @param tipo      INGRESO o EGRESO
     * @return Suma de montos
     */
    @Override
    public BigDecimal sumByUsuarioIdAndTipo(Long usuarioId, TipoTransaccion tipo) {
        TransaccionEntity.TipoTransaccionEnum tipoEnum = mapTipoToEntity(tipo);
        BigDecimal suma = jpaRepository.sumByUsuarioIdAndTipo(usuarioId, tipoEnum);
        return suma != null ? suma : BigDecimal.ZERO;
    }

    /**
     * Calcula el balance de un usuario.
     * Balance = Ingresos - Egresos
     *
     * @param usuarioId ID del usuario
     * @return Balance del usuario
     */
    @Override
    public BigDecimal calcularBalance(Long usuarioId) {
        BigDecimal balance = jpaRepository.calcularBalance(usuarioId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // ==================== ELIMINACIÓN ====================

    /**
     * Elimina una transacción por su ID.
     *
     * @param id ID de la transacción a eliminar
     */
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    // ==================== CONTADORES ====================

    /**
     * Cuenta las transacciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Cantidad de transacciones
     */
    @Override
    public long countByUsuarioId(Long usuarioId) {
        return jpaRepository.countByUsuarioId(usuarioId);
    }

    @Override
    public List<Transaccion> findByUsuarioIdAndCategoriaIdAndFechaBetween(
            Long usuarioId, Long categoriaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return jpaRepository
                .findByUsuarioIdAndCategoriaIdAndFechaBetween(usuarioId, categoriaId, fechaInicio, fechaFin)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== MAPEO DE ENUMS ====================

    /**
     * Mapea TipoTransaccion (Domain) a TipoTransaccionEnum (Entity).
     * Método auxiliar para conversiones.
     */
    private TransaccionEntity.TipoTransaccionEnum mapTipoToEntity(TipoTransaccion tipo) {
        if (tipo == null) {
            return null;
        }

        return switch (tipo) {
            case INGRESO -> TransaccionEntity.TipoTransaccionEnum.INGRESO;
            case EGRESO -> TransaccionEntity.TipoTransaccionEnum.EGRESO;
        };
    }
}