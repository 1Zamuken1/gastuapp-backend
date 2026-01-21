package com.gastuapp.infrastructure.adapter.persistence.repository;

import com.gastuapp.infrastructure.adapter.persistence.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository: CategoriaJpaRepository
 *
 * FLUJO DE DATOS:
 * - USADO POR: CategoriaRepositoryAdapter (Infrastructure Layer)
 * - ACCEDE A: PostgreSQL tabla 'categorias'
 *
 * RESPONSABILIDAD:
 * Interface de Spring Data JPA para operaciones de persistencia de categorías.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Repository
public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {
    /**
     * Lista todas las categorías predefinidas del sistema.
     */
    List<CategoriaEntity> findByPredefinidaTrue();

    /**
     * Lista categorías personalizadas de un usuario.
     */
    List<CategoriaEntity> findByUsuarioId(Long usuarioId);

    /**
     * Lista categorías por tipo (INGRESO, EGRESO, AMBOS).
     */
    List<CategoriaEntity> findByTipo(CategoriaEntity.TipoCategoriaEnum tipo);

    /**
     * Verifica si existe una categoría predefinida con ese nombre.
     */
    boolean existsByNombreAndPredefinidaTrue(String nombre);

    /**
     * Lista todas las categorías disponibles para un usuario.
     * (Predefinidas + personalizadas del usuario)
     */
    @Query("SELECT c FROM CategoriaEntity c WHERE c.predefinida = true OR c.usuarioId = :usuarioId")
    List<CategoriaEntity> findAllDisponiblesParaUsuario(@Param("usuarioId") Long usuarioId);
}
