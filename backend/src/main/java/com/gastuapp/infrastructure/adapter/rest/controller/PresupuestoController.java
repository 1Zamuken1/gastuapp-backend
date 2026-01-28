package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.ActualizarPresupuestoRequestDTO;
import com.gastuapp.application.dto.request.CrearPresupuestoRequestDTO;
import com.gastuapp.application.dto.response.PresupuestoResponseDTO;
import com.gastuapp.application.service.PresupuestoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller: PresupuestoController
 *
 * FLUJO DE DATOS:
 * - RECIBE: HTTP Requests (JSON) con token JWT
 * - LLAMA A: PresupuestoService (Application Layer)
 * - RETORNA: HTTP Responses (JSON) con datos de presupuestos
 *
 * RESPONSABILIDAD:
 * Maneja endpoints protegidos para gestión de planificaciones de presupuesto.
 * Requiere autenticación JWT (JwtAuthenticationFilter).
 * Permite CRUD completo de presupuestos y consultas especializadas.
 *
 * ENDPOINTS:
 * - POST /api/presupuestos-planificaciones → Crear planificación
 * - GET /api/presupuestos-planificaciones → Listar planificaciones del usuario
 * - GET /api/presupuestos-planificaciones/{publicId} → Obtener por ID
 * - PUT /api/presupuestos-planificaciones/{publicId} → Actualizar planificación
 * - PUT /api/presupuestos-planificaciones/{publicId}/desactivar → Desactivar
 * - GET /api/presupuestos-planificaciones/activas → Listar activas
 * - GET /api/presupuestos-planificaciones/cercanos → Cercanos a exceder
 * - POST /api/presupuestos-planificaciones/actualizar-montos → Sincronizar montos
 *
 * SEGURIDAD:
 * - Todos los endpoints requieren JWT válido
 * - Usuario solo puede ver/editar sus propios presupuestos
 * - UsuarioId se obtiene del SecurityContext (JWT)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@RestController
