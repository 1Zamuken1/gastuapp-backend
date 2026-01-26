package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.ahorro.Ahorro;
import com.gastuapp.infrastructure.adapter.persistence.entity.AhorroEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Ahorro (Domain) y AhorroEntity (Infrastructure).
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class AhorroEntityMapper {

    public Ahorro toDomain(AhorroEntity entity) {
        if (entity == null) {
            return null;
        }

        Ahorro ahorro = new Ahorro();
        ahorro.setId(entity.getId());
        ahorro.setMetaAhorroId(entity.getMetaAhorroId());
        ahorro.setUsuarioId(entity.getUsuarioId());
        ahorro.setMonto(entity.getMonto());
        ahorro.setDescripcion(entity.getDescripcion());
        ahorro.setFecha(entity.getFecha());

        return ahorro;
    }

    public AhorroEntity toEntity(Ahorro ahorro) {
        if (ahorro == null) {
            return null;
        }

        AhorroEntity entity = new AhorroEntity();
        entity.setId(ahorro.getId());
        entity.setMetaAhorroId(ahorro.getMetaAhorroId());
        entity.setUsuarioId(ahorro.getUsuarioId());
        entity.setMonto(ahorro.getMonto());
        entity.setDescripcion(ahorro.getDescripcion());
        entity.setFecha(ahorro.getFecha());

        return entity;
    }
}
