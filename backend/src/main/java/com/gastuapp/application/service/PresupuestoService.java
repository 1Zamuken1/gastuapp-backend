package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.ActualizarPresupuestoRequestDTO;
import com.gastuapp.application.dto.request.CrearPresupuestoRequestDTO;
import com.gastuapp.application.dto.response.PresupuestoResponseDTO;
import com.gastuapp.application.mapper.PresupuestoMapper;
import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import com.gastuapp.domain.port.planificacion.PresupuestoRepositoryPort;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import com.gastuapp.domain.port.transaccion.TransaccionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application Service: PresupuestoService
 *
 * FLUJO DE DATOS:
 * - RECIBE: DTOs desde Controllers
 * - USA: PresupuestoRepositoryPort (Domain Port)
 * - USA: CategoriaRepositoryPort (para validaciones)
 * - USA: UsuarioRepositoryPort (para validaciones)
 * - USA: TransaccionRepositoryPort (para cálculo de montos gastados)
 * - USA: PresupuestoMapper (conversión DTO ↔ Domain)
 * - RETORNA: DTOs a Controllers
 *
 * RESPONSABILIDAD:
 * Orquesta los casos de uso relacionados con planificaciones de presupuesto.
 * Valida reglas de negocio antes de persistir.
 * Coordina entre Domain, Mappers y Repositories.
 * Actualiza automáticamente los montos gastados cuando se modifican
 * transacciones.
 *
 * CASOS DE USO:
 * 1. Crear presupuesto
 * 2. Listar presupuestos del usuario
 * 3. Buscar presupuesto por ID
 * 4. Actualizar presupuesto
 * 5. Desactivar presupuesto
 * 6. Listar presupuestos activos
 * 7. Listar presupuestos cercanos a exceder
 * 8. Actualizar montos gastados (integración con transacciones)
 * 9. Procesar auto-renovación
 * 10. Desactivar presupuestos vencidos
 *
 * VALIDACIONES:
 * - Usuario existe
 * - Categoría existe y es de tipo EGRESO
 * - Solo un presupuesto ACTIVO por (usuario + categoría)
 * - Fechas válidas (fin > inicio)
 * - Monto tope positivo
 * - Validaciones del Domain (Presupuesto.validar())
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Service
@Transactional
public class PresupuestoService {

    private final PresupuestoRepositoryPort presupuestoRepository;
    // private final CategoriaRepositoryPort categoriaRepository; // unused
    private final UsuarioRepositoryPort usuarioRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final PresupuestoMapper presupuestoMapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente todos los repositories y mappers.
     */
    public PresupuestoService(
            PresupuestoRepositoryPort presupuestoRepository,
            CategoriaRepositoryPort categoriaRepository,
            UsuarioRepositoryPort usuarioRepository,
            TransaccionRepositoryPort transaccionRepository,
            PresupuestoMapper presupuestoMapper) {
        this.presupuestoRepository = presupuestoRepository;
        // this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccionRepository = transaccionRepository;
        this.presupuestoMapper = presupuestoMapper;
    }

    // ==================== CASO DE USO 1: CREAR PRESUPUESTO ====================

    /**
     * Crea una nueva planificación de presupuesto.
     *
     * FLUJO:
     * POST /api/presupuestos-planificaciones → Controller → [ESTE MÉTODO] →
     * Repository → BD
     *
     * VALIDACIONES:
     * - Usuario existe
     * - Categoría existe y es de tipo EGRESO
     * - No existe otro presupuesto ACTIVO para la misma categoría
     * - Todas las validaciones del Domain
     *
     * @param dto       CrearPresupuestoRequestDTO del cliente
     * @param usuarioId ID del usuario autenticado (obtenido del JWT)
     * @return PresupuestoResponseDTO con el presupuesto creado
     * @throws IllegalArgumentException si validaciones fallan
     */
    public PresupuestoResponseDTO crearPresupuesto(CrearPresupuestoRequestDTO dto, Long usuarioId) {
        // Validar request
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del presupuesto son obligatorios");
        }
        dto.validarFechas();

