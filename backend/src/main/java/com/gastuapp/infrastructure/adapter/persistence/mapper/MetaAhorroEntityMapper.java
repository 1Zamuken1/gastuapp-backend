package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.ahorro.MetaAhorro;
import com.gastuapp.domain.model.ahorro.FrecuenciaAhorro;
import com.gastuapp.infrastructure.adapter.persistence.entity.MetaAhorroEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre MetaAhorro (Domain) y MetaAhorroEntity
 * (Infrastructure).
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class MetaAhorroEntityMapper {

    // ==================== ENTITY → DOMAIN ====================

    public MetaAhorro toDomain(MetaAhorroEntity entity) {
        if (entity == null) {
            return null;
        }

        MetaAhorro meta = new MetaAhorro();
        meta.setId(entity.getId());
        meta.setUsuarioId(entity.getUsuarioId());
        meta.setNombre(entity.getNombre());
        meta.setMontoObjetivo(entity.getMontoObjetivo());
        meta.setMontoActual(entity.getMontoActual());
        meta.setFechaLimite(entity.getFechaLimite());
        meta.setFechaInicio(entity.getFechaInicio());
        if (entity.getFrecuencia() != null) {
            try {
                meta.setFrecuencia(FrecuenciaAhorro.valueOf(entity.getFrecuencia()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum value or handle default
            }
        }
        meta.setColor(entity.getColor());
        meta.setIcono(entity.getIcono());
        meta.setEstado(mapEstadoToDomain(entity.getEstado()));
        meta.setFechaCreacion(entity.getFechaCreacion());

        return meta;
    }

    // ==================== DOMAIN → ENTITY ====================

    public MetaAhorroEntity toEntity(MetaAhorro meta) {
        if (meta == null) {
            return null;
        }

        MetaAhorroEntity entity = new MetaAhorroEntity();
        entity.setId(meta.getId());
        entity.setUsuarioId(meta.getUsuarioId());
        entity.setNombre(meta.getNombre());
        entity.setMontoObjetivo(meta.getMontoObjetivo());
        entity.setMontoActual(meta.getMontoActual());
        entity.setFechaLimite(meta.getFechaLimite());
        entity.setFechaInicio(meta.getFechaInicio());
        if (meta.getFrecuencia() != null) {
            entity.setFrecuencia(meta.getFrecuencia().name());
        }
        entity.setColor(meta.getColor());
        entity.setIcono(meta.getIcono());
        entity.setEstado(mapEstadoToEntity(meta.getEstado()));
        entity.setFechaCreacion(meta.getFechaCreacion());

        return entity;
    }

    // ==================== MAPEO DE ENUMS ====================

    private MetaAhorro.EstadoMeta mapEstadoToDomain(MetaAhorroEntity.EstadoMetaEnum estadoEnum) {
        if (estadoEnum == null)
            return null;
        return switch (estadoEnum) {
            case ACTIVA -> MetaAhorro.EstadoMeta.ACTIVA;
            case COMPLETADA -> MetaAhorro.EstadoMeta.COMPLETADA;
            case PAUSADA -> MetaAhorro.EstadoMeta.PAUSADA;
            case CANCELADA -> MetaAhorro.EstadoMeta.CANCELADA;
        };
    }

    private MetaAhorroEntity.EstadoMetaEnum mapEstadoToEntity(MetaAhorro.EstadoMeta estado) {
        if (estado == null)
            return null;
        return switch (estado) {
            case ACTIVA -> MetaAhorroEntity.EstadoMetaEnum.ACTIVA;
            case COMPLETADA -> MetaAhorroEntity.EstadoMetaEnum.COMPLETADA;
            case PAUSADA -> MetaAhorroEntity.EstadoMetaEnum.PAUSADA;
            case CANCELADA -> MetaAhorroEntity.EstadoMetaEnum.CANCELADA;
        };
    }
}
