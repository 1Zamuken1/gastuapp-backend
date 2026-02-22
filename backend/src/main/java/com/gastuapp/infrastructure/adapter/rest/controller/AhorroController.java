package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.ahorro.AhorroRequestDTO;
import com.gastuapp.application.dto.request.ahorro.MetaAhorroRequestDTO;
import com.gastuapp.application.dto.response.ahorro.AhorroResponseDTO;
import com.gastuapp.application.dto.response.ahorro.CuotaAhorroResponseDTO;
import com.gastuapp.application.dto.response.ahorro.MetaAhorroResponseDTO;
import com.gastuapp.application.service.AhorroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller: AhorroController
 * 
 * <p>
 * Maneja los endpoints para la gestión de Metas de Ahorro y Abonos.
 * </p>
 * 
 * ENDPOINTS:
 * <ul>
 * <li>POST /api/ahorros/metas - Crear nueva meta</li>
 * <li>GET /api/ahorros/metas - Listar metas del usuario</li>
 * <li>DELETE /api/ahorros/metas/{id} - Eliminar meta</li>
 * <li>POST /api/ahorros/abonos - Registrar un abono</li>
 * <li>GET /api/ahorros/metas/{id}/abonos - Historial de abonos de una meta</li>
 * </ul>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@RestController
@RequestMapping("/ahorros")
@CrossOrigin(origins = "*")
public class AhorroController {

    private final AhorroService ahorroService;

    public AhorroController(AhorroService ahorroService) {
        this.ahorroService = ahorroService;
    }

    // ==================== METAS DE AHORRO ====================

    @PostMapping("/metas")
    public ResponseEntity<MetaAhorroResponseDTO> crearMeta(@Valid @RequestBody MetaAhorroRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        MetaAhorroResponseDTO response = ahorroService.crearMeta(dto, usuarioId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/metas")
    public ResponseEntity<List<MetaAhorroResponseDTO>> listarMetas() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(ahorroService.listarMetasPorUsuario(usuarioId));
    }

    @DeleteMapping("/metas/{id}")
    public ResponseEntity<Void> eliminarMeta(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        ahorroService.eliminarMeta(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/metas/{id}")
    public ResponseEntity<MetaAhorroResponseDTO> actualizarMeta(@PathVariable Long id,
            @Valid @RequestBody MetaAhorroRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        MetaAhorroResponseDTO response = ahorroService.actualizarMeta(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    // ==================== ABONOS (AHORROS) ====================

    @PostMapping("/abonos")
    public ResponseEntity<AhorroResponseDTO> registrarAbono(@Valid @RequestBody AhorroRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        AhorroResponseDTO response = ahorroService.realizarAbono(dto, usuarioId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/metas/{id}/abonos")
    public ResponseEntity<List<AhorroResponseDTO>> listarAbonosPorMeta(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(ahorroService.listarAbonosPorMeta(id, usuarioId));
    }

    // ==================== CUOTAS (PLAN DE PAGOS) ====================

    @GetMapping("/metas/{id}/cuotas")
    public ResponseEntity<List<CuotaAhorroResponseDTO>> listarCuotasPorMeta(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(ahorroService.listarCuotasPorMeta(id, usuarioId));
    }

    @DeleteMapping("/abonos/{id}")
    public ResponseEntity<Void> eliminarAbono(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        ahorroService.eliminarAbono(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/abonos/{id}")
    public ResponseEntity<AhorroResponseDTO> actualizarAbono(@PathVariable Long id,
            @Valid @RequestBody AhorroRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        AhorroResponseDTO response = ahorroService.actualizarAbono(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    // ==================== UTILIDADES ====================

    private Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // AnonymousAuthenticationToken tiene isAuthenticated()=true pero NO es un
        // usuario real
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String userIdStr = authentication.getName();
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("ID de usuario inválido en token JWT: " + userIdStr);
        }
    }
}