        // Validar usuario existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        // Validar categoría y que sea de tipo EGRESO
        presupuestoMapper.validarCategoriaEgreso(dto.getCategoriaId());

        // Validar unicidad: solo un presupuesto ACTIVO por (usuario + categoría)
        if (presupuestoRepository.existsByUsuarioIdAndCategoriaIdAndEstado(
                usuarioId, dto.getCategoriaId(), EstadoPresupuesto.ACTIVA)) {
            throw new IllegalArgumentException(
                    "Ya existe un presupuesto activo para esta categoría. Debe desactivarlo primero.");
        }

        // Convertir DTO a Domain
        Presupuesto presupuesto = presupuestoMapper.toPresupuesto(dto, usuarioId);
        presupuesto.setPublicId(UUID.randomUUID().toString());

        // Validar reglas de negocio del Domain
        presupuesto.validar();

        // Calcular monto gastado inicial (transacciones existentes en el período)
        presupuesto.setMontoGastado(calcularMontoGastadoPeriodo(
                usuarioId, dto.getCategoriaId(), dto.getFechaInicio(), dto.getFechaFin()));

        // Verificar si ya está excedido
        if (presupuesto.estaExcedido()) {
            presupuesto.setEstado(EstadoPresupuesto.EXCEDIDA);
        }

        // Guardar en BD
        Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

