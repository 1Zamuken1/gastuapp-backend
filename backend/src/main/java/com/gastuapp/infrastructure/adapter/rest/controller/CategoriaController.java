package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.response.CategoriaResponseDTO;
import com.gastuapp.application.service.CategoriaService;
import com.gastuapp.domain.model.categoria.TipoCategoria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller: CategoriaController
 *
 * FLUJO DE DATOS:
 * - RECIBE: HTTP Requests (con JWT)
 * - LLAMA A: CategoriaService (Application Layer)
 * - RETORNA: HTTP Responses (JSON)
 *
 * RESPONSABILIDAD:
 * Maneja endpoints de categorías.
 * Todos los endpoints son públicos (cualquier usuario autenticado puede
 * listar).
 *
 * ENDPOINTS:
 * - GET /api/categorias → Listar categorías predefinidas
 * - GET /api/categorias/{id} → Obtener categoría por ID
 * - GET /api/categorias/tipo/{tipo} → Listar por tipo
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // ============ LISTAR CATEGORÍAS ============

    /**
     * Lista todas las categorías predefinidas.
     *
     * FLUJO:
     * Cliente → GET /api/categorias (con JWT)
     * → [ESTE MÉTODO] → CategoriaService → BD
     *
     * RESPONSE (200 OK):
     * [
     * {
     * "id": 1,
     * "nombre": "Comida",
     * "icono": "🍔",
     * "tipo": "EGRESO",
     * "predefinida": true
     * }
     * ]
     *
     * @return Lista de categorías predefinidas
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        List<CategoriaResponseDTO> categorias = categoriaService.listarPredefinidas();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return CategoriaResponseDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerCategoria(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(categoria);
    }

    /**
     * Lista categorías por tipo.
     *
     * FLUJO:
     * Cliente → GET /api/categorias/tipo/INGRESO
     * → [ESTE MÉTODO] → CategoriaService
     *
     * @param tipo INGRESO, EGRESO o AMBOS
     * @return Lista de categorías del tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorTipo(@PathVariable String tipo) {
        TipoCategoria tipoCategoria = TipoCategoria.valueOf(tipo.toUpperCase());
        List<CategoriaResponseDTO> categorias = categoriaService.listarPorTipo(tipoCategoria);
        return ResponseEntity.ok(categorias);
    }
}