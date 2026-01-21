package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.categoria.TipoCategoria;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.CategoriaEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.CategoriaEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter: CategoriaRepositoryAdapter
 *
 * FLUJO DE DATOS:
 * - IMPLEMENTA: CategoriaRepositoryPort (Domain Layer)
 * - USA: CategoriaJpaRepository (Spring Data JPA)
 * - USA: CategoriaEntityMapper (conversión Domain ↔ Entity)
 *
 * RESPONSABILIDAD:
 * Implementación concreta del port de categorías.
 * Conecta el Domain con PostgreSQL.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository jpaRepository;
    private final CategoriaEntityMapper mapper;

    public CategoriaRepositoryAdapter(
            CategoriaJpaRepository jpaRepository,
            CategoriaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Categoria save(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Categoria> findAllPredefinidas() {
        return jpaRepository.findByPredefinidaTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Categoria> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Categoria> findAllDisponiblesParaUsuario(Long usuarioId) {
        return jpaRepository.findAllDisponiblesParaUsuario(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Categoria> findByTipo(TipoCategoria tipo) {
        CategoriaEntity.TipoCategoriaEnum tipoEnum = mapTipoToEntity(tipo);
        return jpaRepository.findByTipo(tipoEnum).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNombreAndPredefinidaTrue(String nombre) {
        return jpaRepository.existsByNombreAndPredefinidaTrue(nombre);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    // Mapeo auxiliar
    private CategoriaEntity.TipoCategoriaEnum mapTipoToEntity(TipoCategoria tipo) {
        return switch (tipo) {
            case INGRESO -> CategoriaEntity.TipoCategoriaEnum.INGRESO;
            case EGRESO -> CategoriaEntity.TipoCategoriaEnum.EGRESO;
            case AMBOS -> CategoriaEntity.TipoCategoriaEnum.AMBOS;
        };
    }

}