@RequestMapping("/presupuestos-planificaciones")
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo (Angular)
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    // ==================== CREAR PRESUPUESTO ====================

    /**
     * Crea una nueva planificación de presupuesto.
     *
     * FLUJO:
     * Cliente → POST /api/presupuestos-planificaciones (con JWT + datos)
     * → [ESTE MÉTODO] → PresupuestoService → BD
     *
     * REQUEST BODY:
     * {
     *   "montoTope": 500000.00,
     *   "fechaInicio": "2026-01-01",
     *   "fechaFin": "2026-01-31",
     *   "frecuencia": "MENSUAL",
     *   "autoRenovar": true,
     *   "categoriaId": 5
     * }
     *
     * RESPONSE (201 Created):
     * {
     *   "id": 123,
     *   "publicId": "550e8400-e29b-41d4-a716-446655440000",
     *   "montoTope": 500000.00,
     *   "montoGastado": 120000.00,
     *   "montoRestante": 380000.00,
     *   "porcentajeUtilizacion": 24.0,
     *   "fechaInicio": "2026-01-01",
     *   "fechaFin": "2026-01-31",
     *   "frecuencia": "MENSUAL",
     *   "estado": "ACTIVA",
     *   "autoRenovar": true,
     *   "fechaCreacion": "2026-01-01T08:00:00",
     *   "categoriaId": 5,
     *   "categoriaNombre": "Comida y bebidas",
     *   "categoriaIcono": "🍔",
     *   "estaVigente": true,
     *   "estaExcedido": false,
     *   "diasRestantes": 4
     * }
     *
     * @param dto CrearPresupuestoRequestDTO con datos del presupuesto
     * @return ResponseEntity con PresupuestoResponseDTO (201 Created)
     */
    @PostMapping
    public ResponseEntity<PresupuestoResponseDTO> crearPresupuesto(@Valid @RequestBody CrearPresupuestoRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        PresupuestoResponseDTO response = presupuestoService.crearPresupuesto(dto, usuarioId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== LISTAR PRESUPUESTOS ====================

    /**
     * Lista todas las planificaciones del usuario autenticado.
     *
     * RESPONSE (200 OK):
     * [ PresupuestoResponseDTO[], ... ]
     *
     * @return ResponseEntity con lista de PresupuestoResponseDTO
     */
    @GetMapping
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPresupuestos() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<PresupuestoResponseDTO> presupuestos = presupuestoService.listarPresupuestosPorUsuario(usuarioId);
        return ResponseEntity.ok(presupuestos);
    }

    // ==================== OBTENER PRESUPUESTO POR ID ====================

    /**
     * Obtiene una planificación específica por su publicId.
     *
     * PATH VARIABLE:
     * /api/presupuestos-planificaciones/{publicId}
     *
     * RESPONSE (200 OK):
     * PresupuestoResponseDTO
     *
     * @param publicId ID público del presupuesto
     * @return ResponseEntity con PresupuestoResponseDTO
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<PresupuestoResponseDTO> obtenerPresupuesto(@PathVariable String publicId) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        PresupuestoResponseDTO presupuesto = presupuestoService.buscarPresupuestoPorPublicId(publicId, usuarioId);
        return ResponseEntity.ok(presupuesto);
    }

    // ==================== ACTUALIZAR PRESUPUESTO ====================

    /**
     * Actualiza una planificación de presupuesto existente.
     *
     * PATH VARIABLE:
     * /api/presupuestos-planificaciones/{publicId}
     *
     * REQUEST BODY (parcial):
     * {
     *   "montoTope": 600000.00,
     *   "autoRenovar": false
     * }
     *
     * RESPONSE (200 OK):
     * PresupuestoResponseDTO actualizado
     *
     * @param publicId ID público del presupuesto a actualizar
     * @param dto      ActualizarPresupuestoRequestDTO con cambios
     * @return ResponseEntity con PresupuestoResponseDTO actualizado
     */
    @PutMapping("/{publicId}")
    public ResponseEntity<PresupuestoResponseDTO> actualizarPresupuesto(
            @PathVariable String publicId, 
            @Valid @RequestBody ActualizarPresupuestoRequestDTO dto) {
        
        Long usuarioId = obtenerUsuarioIdAutenticado();
        PresupuestoResponseDTO presupuesto = presupuestoService.actualizarPresupuesto(publicId, dto, usuarioId);
        return ResponseEntity.ok(presupuesto);
    }

    // ==================== DESACTIVAR PRESUPUESTO ====================

    /**
     * Desactiva una planificación de presupuesto.
     * No elimina el registro, solo cambia el estado a INACTIVA.
     *
     * PATH VARIABLE:
     * /api/presupuestos-planificaciones/{publicId}/desactivar
     *
     * RESPONSE (200 OK):
     * PresupuestoResponseDTO desactivado
     *
     * @param publicId ID público del presupuesto a desactivar
     * @return ResponseEntity con PresupuestoResponseDTO desactivado
     */
    @PutMapping("/{publicId}/desactivar")
    public ResponseEntity<PresupuestoResponseDTO> desactivarPresupuesto(@PathVariable String publicId) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        PresupuestoResponseDTO presupuesto = presupuestoService.desactivarPresupuesto(publicId, usuarioId);
        return ResponseEntity.ok(presupuesto);
    }

    // ==================== LISTAR PRESUPUESTOS ACTIVOS ====================

    /**
     * Lista solo las planificaciones activas del usuario.
     * Útil para dashboard y visualizaciones principales.
     *
     * ENDPOINT:
     * /api/presupuestos-planificaciones/activas
     *
     * RESPONSE (200 OK):
     * [ PresupuestoResponseDTO[], ... ]
     *
     * @return ResponseEntity con lista de PresupuestoResponseDTO activos
     */
    @GetMapping("/activas")
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPresupuestosActivos() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<PresupuestoResponseDTO> presupuestos = presupuestoService.listarPresupuestosActivos(usuarioId);
        return ResponseEntity.ok(presupuestos);
    }

    // ==================== LISTAR PRESUPUESTOS CERCANOS A EXCEDER ====================

    /**
     * Lista presupuestos que están cerca de exceder el tope.
     * Útil para notificaciones y alertas preventivas.
     *
     * QUERY PARAM:
     * ?porcentaje=80.0 (por defecto 80.0 si no se especifica)
     *
     * ENDPOINT:
     * /api/presupuestos-planificaciones/cercanos?porcentaje=80.0
     *
     * RESPONSE (200 OK):
     * [ PresupuestoResponseDTO[], ... ]
     *
     * @param porcentaje Umbral de porcentaje (ej: 80.0 para 80%)
     * @return ResponseEntity con lista de PresupuestoResponseDTO cercanos a exceder
     */
    @GetMapping("/cercanos")
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPresupuestosCercanos(
            @RequestParam(defaultValue = "80.0") Double porcentaje) {
        
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<PresupuestoResponseDTO> presupuestos = presupuestoService.listarPresupuestosPorExceder(usuarioId, porcentaje);
        return ResponseEntity.ok(presupuestos);
    }

    // ==================== SINCRONIZAR MONTOS GASTADOS ====================

    /**
     * Sincroniza todos los montos gastados de los presupuestos del usuario.
     * Útil para correcciones de datos o recálculo manual.
     *
     * ENDPOINT:
     * /api/presupuestos-planificaciones/actualizar-montos
     *
     * RESPONSE (200 OK):
     * {
     *   "message": "Montos sincronizados exitosamente",
     *   "presupuestosActualizados": 5
     * }
     *
     * @return ResponseEntity con mensaje de confirmación
     */
    @PostMapping("/actualizar-montos")
    public ResponseEntity<String> sincronizarMontosGastados() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        presupuestoService.sincronizarMontosGastados(usuarioId);
        return ResponseEntity.ok("Montos sincronizados exitosamente para el usuario " + usuarioId);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext.
     * El JwtAuthenticationFilter establece el userId como principal.
     *
     * @return ID del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    private Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        // El JwtFilter establece el userId como principal (String)
        String userIdStr = authentication.getName();
        
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("ID de usuario inválido en token JWT");
        }
    }

    // ==================== MANEJO DE ERRORES ====================

    /**
     * Manejo centralizado de IllegalArgumentException.
     * Convierte validaciones de negocio en respuestas HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Manejo centralizado de IllegalStateException.
     * Convierte errores de estado en respuestas HTTP 409.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Manejo centralizado de RuntimeException.
     * Convierte errores inesperados en respuestas HTTP 500.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + ex.getMessage());
    }
}