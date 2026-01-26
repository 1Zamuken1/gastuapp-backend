package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.ahorro.CuotaAhorro;
import com.gastuapp.infrastructure.adapter.persistence.entity.CuotaAhorroEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre CuotaAhorro (Domain) y CuotaAhorroEntity
 * (Infrastructure).
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
@Component
public class CuotaAhorroEntityMapper {

    public CuotaAhorro toDomain(CuotaAhorroEntity entity) {
        if (entity == null)
            return null;

        CuotaAhorro domain = new CuotaAhorro();
        domain.setId(entity.getId());
        domain.setMetaAhorroId(entity.getMetaAhorroId());
        domain.setNumeroCuota(entity.getNumeroCuota());
        domain.setFechaProgramada(entity.getFechaProgramada());
        domain.setMontoEsperado(entity.getMontoEsperado());
        domain.setEstado(mapEstadoToDomain(entity.getEstado()));
        domain.setAhorroId(entity.getAhorroId());
        return domain;
    }

    public CuotaAhorroEntity toEntity(CuotaAhorro domain) {
        if (domain == null)
            return null;

        CuotaAhorroEntity entity = new CuotaAhorroEntity();
        entity.setId(domain.getId());
        entity.setMetaAhorroId(domain.getMetaAhorroId());
        entity.setNumeroCuota(domain.getNumeroCuota());
        entity.setFechaProgramada(domain.getFechaProgramada());
        entity.setMontoEsperado(domain.getMontoEsperado());
        entity.setEstado(mapEstadoToEntity(domain.getEstado()));
        entity.setAhorroId(domain.getAhorroId());
        return entity;
    }

    private CuotaAhorro.EstadoCuota mapEstadoToDomain(CuotaAhorroEntity.EstadoCuotaEnum entityEnum) {
        if (entityEnum == null)
            return null;
        return switch (entityEnum) {
            case PENDIENTE -> CuotaAhorro.EstadoCuota.PENDIENTE;
            case PAGADA -> CuotaAhorro.EstadoCuota.PAGADA;
            case VENCIDA -> CuotaAhorro.EstadoCuota.VENCIDA;
            case CANCELADA -> CuotaAhorro.EstadoCuota.CANCELADA;
        };
    }

    private CuotaAhorroEntity.EstadoCuotaEnum mapEstadoToEntity(CuotaAhorro.EstadoCuota domainEnum) {
        if (domainEnum == null)
            return null;
        return switch (domainEnum) {
            case PENDIENTE -> CuotaAhorroEntity.EstadoCuotaEnum.PENDIENTE;
            case PAGADA -> CuotaAhorroEntity.EstadoCuotaEnum.PAGADA;
            case VENCIDA -> CuotaAhorroEntity.EstadoCuotaEnum.VENCIDA;
            case CANCELADA -> CuotaAhorroEntity.EstadoCuotaEnum.CANCELADA;
        };
    }
}
