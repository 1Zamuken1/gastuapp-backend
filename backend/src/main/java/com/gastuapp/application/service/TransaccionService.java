package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.TransaccionRequestDTO;
import com.gastuapp.application.dto.response.TransaccionResponseDTO;
import com.gastuapp.application.mapper.TransaccionMapper;
import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import com.gastuapp.domain.model.transaccion.Transaccion;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import com.gastuapp.domain.port.transaccion.TransaccionRepositoryPort;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service: TransaccionService
 *
 * FLUJO DE DATOS:
 * - RECIBE: DTOs desde Controllers
 * - USA: TransaccionRepositoryPort (Domain Port)
 * - USA: CategoriaRepositoryPort (para validaciones)
 * - USA: UsuarioRepositoryPort (para validaciones)
 * - USA: TransaccionMapper (conversión DTO ↔ Domain)
 * - RETORNA: DTOs a Controllers
 *
 * RESPONSABILIDAD:
 * Orquesta los casos de uso relacionados con transacciones.
 * Valida reglas de negocio antes de persistir.
 * Coordina entre Domain, Mappers y Repositories.
 *
 * CASOS DE USO:
 * 1. Crear transacción
 * 2. Buscar transacción por ID
 * 3. Listar transacciones del usuario
 * 4. Listar por tipo (ingresos o egresos)
 * 5. Listar por categoría
 * 6. Listar por rango de fechas
 * 7. Actualizar transacción
 * 8. Eliminar transacción
 * 9. Calcular balance del usuario
 * 10. Obtener resumen financiero
 *
 * VALIDACIONES:
 * - Categoría existe y está disponible para el usuario
 * - Usuario existe
 * - Tipo de transacción compatible con tipo de categoría
 * - Validaciones del Domain (Transaccion.validar())
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Service
@Transactional
public class TransaccionService {

    private final TransaccionRepositoryPort transaccionRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final TransaccionMapper transaccionMapper;
    
    @Autowired
    private PresupuestoService presupuestoService;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente todos los repositories y mappers.
     */
    public TransaccionService(
            TransaccionRepositoryPort transaccionRepository,
            CategoriaRepositoryPort categoriaRepository,
            UsuarioRepositoryPort usuarioRepository,
            TransaccionMapper transaccionMapper) {
        this.transaccionRepository = transaccionRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccionMapper = transaccionMapper;
    }

    // ==================== CASO DE USO 1: CREAR TRANSACCIÓN ====================

    /**
     * Crea una nueva transacción.
     *
     * FLUJO:
     * POST /api/transacciones → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * VALIDACIONES:
     * - Usuario existe
     * - Categoría existe y está disponible para el usuario
     * - Tipo de transacción compatible con tipo de categoría
     * - Todas las validaciones del Domain
     *
     * @param dto       TransaccionRequestDTO del cliente
     * @param usuarioId ID del usuario autenticado (obtenido del JWT)
     * @return TransaccionResponseDTO con la transacción creada
     * @throws IllegalArgumentException si validaciones fallan
     */
    public TransaccionResponseDTO crearTransaccion(TransaccionRequestDTO dto, Long usuarioId) {
        // 1. Validar que el usuario exista
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con ID " + usuarioId + " no encontrado"));

        // 2. Validar que la categoría exista
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría con ID " + dto.getCategoriaId() + " no encontrada"));

        // 3. Validar que la categoría esté disponible para el usuario
        if (!categoria.getPredefinida() && !categoria.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "La categoría no está disponible para este usuario");
        }

        // 4. Validar que el tipo de transacción sea compatible con la categoría
        if (!categoria.permiteTransaccion(dto.getTipo())) {
            throw new IllegalArgumentException(
                    "La categoría '" + categoria.getNombre() +
                            "' no permite transacciones de tipo " + dto.getTipo());
        }

        // 5. Convertir DTO → Domain
        Transaccion transaccion = transaccionMapper.toTransaccion(dto, usuarioId);

        // 6. Validar reglas de negocio del Domain
        transaccion.validar();

        // 7. Guardar en BD
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        // 8. Actualizar montos gastados en presupuestos (INTEGRACIÓN)
        // Solo para transacciones de EGRESO
        if (transaccion.getTipo() == TipoTransaccion.EGRESO) {
            presupuestoService.actualizarMontoGastado(
                    usuarioId, transaccion.getCategoriaId(), transaccion.getMonto());
        }

