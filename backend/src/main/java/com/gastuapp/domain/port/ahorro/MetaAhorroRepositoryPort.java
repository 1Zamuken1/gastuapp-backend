package com.gastuapp.domain.port.ahorro;

import com.gastuapp.domain.model.ahorro.MetaAhorro;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Metas de Ahorro.
 * 
 * <p>
 * Define las operaciones CRUD y de consulta necesarias para el dominio,
 * desacopladas de la persistencia específica (JPA, SQL, Mongo, etc).
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
public interface MetaAhorroRepositoryPort {

    MetaAhorro save(MetaAhorro metaAhorro);

    Optional<MetaAhorro> findById(Long id);

    List<MetaAhorro> findAllByUsuarioId(Long usuarioId);

    void deleteById(Long id);

    /**
     * Verifica si existe una meta con el mismo nombre para el usuario.
     * Útil para evitar duplicados lógicos.
     */
    boolean existsByNombreAndUsuarioId(String nombre, Long usuarioId);
}
