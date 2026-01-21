package com.gastuapp.application.service;

import com.gastuapp.application.dto.response.CategoriaResponseDTO;
import com.gastuapp.application.mapper.CategoriaMapper;
import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.categoria.TipoCategoria;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service: CategoriaService
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: CategoriaController (Infrastructure Layer)
 * - USA: CategoriaRepositoryPort (Domain Port)
 * - RETORNA: CategoriaResponseDTO
 *
 * RESPONSABILIDAD:
 * Orquesta casos de uso de categorías.
 * Gestiona categorías predefinidas y personalizadas.
 *
 * CASOS DE USO:
 * 1. Listar categorías disponibles para un usuario
 * 2. Listar solo categorías predefinidas
 * 3. Buscar categoría por ID
 * 4. Listar por tipo (INGRESO, EGRESO, AMBOS)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepositoryPort categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(
            CategoriaRepositoryPort categoriaRepository,
            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    // ========= Listar categorías =========
    /**
     * Lista todas las categorías disponibles para un usuario.
     * (Predefinidas + personalizadas del usuario)
     *
     * @param usuarioId ID del usuario
     * @return Lista de categorías disponibles
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarDisponiblesParaUsuario(Long usuarioId) {
        return categoriaRepository.findAllDisponiblesParaUsuario(usuarioId).stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo las categorías predefinidas del sistema.
     *
     * @return Lista de categorías predefinidas
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarPredefinidas() {
        return categoriaRepository.findAllPredefinidas().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista categorías por tipo.
     *
     * @param tipo INGRESO, EGRESO o AMBOS
     * @return Lista de categorías del tipo especificado
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarPorTipo(TipoCategoria tipo) {
        return categoriaRepository.findByTipo(tipo).stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ============ BUSCAR POR ID ============

    /**
     * Busca una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return CategoriaResponseDTO
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría con ID " + id + " no encontrada"));

        return categoriaMapper.toResponseDTO(categoria);
    }

}