        // 9. Convertir Domain → DTO Response
        return transaccionMapper.toResponseDTO(transaccionGuardada);
    }

    // ==================== CASO DE USO 2: BUSCAR POR ID ====================

    /**
     * Busca una transacción por su ID.
     * Solo si pertenece al usuario autenticado.
     *
     * FLUJO:
     * GET /api/transacciones/{id} → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * VALIDACIÓN:
     * La transacción debe pertenecer al usuario autenticado.
     *
     * @param id        ID de la transacción
     * @param usuarioId ID del usuario autenticado
     * @return TransaccionResponseDTO
     * @throws IllegalArgumentException si no existe o no pertenece al usuario
     */
    @Transactional(readOnly = true)
    public TransaccionResponseDTO buscarPorId(Long id, Long usuarioId) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transacción con ID " + id + " no encontrada"));

        // Validar que pertenezca al usuario
        if (!transaccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "No tienes permiso para ver esta transacción");
        }

        return transaccionMapper.toResponseDTO(transaccion);
    }

    // ==================== CASO DE USO 3: LISTAR TRANSACCIONES ====================

    /**
     * Lista todas las transacciones de un usuario.
     *
     * FLUJO:
     * GET /api/transacciones → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * @param usuarioId ID del usuario autenticado
     * @return Lista de TransaccionResponseDTO
     */
    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> listarTransacciones(Long usuarioId) {
        return transaccionRepository.findByUsuarioId(usuarioId).stream()
                .map(transaccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 4: LISTAR POR TIPO ====================

    /**
     * Lista transacciones de un usuario por tipo.
     *
     * FLUJO:
     * GET /api/transacciones/tipo/{tipo} → Controller → [ESTE MÉTODO] → Repository
     * → BD
     *
     * @param usuarioId ID del usuario autenticado
     * @param tipo      INGRESO o EGRESO
     * @return Lista de transacciones del tipo
     */
    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> listarPorTipo(Long usuarioId, TipoTransaccion tipo) {
        return transaccionRepository.findByUsuarioIdAndTipo(usuarioId, tipo).stream()
                .map(transaccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 5: LISTAR POR CATEGORÍA ====================

    /**
     * Lista transacciones de un usuario por categoría.
     *
     * @param usuarioId   ID del usuario autenticado
     * @param categoriaId ID de la categoría
     * @return Lista de transacciones de la categoría
     */
    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> listarPorCategoria(Long usuarioId, Long categoriaId) {
        // Validar que la categoría exista
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría con ID " + categoriaId + " no encontrada"));

        return transaccionRepository.findByUsuarioIdAndCategoriaId(usuarioId, categoriaId).stream()
                .map(transaccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 6: LISTAR POR FECHA ====================

    /**
     * Lista transacciones de un usuario en un rango de fechas.
     *
     * FLUJO:
     * GET /api/transacciones/rango?inicio=...&fin=... → Controller → [ESTE MÉTODO]
     *
     * @param usuarioId   ID del usuario autenticado
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin    Fecha final (inclusive)
     * @return Lista de transacciones en el rango
     */
    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> listarPorRangoFechas(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        // Validar que fechaInicio <= fechaFin
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final");
        }

        return transaccionRepository.findByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin).stream()
                .map(transaccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 7: ACTUALIZAR TRANSACCIÓN
    // ====================

    /**
     * Actualiza una transacción existente.
     *
     * FLUJO:
     * PUT /api/transacciones/{id} → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * VALIDACIONES:
     * - Transacción existe
     * - Pertenece al usuario autenticado
     * - Nueva categoría existe y está disponible
     * - Tipo compatible con categoría
     *
     * @param id        ID de la transacción a actualizar
     * @param dto       Datos actualizados
     * @param usuarioId ID del usuario autenticado
     * @return TransaccionResponseDTO actualizada
     */
    public TransaccionResponseDTO actualizarTransaccion(
            Long id,
            TransaccionRequestDTO dto,
            Long usuarioId) {

        // 1. Buscar transacción existente
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transacción con ID " + id + " no encontrada"));

        // 2. Validar que pertenezca al usuario
        if (!transaccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "No tienes permiso para editar esta transacción");
        }

        // 3. Validar categoría
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría con ID " + dto.getCategoriaId() + " no encontrada"));

        if (!categoria.getPredefinida() && !categoria.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "La categoría no está disponible para este usuario");
        }

        // 4. Validar compatibilidad tipo-categoría
        if (!categoria.permiteTransaccion(dto.getTipo())) {
            throw new IllegalArgumentException(
                    "La categoría '" + categoria.getNombre() +
                            "' no permite transacciones de tipo " + dto.getTipo());
        }

        // 5. Actualizar campos
        transaccion.setMonto(dto.getMonto());
        transaccion.setTipo(dto.getTipo());
        transaccion.setDescripcion(dto.getDescripcion());
        transaccion.setFecha(dto.getFecha());
        transaccion.setCategoriaId(dto.getCategoriaId());

        // 6. Validar reglas de negocio
        transaccion.validar();

        // 7. Guardar cambios
        Transaccion transaccionActualizada = transaccionRepository.save(transaccion);

        // 8. Actualizar montos gastados en presupuestos (INTEGRACIÓN)
        // Calcular diferencia y actualizar según el cambio
        BigDecimal montoAnterior = transaccion.getMonto(); // Monto original antes del cambio
        BigDecimal montoNuevo = dto.getMonto(); // Monto nuevo
        
        if (transaccion.getTipo() == TipoTransaccion.EGRESO) {
            BigDecimal diferencia = montoNuevo.subtract(montoAnterior);
            presupuestoService.actualizarMontoGastado(
                    usuarioId, transaccion.getCategoriaId(), diferencia);
        }

        // 9. Convertir a DTO
        return transaccionMapper.toResponseDTO(transaccionActualizada);
    }

    // ==================== CASO DE USO 8: ELIMINAR TRANSACCIÓN ====================

    /**
     * Elimina una transacción.
     *
     * FLUJO:
     * DELETE /api/transacciones/{id} → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * VALIDACIÓN:
     * La transacción debe pertenecer al usuario autenticado.
     *
     * @param id        ID de la transacción a eliminar
     * @param usuarioId ID del usuario autenticado
     * @throws IllegalArgumentException si no existe o no pertenece al usuario
     */
    public void eliminarTransaccion(Long id, Long usuarioId) {
        // Verificar que exista y pertenezca al usuario
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transacción con ID " + id + " no encontrada"));

        if (!transaccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "No tienes permiso para eliminar esta transacción");
        }

        // Eliminar (después de actualizar presupuestos)
        // 8. Actualizar montos gastados en presupuestos (INTEGRACIÓN)
        // Solo para transacciones de EGRESO (restar el monto eliminado)
        if (transaccion.getTipo() == TipoTransaccion.EGRESO) {
            presupuestoService.actualizarMontoGastado(
                    usuarioId, transaccion.getCategoriaId(), transaccion.getMonto().negate());
        }

        // 9. Eliminar
        transaccionRepository.deleteById(id);
    }

    // ==================== CASO DE USO 9: CALCULAR BALANCE ====================

    /**
     * Calcula el balance actual de un usuario.
     * Balance = Total Ingresos - Total Egresos
     *
     * FLUJO:
     * GET /api/transacciones/balance → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * @param usuarioId ID del usuario autenticado
     * @return Balance del usuario
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularBalance(Long usuarioId) {
        return transaccionRepository.calcularBalance(usuarioId);
    }

    // ==================== CASO DE USO 10: RESUMEN FINANCIERO ====================

    /**
     * Obtiene un resumen financiero del usuario.
     * Incluye: total ingresos, total egresos, balance, cantidad de transacciones.
     *
     * @param usuarioId ID del usuario autenticado
     * @return Map con el resumen
     */
    @Transactional(readOnly = true)
    public ResumenFinancieroDTO obtenerResumenFinanciero(Long usuarioId) {
        BigDecimal totalIngresos = transaccionRepository.sumByUsuarioIdAndTipo(
                usuarioId, TipoTransaccion.INGRESO);

        BigDecimal totalEgresos = transaccionRepository.sumByUsuarioIdAndTipo(
                usuarioId, TipoTransaccion.EGRESO);

        BigDecimal balance = transaccionRepository.calcularBalance(usuarioId);

        long cantidadTransacciones = transaccionRepository.countByUsuarioId(usuarioId);

        return new ResumenFinancieroDTO(
                totalIngresos,
                totalEgresos,
                balance,
                cantidadTransacciones);
    }

    // ==================== DTO INTERNO PARA RESUMEN ====================

    /**
     * DTO interno para resumen financiero.
     * Se puede mover a un archivo separado si crece.
     */
    public record ResumenFinancieroDTO(
            BigDecimal totalIngresos,
            BigDecimal totalEgresos,
            BigDecimal balance,
            long cantidadTransacciones) {
    }
}