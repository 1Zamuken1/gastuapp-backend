package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.TransaccionRequestDTO;
import com.gastuapp.application.dto.response.TransaccionResponseDTO;
import com.gastuapp.application.service.TransaccionService;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller: TransaccionController
 *
 * FLUJO DE DATOS:
 * - RECIBE: HTTP Requests (JSON) con token JWT
 * - LLAMA A: TransaccionService (Application Layer)
 * - RETORNA: HTTP Responses (JSON) con datos de transacciones
 *
 * RESPONSABILIDAD:
 * Maneja endpoints protegidos para gestión de transacciones.
 * Requiere autenticación JWT (JwtAuthenticationFilter).
 * Permite CRUD completo de transacciones y consultas financieras.
 *
 * ENDPOINTS:
 * - POST /api/transacciones → Crear transacción
 * - GET /api/transacciones → Listar transacciones del usuario
 * - GET /api/transacciones/{id} → Obtener transacción por ID
 * - PUT /api/transacciones/{id} → Actualizar transacción
 * - DELETE /api/transacciones/{id} → Eliminar transacción
 * - GET /api/transacciones/tipo/{tipo} → Listar por tipo
 * - GET /api/transacciones/categoria/{categoriaId} → Listar por categoría
 * - GET /api/transacciones/rango → Listar por rango de fechas
 * - GET /api/transacciones/balance → Calcular balance
 * - GET /api/transacciones/resumen → Resumen financiero
 *
 * SEGURIDAD:
 * - Todos los endpoints requieren JWT válido
 * - Usuario solo puede ver/editar sus propias transacciones
 * - UsuarioId se obtiene del SecurityContext (JWT)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@RestController
