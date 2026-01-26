package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.ahorro.MetaAhorro;
import com.gastuapp.domain.port.ahorro.MetaAhorroRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.MetaAhorroEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.MetaAhorroEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.JpaMetaAhorroRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter: MetaAhorroRepositoryAdapter
 * 
 * <p>
 * Implementación concreta del puerto de repositorio para Metas de Ahorro.
 * Utiliza JPA para la persistencia.
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class MetaAhorroRepositoryAdapter implements MetaAhorroRepositoryPort {

    private final JpaMetaAhorroRepository jpaRepository;
    private final MetaAhorroEntityMapper mapper;

    public MetaAhorroRepositoryAdapter(
            JpaMetaAhorroRepository jpaRepository,
            MetaAhorroEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MetaAhorro save(MetaAhorro metaAhorro) {
        MetaAhorroEntity entity = mapper.toEntity(metaAhorro);
        MetaAhorroEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<MetaAhorro> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<MetaAhorro> findAllByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombreAndUsuarioId(String nombre, Long usuarioId) {
        return jpaRepository.existsByNombreAndUsuarioId(nombre, usuarioId);
    }
}
