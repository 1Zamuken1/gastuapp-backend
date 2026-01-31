package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.proyeccion.Proyeccion;
import com.gastuapp.infrastructure.adapter.persistence.entity.ProyeccionEntity;
import org.springframework.stereotype.Component;

@Component
public class ProyeccionEntityMapper {

    public Proyeccion toDomain(ProyeccionEntity entity) {
        if (entity == null)
            return null;
        return new Proyeccion(
                entity.getId(),
                entity.getNombre(),
                entity.getMonto(),
                entity.getTipo(),
                entity.getCategoriaId(),
                entity.getUsuarioId(),
                entity.getFrecuencia(),
                entity.getFechaInicio(),
                entity.getUltimaEjecucion(),
                entity.getActivo());
    }

    public ProyeccionEntity toEntity(Proyeccion domain) {
        if (domain == null)
            return null;
        ProyeccionEntity entity = new ProyeccionEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
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