@RequestMapping("/transacciones")
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo (Angular)
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // ==================== CREAR TRANSACCIÓN ====================

    /**
     * Crea una nueva transacción.
     *
     * FLUJO:
     * Cliente → POST /api/transacciones (con JWT + datos)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * REQUEST BODY:
     * {
     * "monto": 45000.50,
     * "tipo": "EGRESO",
     * "descripcion": "Compra de mercado en Éxito",
     * "fecha": "2025-01-21",
     * "categoriaId": 1
     * }
     *
     * RESPONSE (201 Created):
     * {
     * "id": 123,
     * "monto": 45000.50,
     * "tipo": "EGRESO",
     * "descripcion": "Compra de mercado en Éxito",
     * "fecha": "2025-01-21",
     * "categoriaId": 1,
     * "categoriaNombre": "Comida y bebidas",
     * "categoriaIcono": "🍔",
     * "usuarioId": 5
     * }
     *
     * @param dto TransaccionRequestDTO con datos de la transacción
     * @return ResponseEntity con TransaccionResponseDTO (201 Created)
     */
    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> crearTransaccion(@Valid @RequestBody TransaccionRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        TransaccionResponseDTO transaccion = transaccionService.crearTransaccion(dto, usuarioId);
        return new ResponseEntity<>(transaccion, HttpStatus.CREATED);
    }

    // ==================== LISTAR TRANSACCIONES ====================

    /**
     * Lista todas las transacciones del usuario autenticado.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * RESPONSE (200 OK):
     * [
     * { "id": 1, "monto": 5000, "tipo": "INGRESO", ... },
     * { "id": 2, "monto": 3000, "tipo": "EGRESO", ... }
     * ]
     *
     * @return ResponseEntity con lista de TransaccionResponseDTO
     */
    @GetMapping
    public ResponseEntity<List<TransaccionResponseDTO>> listarTransacciones() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<TransaccionResponseDTO> transacciones = transaccionService.listarTransacciones(usuarioId);
        return ResponseEntity.ok(transacciones);
    }

    // ==================== OBTENER TRANSACCIÓN POR ID ====================

    /**
     * Obtiene una transacción por su ID.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/{id} (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * SEGURIDAD:
     * Solo si la transacción pertenece al usuario autenticado.
     *
     * @param id ID de la transacción
     * @return ResponseEntity con TransaccionResponseDTO (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtenerTransaccion(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        TransaccionResponseDTO transaccion = transaccionService.buscarPorId(id, usuarioId);
        return ResponseEntity.ok(transaccion);
    }

    // ==================== ACTUALIZAR TRANSACCIÓN ====================

    /**
     * Actualiza una transacción existente.
     *
     * FLUJO:
     * Cliente → PUT /api/transacciones/{id} (con JWT + datos)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * REQUEST BODY:
     * {
     * "monto": 50000,
     * "tipo": "EGRESO",
     * "descripcion": "Compra actualizada",
     * "fecha": "2025-01-21",
     * "categoriaId": 1
     * }
     *
     * @param id  ID de la transacción a actualizar
     * @param dto Datos actualizados
     * @return ResponseEntity con TransaccionResponseDTO actualizada (200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> actualizarTransaccion(
            @PathVariable Long id,
            @Valid @RequestBody TransaccionRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        TransaccionResponseDTO transaccion = transaccionService.actualizarTransaccion(id, dto, usuarioId);
        return ResponseEntity.ok(transaccion);
    }

    // ==================== ELIMINAR TRANSACCIÓN ====================

    /**
     * Elimina una transacción.
     *
     * FLUJO:
     * Cliente → DELETE /api/transacciones/{id} (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * @param id ID de la transacción a eliminar
     * @return ResponseEntity sin contenido (204 No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTransaccion(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        transaccionService.eliminarTransaccion(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ==================== LISTAR POR TIPO ====================

    /**
     * Lista transacciones del usuario por tipo.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/tipo/INGRESO (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * @param tipo INGRESO o EGRESO
     * @return ResponseEntity con lista de transacciones del tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransaccionResponseDTO>> listarPorTipo(@PathVariable String tipo) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        TipoTransaccion tipoTransaccion = TipoTransaccion.valueOf(tipo.toUpperCase());
        List<TransaccionResponseDTO> transacciones = transaccionService.listarPorTipo(usuarioId, tipoTransaccion);
        return ResponseEntity.ok(transacciones);
    }

    // ==================== LISTAR POR CATEGORÍA ====================

    /**
     * Lista transacciones del usuario por categoría.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/categoria/1 (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * @param categoriaId ID de la categoría
     * @return ResponseEntity con lista de transacciones de la categoría
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<TransaccionResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<TransaccionResponseDTO> transacciones = transaccionService.listarPorCategoria(usuarioId, categoriaId);
        return ResponseEntity.ok(transacciones);
    }

    // ==================== LISTAR POR RANGO DE FECHAS ====================

    /**
     * Lista transacciones del usuario en un rango de fechas.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/rango?inicio=2025-01-01&fin=2025-01-31 (con
     * JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * @param fechaInicio Fecha inicial (formato: yyyy-MM-dd)
     * @param fechaFin    Fecha final (formato: yyyy-MM-dd)
     * @return ResponseEntity con lista de transacciones en el rango
     */
    @GetMapping("/rango")
    public ResponseEntity<List<TransaccionResponseDTO>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        List<TransaccionResponseDTO> transacciones = transaccionService.listarPorRangoFechas(
                usuarioId, fechaInicio, fechaFin);
        return ResponseEntity.ok(transacciones);
    }

    // ==================== CALCULAR BALANCE ====================

    /**
     * Calcula el balance actual del usuario.
     * Balance = Total Ingresos - Total Egresos
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/balance (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * RESPONSE (200 OK):
     * {
     * "balance": 125000.50
     * }
     *
     * @return ResponseEntity con el balance
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> calcularBalance() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        BigDecimal balance = transaccionService.calcularBalance(usuarioId);
        return ResponseEntity.ok(new BalanceResponse(balance));
    }

    // ==================== RESUMEN FINANCIERO ====================

    /**
     * Obtiene un resumen financiero completo del usuario.
     *
     * FLUJO:
     * Cliente → GET /api/transacciones/resumen (con JWT)
     * → [ESTE MÉTODO] → TransaccionService → BD
     *
     * RESPONSE (200 OK):
     * {
     * "totalIngresos": 500000,
     * "totalEgresos": 374999.50,
     * "balance": 125000.50,
     * "cantidadTransacciones": 45
     * }
     *
     * @return ResponseEntity con el resumen financiero
     */
    @GetMapping("/resumen")
    public ResponseEntity<TransaccionService.ResumenFinancieroDTO> obtenerResumen() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        TransaccionService.ResumenFinancieroDTO resumen = transaccionService.obtenerResumenFinanciero(usuarioId);
        return ResponseEntity.ok(resumen);
    }

    // ==================== UTILIDADES ====================

    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext.
     * El ID se extrae directamente del Authentication (viene del JWT).
     *
     * @return ID del usuario autenticado
     */
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

    // ==================== DTOs INTERNOS ====================

    /**
     * DTO interno para respuesta de balance.
     */
    public record BalanceResponse(BigDecimal balance) {
    }
}