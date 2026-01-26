package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.ahorro.Ahorro;
import com.gastuapp.domain.port.ahorro.AhorroRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.AhorroEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.AhorroEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.JpaAhorroRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter: AhorroRepositoryAdapter
 * 
 * <p>
 * Implementación concreta del puerto de repositorio para Movimientos de Ahorro.
 * Utiliza JPA para la persistencia.
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class AhorroRepositoryAdapter implements AhorroRepositoryPort {

    private final JpaAhorroRepository jpaRepository;
    private final AhorroEntityMapper mapper;

    public AhorroRepositoryAdapter(
            JpaAhorroRepository jpaRepository,
            AhorroEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Ahorro save(Ahorro ahorro) {
        AhorroEntity entity = mapper.toEntity(ahorro);
        AhorroEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Ahorro> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Ahorro> findAllByMetaAhorroId(Long metaAhorroId) {
        return jpaRepository.findByMetaAhorroId(metaAhorroId).stream()
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
