package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.AhorroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para AhorroEntity.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Repository
public interface JpaAhorroRepository extends JpaRepository<AhorroEntity, Long> {

    List<AhorroEntity> findByMetaAhorroId(Long metaAhorroId);

    void deleteByMetaAhorroId(Long metaAhorroId);
}
