package com.gastuapp.domain.port.categoria;

import java.util.List;
import java.util.Optional;

import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.categoria.TipoCategoria;

/**
 * Port: CategoriaRepositoryPort
 *
 * FLUJO DE DATOS:
 * - USADO POR: CategoriaService (Application Layer)
 * - IMPLEMENTADO POR: CategoriaRepositoryAdapter (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Define el contrato para operaciones de persistencia de Categoria.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
public interface CategoriaRepositoryPort {

    /**
     * Guarda una categoría (create o update).
     */
    Categoria save(Categoria categoria);

    /**
     * Busca una categoría por ID.
     */
    Optional<Categoria> findById(Long id);

    /**
     * Lista todas las categorías predefinidas.
     */
    List<Categoria> findAllPredefinidas();

    /**
     * Lista categorías de un usuario (personalizadas).
     */
    List<Categoria> findByUsuarioId(Long usuarioId);

    /**
     * Lista todas las categorías disponibles para un usuario.
     * (Predefinidas + personalizadas del usuario)
     */
    List<Categoria> findAllDisponiblesParaUsuario(Long usuarioId);

    /**
     * Busca categorías por tipo (INGRESO, EGRESO, AMBOS).
     */
    List<Categoria> findByTipo(TipoCategoria tipo);

    /**
     * Verifica si existe una categoría predefinida con ese nombre.
     */
    boolean existsByNombreAndPredefinidaTrue(String nombre);

    /**
     * Elimina una categoría por ID.
     * Solo se pueden eliminar categorías personalizadas.
     */
    void deleteById(Long id);
}