        // Retornar DTO de respuesta
        return presupuestoMapper.toResponseDTO(presupuestoGuardado);
    }

    // ==================== CASO DE USO 2: LISTAR PRESUPUESTOS DEL USUARIO
    // ====================

    /**
     * Lista todas las planificaciones del usuario autenticado.
     *
     * @param usuarioId ID del usuario autenticado
     * @return Lista de PresupuestoResponseDTO
     */
    public List<PresupuestoResponseDTO> listarPresupuestosPorUsuario(Long usuarioId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByUsuarioId(usuarioId);
        return presupuestos.stream()
                .map(presupuestoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 3: BUSCAR POR ID ====================

    /**
     * Busca un presupuesto por su ID público.
     * Valida que pertenezca al usuario autenticado.
     *
     * @param publicId  ID público del presupuesto
     * @param usuarioId ID del usuario autenticado
     * @return PresupuestoResponseDTO encontrado
     * @throws IllegalArgumentException si no existe o no pertenece al usuario
     */
    public PresupuestoResponseDTO buscarPresupuestoPorPublicId(String publicId, Long usuarioId) {
        Presupuesto presupuesto = presupuestoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (!presupuesto.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene acceso a este presupuesto");
        }

        return presupuestoMapper.toResponseDTO(presupuesto);
    }

    // ==================== CASO DE USO 4: ACTUALIZAR PRESUPUESTO
    // ====================

    /**
     * Actualiza una planificación de presupuesto existente.
     *
     * @param publicId  ID público del presupuesto a actualizar
     * @param dto       ActualizarPresupuestoRequestDTO con los cambios
     * @param usuarioId ID del usuario autenticado
     * @return PresupuestoResponseDTO actualizado
     */
    public PresupuestoResponseDTO actualizarPresupuesto(
            String publicId, ActualizarPresupuestoRequestDTO dto, Long usuarioId) {

        // Validar request
        if (dto == null || !dto.tieneDatosParaActualizar()) {
            throw new IllegalArgumentException("No hay datos para actualizar");
        }
        dto.validarFechas();

        // Buscar presupuesto existente
        Presupuesto presupuestoExistente = presupuestoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        // Validar que pertenezca al usuario
        if (!presupuestoExistente.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene acceso a este presupuesto");
        }

        // Aplicar actualizaciones
        presupuestoMapper.actualizarPresupuesto(presupuestoExistente, dto);

        // Validar reglas de negocio
        presupuestoExistente.validar();

        // Recalcular estado basado en monto gastado
        if (presupuestoExistente.estaExcedido()) {
            presupuestoExistente.setEstado(EstadoPresupuesto.EXCEDIDA);
        } else if (presupuestoExistente.getEstado() == EstadoPresupuesto.EXCEDIDA) {
            presupuestoExistente.setEstado(EstadoPresupuesto.ACTIVA);
        }

        // Guardar cambios
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuestoExistente);

        return presupuestoMapper.toResponseDTO(presupuestoActualizado);
    }

    // ==================== CASO DE USO 5: DESACTIVAR PRESUPUESTO
    // ====================

    /**
     * Desactiva una planificación de presupuesto.
     * No elimina el registro, solo cambia el estado a INACTIVA.
     *
     * @param publicId  ID público del presupuesto a desactivar
     * @param usuarioId ID del usuario autenticado
     * @return PresupuestoResponseDTO desactivado
     */
    public PresupuestoResponseDTO desactivarPresupuesto(String publicId, Long usuarioId) {
        Presupuesto presupuesto = presupuestoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (!presupuesto.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene acceso a este presupuesto");
        }

        presupuesto.desactivar();
        Presupuesto presupuestoDesactivado = presupuestoRepository.save(presupuesto);

        return presupuestoMapper.toResponseDTO(presupuestoDesactivado);
    }

    // ==================== CASO DE USO 6: LISTAR PRESUPUESTOS ACTIVOS
    // ====================

    /**
     * Lista solo los presupuestos activos del usuario.
     * Útil para el dashboard.
     *
     * @param usuarioId ID del usuario autenticado
     * @return Lista de PresupuestoResponseDTO activos
     */
    public List<PresupuestoResponseDTO> listarPresupuestosActivos(Long usuarioId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByUsuarioIdAndEstado(
                usuarioId, EstadoPresupuesto.ACTIVA);
        return presupuestos.stream()
                .map(presupuestoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 7: LISTAR PRESUPUESTOS CERCANOS A EXCEDER
    // ====================

    /**
     * Lista presupuestos que están cerca de exceder el tope.
     * Por defecto, aquellos con más del 80% de utilización.
     *
     * @param usuarioId        ID del usuario autenticado
     * @param porcentajeUmbral Porcentaje umbral (ej: 80.0 para 80%)
     * @return Lista de PresupuestoResponseDTO cercanos a exceder
     */
    public List<PresupuestoResponseDTO> listarPresupuestosPorExceder(Long usuarioId, double porcentajeUmbral) {
        List<Presupuesto> presupuestos = presupuestoRepository.findPorExceder(usuarioId, porcentajeUmbral);
        return presupuestos.stream()
                .map(presupuestoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== CASO DE USO 8: ACTUALIZACIÓN AUTOMÁTICA DE MONTOS
    // (INTEGRACIÓN) ====================

    /**
     * Actualiza el monto gastado de un presupuesto cuando se crea/actualiza/elimina
     * una transacción.
     * Este método es llamado por TransaccionService.
     *
     * @param usuarioId   ID del usuario de la transacción
     * @param categoriaId ID de la categoría de la transacción
     * @param monto       Cambio de monto (positivo para agregar, negativo para
     *                    restar)
     */
    public void actualizarMontoGastado(Long usuarioId, Long categoriaId, java.math.BigDecimal monto) {
        // Buscar presupuesto activo para esta categoría
        presupuestoRepository.findByUsuarioIdAndCategoriaIdAndEstado(
                usuarioId, categoriaId, EstadoPresupuesto.ACTIVA)
                .ifPresent(presupuesto -> {
                    if (monto.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        presupuesto.agregarGasto(monto);
                    } else {
                        presupuesto.restarGasto(monto.abs());
                    }
                    presupuestoRepository.save(presupuesto);
                });
    }

    /**
     * Recalcula todos los montos gastados de un usuario.
     * Útil para sincronizar datos o correcciones.
     *
     * @param usuarioId ID del usuario a sincronizar
     */
    public void sincronizarMontosGastados(Long usuarioId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByUsuarioId(usuarioId);

        for (Presupuesto presupuesto : presupuestos) {
            java.math.BigDecimal montoCalculado = calcularMontoGastadoPeriodo(
                    usuarioId, presupuesto.getCategoriaId(),
                    presupuesto.getFechaInicio(), presupuesto.getFechaFin());
            presupuesto.setMontoGastado(montoCalculado);

            // Actualizar estado según nuevo monto
            if (presupuesto.estaExcedido()) {
                presupuesto.setEstado(EstadoPresupuesto.EXCEDIDA);
            } else if (presupuesto.getEstado() == EstadoPresupuesto.EXCEDIDA) {
                presupuesto.setEstado(EstadoPresupuesto.ACTIVA);
            }

            presupuestoRepository.save(presupuesto);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Calcula el monto gastado para una categoría en un período específico.
     *
     * @param usuarioId   ID del usuario
     * @param categoriaId ID de la categoría
     * @param fechaInicio Fecha inicio del período
     * @param fechaFin    Fecha fin del período
     * @return Monto total gastado
     */
    private java.math.BigDecimal calcularMontoGastadoPeriodo(
            Long usuarioId, Long categoriaId, LocalDate fechaInicio, LocalDate fechaFin) {

        return transaccionRepository
                .findByUsuarioIdAndCategoriaIdAndFechaBetween(usuarioId, categoriaId, fechaInicio, fechaFin)
                .stream()
                .filter(transaccion -> transaccion.getTipo().name().equals("EGRESO"))
                .map(transaccion -> transaccion.getMonto())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // ==================== MÉTODOS PARA SCHEDULER ====================

    /**
     * Busca presupuestos que necesitan procesamiento (vencidos).
     * Usado por PresupuestoScheduler.
     *
     * @param fecha Fecha de referencia
     * @return Lista de presupuestos pendientes
     */
    public List<Presupuesto> buscarPendientesDeProcesamiento(LocalDate fecha) {
        return presupuestoRepository.findPendientesDeRenovacion(fecha);
    }

    /**
     * Desactiva un presupuesto (usado por scheduler).
     *
     * @param presupuesto Presupuesto a desactivar
     */
    @Transactional
    public void desactivarPresupuestoPorScheduler(Presupuesto presupuesto) {
        presupuesto.desactivar();
        presupuestoRepository.save(presupuesto);
    }

    /**
     * Guarda un presupuesto renovado (usado por scheduler).
     *
     * @param presupuesto Presupuesto renovado a guardar
     */
    @Transactional
    public void guardarPresupuestoRenovado(Presupuesto presupuesto) {
        presupuesto.validar();
        presupuestoRepository.save(presupuesto);
    }

    /**
     * Actualiza un presupuesto (usado por scheduler).
     *
     * @param presupuesto Presupuesto a actualizar
     */
    @Transactional
    public void actualizarPresupuestoPorScheduler(Presupuesto presupuesto) {
        presupuestoRepository.save(presupuesto);
    }

    /**
     * Cuenta presupuestos activos.
     *
     * @return Cantidad de presupuestos activos
     */
    @Transactional(readOnly = true)
    public int contarPresupuestosActivos() {
        return (int) presupuestoRepository.countByEstado(EstadoPresupuesto.ACTIVA);
    }

    /**
     * Cuenta presupuestos excedidos.
     *
     * @return Cantidad de presupuestos excedidos
     */
    @Transactional(readOnly = true)
    public int contarPresupuestosExcedidos() {
        return (int) presupuestoRepository.countByEstado(EstadoPresupuesto.EXCEDIDA);
    }

    /**
     * Cuenta presupuestos vencidos.
     *
     * @return Cantidad de presupuestos vencidos
     */
    @Transactional(readOnly = true)
    public int contarPresupuestosVencidos() {
        LocalDate hoy = LocalDate.now();
        return presupuestoRepository.findByFechaFinBefore(hoy)
                .stream()
                .mapToInt(p -> 1)
                .sum();
    }
}