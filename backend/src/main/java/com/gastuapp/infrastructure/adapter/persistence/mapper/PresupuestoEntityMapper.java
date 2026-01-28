package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;
import com.gastuapp.domain.model.planificacion.FrecuenciaPresupuesto;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.infrastructure.adapter.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity Mapper: PresupuestoEntityMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: PresupuestoRepositoryAdapter
 * - CONVIERTE: Presupuesto (Domain) ↔ PresupuestoEntity (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Traduce entre el modelo de dominio puro y la entidad JPA.
 * Mapea enums de Domain a enums de Entity y viceversa.
 * Mantiene la independencia de capas (Domain no conoce JPA).
 *
 * CONVERSIONES:
 * - toDomain(): PresupuestoEntity → Presupuesto (para leer de BD)
 * - toEntity(): Presupuesto → PresupuestoEntity (para guardar en BD)
 * - updateEntity(): Actualiza PresupuestoEntity existente con datos de Presupuesto
 * - toDomainList(): Lista de Entity → Lista de Domain
 *
 * MAPEO DE ENUMS:
 * - FrecuenciaPresupuesto (Domain) ↔ PresupuestoEntity.FrecuenciaPresupuestoEnum (Infrastructure)
 * - EstadoPresupuesto (Domain) ↔ PresupuestoEntity.EstadoPresupuestoEnum (Infrastructure)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Component
public class PresupuestoEntityMapper {

    // ==================== ENTITY → DOMAIN ====================

    /**
     * Convierte PresupuestoEntity (JPA) a Presupuesto (Domain).
     * Usado cuando se lee de la base de datos.
     *
     * FLUJO:
     * PostgreSQL → JpaRepository → PresupuestoEntity → [ESTE MÉTODO] → Presupuesto → Service
     *
     * @param entity PresupuestoEntity de la BD
     * @return Presupuesto del Domain (null si entity es null)
     */
    public Presupuesto toDomain(PresupuestoEntity entity) {
        if (entity == null) {
            return null;
        }

        Presupuesto presupuesto = new Presupuesto();

        // IDs y referencias
        presupuesto.setId(entity.getId());
        presupuesto.setPublicId(entity.getPublicId());
        presupuesto.setUsuarioId(entity.getUsuarioId());
        presupuesto.setCategoriaId(entity.getCategoriaId());

        // Datos del presupuesto
        presupuesto.setMontoTope(entity.getMontoTope());
        presupuesto.setMontoGastado(entity.getMontoGastado());
        presupuesto.setFechaInicio(entity.getFechaInicio());
        presupuesto.setFechaFin(entity.getFechaFin());
        presupuesto.setFrecuencia(mapFrecuenciaToDomain(entity.getFrecuencia()));
        presupuesto.setEstado(mapEstadoToDomain(entity.getEstado()));
        presupuesto.setAutoRenovar(entity.getAutoRenovar());
        presupuesto.setFechaCreacion(entity.getFechaCreacion());

        return presupuesto;
    }

    /**
     * Convierte una lista de PresupuestoEntity a Presupuesto (Domain).
     *
     * @param entities Lista de entidades de la BD
     * @return Lista de modelos del Domain
     */
    public List<Presupuesto> toDomainList(List<PresupuestoEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== DOMAIN → ENTITY ====================

    /**
     * Convierte Presupuesto (Domain) a PresupuestoEntity (JPA).
     * Usado cuando se va a guardar en la base de datos.
     *
     * FLUJO:
     * Service → Presupuesto → [ESTE MÉTODO] → PresupuestoEntity → JpaRepository → PostgreSQL
     *
     * @param presupuesto Presupuesto del Domain
     * @return PresupuestoEntity para la BD (null si presupuesto es null)
     */
    public PresupuestoEntity toEntity(Presupuesto presupuesto) {
        if (presupuesto == null) {
            return null;
        }

        PresupuestoEntity entity = new PresupuestoEntity();

        // IDs y referencias
        entity.setId(presupuesto.getId());
        entity.setPublicId(presupuesto.getPublicId());
        entity.setUsuarioId(presupuesto.getUsuarioId());
        entity.setCategoriaId(presupuesto.getCategoriaId());

        // Datos del presupuesto
        entity.setMontoTope(presupuesto.getMontoTope());
        entity.setMontoGastado(presupuesto.getMontoGastado());
        entity.setFechaInicio(presupuesto.getFechaInicio());
        entity.setFechaFin(presupuesto.getFechaFin());
        entity.setFrecuencia(mapFrecuenciaToEntity(presupuesto.getFrecuencia()));
        entity.setEstado(mapEstadoToEntity(presupuesto.getEstado()));
        entity.setAutoRenovar(presupuesto.getAutoRenovar());
        entity.setFechaCreacion(presupuesto.getFechaCreacion());

        return entity;
    }

    // ==================== UPDATE ENTITY ====================

    /**
     * Actualiza una PresupuestoEntity existente con datos de Presupuesto.
     * Usado para operaciones de UPDATE (no se cambia el ID).
     *
     * IMPORTANTE:
     * - NO actualiza: id, publicId, usuarioId, fechaCreacion (inmutables)
     * - SÍ actualiza: todos los demás campos
     *
     * FLUJO:
     * Service → Presupuesto actualizada → [ESTE MÉTODO] → PresupuestoEntity actualizada → JpaRepository → PostgreSQL
     *
     * @param entity      PresupuestoEntity existente (con id de BD)
     * @param presupuesto Presupuesto con datos nuevos
     */
    public void updateEntity(PresupuestoEntity entity, Presupuesto presupuesto) {
        if (entity == null || presupuesto == null) {
            return;
        }

        // Actualizar referencias (solo categoría es editable)
        entity.setCategoriaId(presupuesto.getCategoriaId());

        // Actualizar datos del presupuesto
        entity.setMontoTope(presupuesto.getMontoTope());
        entity.setMontoGastado(presupuesto.getMontoGastado());
        entity.setFechaInicio(presupuesto.getFechaInicio());
        entity.setFechaFin(presupuesto.getFechaFin());
        entity.setFrecuencia(mapFrecuenciaToEntity(presupuesto.getFrecuencia()));
        entity.setEstado(mapEstadoToEntity(presupuesto.getEstado()));
        entity.setAutoRenovar(presupuesto.getAutoRenovar());

        // NO actualizar: id, publicId, usuarioId, fechaCreacion
    }

    // ==================== MAPEO DE ENUMS ====================

    /**
     * Convierte FrecuenciaPresupuestoEnum (Entity) a FrecuenciaPresupuesto (Domain).
     */
    private FrecuenciaPresupuesto mapFrecuenciaToDomain(PresupuestoEntity.FrecuenciaPresupuestoEnum frecuenciaEnum) {
        if (frecuenciaEnum == null) {
            return null;
        }

        return switch (frecuenciaEnum) {
            case SEMANAL -> FrecuenciaPresupuesto.SEMANAL;
            case QUINCENAL -> FrecuenciaPresupuesto.QUINCENAL;
            case MENSUAL -> FrecuenciaPresupuesto.MENSUAL;
            case TRIMESTRAL -> FrecuenciaPresupuesto.TRIMESTRAL;
            case SEMESTRAL -> FrecuenciaPresupuesto.SEMESTRAL;
            case ANUAL -> FrecuenciaPresupuesto.ANUAL;
        };
    }

    /**
     * Convierte FrecuenciaPresupuesto (Domain) a FrecuenciaPresupuestoEnum (Entity).
     */
    private PresupuestoEntity.FrecuenciaPresupuestoEnum mapFrecuenciaToEntity(FrecuenciaPresupuesto frecuencia) {
        if (frecuencia == null) {
            return null;
        }

        return switch (frecuencia) {
            case SEMANAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.SEMANAL;
            case QUINCENAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.QUINCENAL;
            case MENSUAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.MENSUAL;
            case TRIMESTRAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.TRIMESTRAL;
            case SEMESTRAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.SEMESTRAL;
            case ANUAL -> PresupuestoEntity.FrecuenciaPresupuestoEnum.ANUAL;
        };
    }

    /**
     * Convierte EstadoPresupuestoEnum (Entity) a EstadoPresupuesto (Domain).
     */
    private EstadoPresupuesto mapEstadoToDomain(PresupuestoEntity.EstadoPresupuestoEnum estadoEnum) {
        if (estadoEnum == null) {
            return null;
        }

        return switch (estadoEnum) {
            case ACTIVA -> EstadoPresupuesto.ACTIVA;
            case INACTIVA -> EstadoPresupuesto.INACTIVA;
            case EXCEDIDA -> EstadoPresupuesto.EXCEDIDA;
        };
    }

    /**
     * Convierte EstadoPresupuesto (Domain) a EstadoPresupuestoEnum (Entity).
     */
    private PresupuestoEntity.EstadoPresupuestoEnum mapEstadoToEntity(EstadoPresupuesto estado) {
        if (estado == null) {
            return null;
        }

        return switch (estado) {
            case ACTIVA -> PresupuestoEntity.EstadoPresupuestoEnum.ACTIVA;
            case INACTIVA -> PresupuestoEntity.EstadoPresupuestoEnum.INACTIVA;
            case EXCEDIDA -> PresupuestoEntity.EstadoPresupuestoEnum.EXCEDIDA;
        };
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte lista de Domain a Entity.
     * Útil para operaciones batch.
     *
     * @param presupuestos Lista de modelos del Domain
     * @return Lista de entidades para la BD
     */
    public List<PresupuestoEntity> toEntityList(List<Presupuesto> presupuestos) {
        if (presupuestos == null) {
            return List.of();
        }
        return presupuestos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si dos entidades representan el mismo presupuesto.
     * Compara por publicId para independencia de ID interno.
     *
     * @param entity1 Primera entidad
     * @param entity2 Segunda entidad
     * @return true si representan el mismo presupuesto
     */
    public boolean esMismoPresupuesto(PresupuestoEntity entity1, PresupuestoEntity entity2) {
        if (entity1 == null || entity2 == null) {
            return false;
        }
        return entity1.getPublicId() != null && 
               entity1.getPublicId().equals(entity2.getPublicId());
    }

    /**
     * Crea una copia de una entidad sin ID.
     * Útil para duplicar presupuestos (ej: auto-renovación).
     *
     * @param original Entidad original
     * @return Nueva entidad sin ID (para insertar como nuevo registro)
     */
    public PresupuestoEntity copiarSinId(PresupuestoEntity original) {
        if (original == null) {
            return null;
        }

        PresupuestoEntity copia = new PresupuestoEntity();
        copia.setPublicId(original.getPublicId());
        copia.setUsuarioId(original.getUsuarioId());
        copia.setCategoriaId(original.getCategoriaId());
        copia.setMontoTope(original.getMontoTope());
        copia.setMontoGastado(original.getMontoGastado());
        copia.setFechaInicio(original.getFechaInicio());
        copia.setFechaFin(original.getFechaFin());
        copia.setFrecuencia(original.getFrecuencia());
        copia.setEstado(original.getEstado());
        copia.setAutoRenovar(original.getAutoRenovar());
        // No copiar fechaCreacion, se generará nueva

        return copia;
    }
}