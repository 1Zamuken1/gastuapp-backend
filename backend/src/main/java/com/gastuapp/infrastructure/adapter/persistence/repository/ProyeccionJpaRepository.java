package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.ProyeccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: ProyeccionJpaRepository
 *
 * RESPONSABILIDAD:
 * Interfaz Spring Data JPA para operaciones CRUD en 'proyecciones'.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
@Repository
public interface ProyeccionJpaRepository extends JpaRepository<ProyeccionEntity, Long> {

    List<ProyeccionEntity> findByUsuarioIdAndActivoTrue(Long usuarioId);
}
