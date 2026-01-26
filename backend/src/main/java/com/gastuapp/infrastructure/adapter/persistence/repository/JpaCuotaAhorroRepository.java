package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.CuotaAhorroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para CuotaAhorroEntity.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
@Repository
public interface JpaCuotaAhorroRepository extends JpaRepository<CuotaAhorroEntity, Long> {

    List<CuotaAhorroEntity> findByMetaAhorroIdOrderByNumeroCuotaAsc(Long metaAhorroId);

    void deleteByMetaAhorroId(Long metaAhorroId);
}
