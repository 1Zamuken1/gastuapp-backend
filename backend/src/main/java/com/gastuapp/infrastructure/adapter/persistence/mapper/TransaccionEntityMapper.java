package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import com.gastuapp.domain.model.transaccion.Transaccion;
import com.gastuapp.infrastructure.adapter.persistence.entity.TransaccionEntity;
import org.springframework.stereotype.Component;

/**
 * Entity Mapper: TransaccionEntityMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: TransaccionRepositoryAdapter
 * - CONVIERTE: Transaccion (Domain) ↔ TransaccionEntity (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Traduce entre el modelo de dominio puro y la entidad JPA.
 * Mapea enums de Domain a enums de Entity y viceversa.
 * Mantiene la independencia de capas (Domain no conoce JPA).
 *
 * CONVERSIONES:
 * - toDomain(): TransaccionEntity → Transaccion (para leer de BD)
 * - toEntity(): Transaccion → TransaccionEntity (para guardar en BD)
 * - updateEntity(): Actualiza TransaccionEntity existente con datos de
 * Transaccion
 *
 * MAPEO DE ENUMS:
 * - TipoTransaccion (Domain) ↔ TransaccionEntity.TipoTransaccionEnum
 * (Infrastructure)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class TransaccionEntityMapper {

    // ==================== ENTITY → DOMAIN ====================

    /**
     * Convierte TransaccionEntity (JPA) a Transaccion (Domain).
     * Usado cuando se lee de la base de datos.
     *
     * FLUJO:
     * PostgreSQL → JpaRepository → TransaccionEntity → [ESTE MÉTODO] → Transaccion
     * → Service
     *
     * @param entity TransaccionEntity de la BD
     * @return Transaccion del Domain (null si entity es null)
     */
    public Transaccion toDomain(TransaccionEntity entity) {
        if (entity == null) {
            return null;
        }

        Transaccion transaccion = new Transaccion();

        // IDs y referencias
        transaccion.setId(entity.getId());
        transaccion.setUsuarioId(entity.getUsuarioId());
        transaccion.setCategoriaId(entity.getCategoriaId());
        transaccion.setProyeccionId(entity.getProyeccionId());

        // Datos de la transacción
        transaccion.setMonto(entity.getMonto());
        transaccion.setTipo(mapTipoToDomain(entity.getTipo()));
        transaccion.setDescripcion(entity.getDescripcion());
        transaccion.setFecha(entity.getFecha());
        transaccion.setFechaCreacion(entity.getFechaCreacion());

        // Flags
        transaccion.setEsAutomatica(entity.getEsAutomatica());

        return transaccion;
    }

    // ==================== DOMAIN → ENTITY ====================

    /**
     * Convierte Transaccion (Domain) a TransaccionEntity (JPA).
     * Usado cuando se va a guardar en la base de datos.
     *
     * FLUJO:
     * Service → Transaccion → [ESTE MÉTODO] → TransaccionEntity → JpaRepository →
     * PostgreSQL
     *
     * @param transaccion Transaccion del Domain
     * @return TransaccionEntity para la BD (null si transaccion es null)
     */
    public TransaccionEntity toEntity(Transaccion transaccion) {
        if (transaccion == null) {
            return null;
        }

        TransaccionEntity entity = new TransaccionEntity();

        // IDs y referencias
        entity.setId(transaccion.getId());
        entity.setUsuarioId(transaccion.getUsuarioId());
        entity.setCategoriaId(transaccion.getCategoriaId());
        entity.setProyeccionId(transaccion.getProyeccionId());

        // Datos de la transacción
        entity.setMonto(transaccion.getMonto());
        entity.setTipo(mapTipoToEntity(transaccion.getTipo()));
        entity.setDescripcion(transaccion.getDescripcion());
        entity.setFecha(transaccion.getFecha());
        entity.setFechaCreacion(transaccion.getFechaCreacion());

        // Flags
        entity.setEsAutomatica(transaccion.getEsAutomatica());

        return entity;
    }

    // ==================== UPDATE ENTITY ====================

    /**
     * Actualiza una TransaccionEntity existente con datos de Transaccion.
     * Usado para operaciones de UPDATE (no se cambia el ID).
     *
     * IMPORTANTE:
     * - NO actualiza: id, fechaCreacion (inmutables)
     * - SÍ actualiza: todos los demás campos
     *
     * FLUJO:
     * Service → Transaccion actualizada → [ESTE MÉTODO] → TransaccionEntity
     * actualizada → JpaRepository → PostgreSQL
     *
     * @param entity      TransaccionEntity existente (con id de BD)
     * @param transaccion Transaccion con datos nuevos
     */
    public void updateEntity(TransaccionEntity entity, Transaccion transaccion) {
        if (entity == null || transaccion == null) {
            return;
        }

        // Actualizar referencias (solo categoría es editable)
        entity.setCategoriaId(transaccion.getCategoriaId());
        entity.setProyeccionId(transaccion.getProyeccionId());

        // Actualizar datos de la transacción
        entity.setMonto(transaccion.getMonto());
        entity.setTipo(mapTipoToEntity(transaccion.getTipo()));
        entity.setDescripcion(transaccion.getDescripcion());
        entity.setFecha(transaccion.getFecha());

        // Actualizar flags
        entity.setEsAutomatica(transaccion.getEsAutomatica());

        // NO actualizar: id, usuarioId, fechaCreacion
    }

    // ==================== MAPEO DE ENUMS ====================

    /**
     * Convierte TipoTransaccionEnum (Entity) a TipoTransaccion (Domain).
     */
    private TipoTransaccion mapTipoToDomain(TransaccionEntity.TipoTransaccionEnum tipoEnum) {
        if (tipoEnum == null) {
            return null;
        }

        return switch (tipoEnum) {
            case INGRESO -> TipoTransaccion.INGRESO;
            case EGRESO -> TipoTransaccion.EGRESO;
        };
    }

    /**
     * Convierte TipoTransaccion (Domain) a TipoTransaccionEnum (Entity).
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