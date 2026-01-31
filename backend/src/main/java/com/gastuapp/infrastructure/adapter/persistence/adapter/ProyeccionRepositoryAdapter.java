package com.gastuapp.infrastructure.adapter.persistence.adapter;

import com.gastuapp.domain.model.proyeccion.Proyeccion;
import com.gastuapp.domain.port.proyeccion.ProyeccionRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.ProyeccionEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.ProyeccionEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.ProyeccionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter: ProyeccionRepositoryAdapter
 *
 * RESPONSABILIDAD:
 * Implementa el puerto del dominio usando JPA.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
@Component
public class ProyeccionRepositoryAdapter implements ProyeccionRepositoryPort {

    private final ProyeccionJpaRepository repository;
    private final ProyeccionEntityMapper mapper;

    public ProyeccionRepositoryAdapter(ProyeccionJpaRepository repository, ProyeccionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Proyeccion save(Proyeccion proyeccion) {
        ProyeccionEntity entity = mapper.toEntity(proyeccion);
        ProyeccionEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Proyeccion> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Proyeccion> findAllByUsuarioIdAndActivoTrue(Long usuarioId) {
        return repository.findByUsuarioIdAndActivoTrue(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        // En este caso, el borrado físico se usará si lo llamasemos.
        // Pero para soft delete, se usa save() con el campo activo=false desde el
        // dominio.
        // Sin embargo, implementamos el método para cumplir el contrato si fuera
        // necesario.
        repository.deleteById(id);
    }
}
