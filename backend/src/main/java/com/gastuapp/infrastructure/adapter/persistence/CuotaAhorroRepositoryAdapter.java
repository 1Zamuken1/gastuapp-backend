package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.ahorro.CuotaAhorro;
import com.gastuapp.domain.port.ahorro.CuotaAhorroRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.CuotaAhorroEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.CuotaAhorroEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.JpaCuotaAhorroRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de repositorio para Cuotas de Ahorro.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
@Component
public class CuotaAhorroRepositoryAdapter implements CuotaAhorroRepositoryPort {

    private final JpaCuotaAhorroRepository jpaRepository;
    private final CuotaAhorroEntityMapper mapper;

    public CuotaAhorroRepositoryAdapter(JpaCuotaAhorroRepository jpaRepository, CuotaAhorroEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CuotaAhorro save(CuotaAhorro cuota) {
        CuotaAhorroEntity entity = mapper.toEntity(cuota);
        /*
         * Si el ID es nulo, JPA crea nuevo. Si no, actualiza.
         * Aseguramos que la entidad esté completa.
         */
        CuotaAhorroEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<CuotaAhorro> saveAll(List<CuotaAhorro> cuotas) {
        List<CuotaAhorroEntity> entities = cuotas.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        List<CuotaAhorroEntity> savedEntities = jpaRepository.saveAll(entities);
        return savedEntities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CuotaAhorro> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CuotaAhorro> findAllByMetaAhorroId(Long metaAhorroId) {
        return jpaRepository.findByMetaAhorroIdOrderByNumeroCuotaAsc(metaAhorroId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByMetaAhorroId(Long metaAhorroId) {
        jpaRepository.deleteByMetaAhorroId(metaAhorroId);
    }
}
