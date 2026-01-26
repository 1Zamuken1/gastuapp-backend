package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.MetaAhorroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para MetaAhorroEntity.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Repository
public interface JpaMetaAhorroRepository extends JpaRepository<MetaAhorroEntity, Long> {

    List<MetaAhorroEntity> findByUsuarioId(Long usuarioId);

    boolean existsByNombreAndUsuarioId(String nombre, Long usuarioId);
}
