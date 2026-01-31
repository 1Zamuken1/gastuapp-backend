package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.proyeccion.Proyeccion;
import com.gastuapp.infrastructure.adapter.persistence.entity.ProyeccionEntity;
import org.springframework.stereotype.Component;

@Component
public class ProyeccionEntityMapper {

    public Proyeccion toDomain(ProyeccionEntity entity) {
        if (entity == null)
            return null;
        Proyeccion domain = new Proyeccion();
        domain.setId(entity.getId());
        domain.setMonto(entity.getMonto());
        domain.setTipo(entity.getTipo());
        domain.setCategoriaId(entity.getCategoriaId());
        domain.setUsuarioId(entity.getUsuarioId());
        domain.setFrecuencia(entity.getFrecuencia());
        domain.setFechaInicio(entity.getFechaInicio());
        domain.setUltimaEjecucion(entity.getUltimaEjecucion());
        domain.setActivo(entity.getActivo());
        return domain;
    }

    public ProyeccionEntity toEntity(Proyeccion domain) {
        if (domain == null)
            return null;
        ProyeccionEntity entity = new ProyeccionEntity();
        entity.setId(domain.getId());
        entity.setMonto(domain.getMonto());
        entity.setTipo(domain.getTipo());
        entity.setCategoriaId(domain.getCategoriaId());
        entity.setUsuarioId(domain.getUsuarioId());
        entity.setFrecuencia(domain.getFrecuencia());
        entity.setFechaInicio(domain.getFechaInicio());
        entity.setUltimaEjecucion(domain.getUltimaEjecucion());
        entity.setActivo(domain.getActivo());
        return entity;
    }
}
