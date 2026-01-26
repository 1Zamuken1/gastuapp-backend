package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.ahorro.AhorroRequestDTO;
import com.gastuapp.application.dto.request.ahorro.MetaAhorroRequestDTO;
import com.gastuapp.application.dto.response.ahorro.AhorroResponseDTO;
import com.gastuapp.application.dto.response.ahorro.CuotaAhorroResponseDTO;
import com.gastuapp.application.dto.response.ahorro.MetaAhorroResponseDTO;
import com.gastuapp.application.mapper.AhorroMapper;
import com.gastuapp.application.mapper.MetaAhorroMapper;
import com.gastuapp.domain.model.ahorro.Ahorro;
import com.gastuapp.domain.model.ahorro.CuotaAhorro;
import com.gastuapp.domain.model.ahorro.FrecuenciaAhorro;
import com.gastuapp.domain.model.ahorro.MetaAhorro;
import com.gastuapp.domain.port.ahorro.AhorroRepositoryPort;
import com.gastuapp.domain.port.ahorro.CuotaAhorroRepositoryPort;
import com.gastuapp.domain.port.ahorro.MetaAhorroRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para Metas de Ahorro y Abonos.
 * 
 * <p>
 * Implementa la lógica de negocio para:
 * </p>
 * <ul>
 * <li>Crear y gestionar Metas de Ahorro.</li>
 * <li>Registrar abonos y actualizar el progreso de la meta.</li>
 * <li>Calcular estados (COMPLETADA) automáticamente.</li>
 * </ul>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Service
public class AhorroService {

    private final MetaAhorroRepositoryPort metaRepository;
    private final AhorroRepositoryPort ahorroRepository;
    private final CuotaAhorroRepositoryPort cuotaRepository;
    private final MetaAhorroMapper metaMapper;
    private final AhorroMapper ahorroMapper;

    public AhorroService(
            MetaAhorroRepositoryPort metaRepository,
            AhorroRepositoryPort ahorroRepository,
            CuotaAhorroRepositoryPort cuotaRepository,
            MetaAhorroMapper metaMapper,
            AhorroMapper ahorroMapper) {
        this.metaRepository = metaRepository;
        this.ahorroRepository = ahorroRepository;
        this.cuotaRepository = cuotaRepository;
        this.metaMapper = metaMapper;
        this.ahorroMapper = ahorroMapper;
    }

    // ==================== METAS DE AHORRO ====================

    @Transactional
    public MetaAhorroResponseDTO crearMeta(MetaAhorroRequestDTO dto, Long usuarioId) {
        if (metaRepository.existsByNombreAndUsuarioId(dto.getNombre(), usuarioId)) {
            throw new IllegalArgumentException("Ya existe una meta con este nombre.");
        }

        MetaAhorro meta = metaMapper.toDomain(dto, usuarioId);

        // Asignar fecha inicio por defecto si no viene (aunque DTO debería manejarlo o
        // frontend)
        if (meta.getFechaInicio() == null) {
            meta.setFechaInicio(LocalDateTime.now());
        }

        MetaAhorro savedMeta = metaRepository.save(meta);

        // Generar cuotas si hay frecuencia definida
        if (savedMeta.getFrecuencia() != null && savedMeta.getFechaLimite() != null) {
            generarCuotas(savedMeta);
        }

        return metaMapper.toResponseDTO(savedMeta);
    }

    @Transactional(readOnly = true)
    public List<MetaAhorroResponseDTO> listarMetasPorUsuario(Long usuarioId) {
        return metaRepository.findAllByUsuarioId(usuarioId).stream()
                .map(metaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarMeta(Long id, Long usuarioId) {
        MetaAhorro meta = metaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para eliminar esta meta");
        }

        // Eliminar abonos asociados primero (Cascade manual)
        ahorroRepository.deleteAllByMetaAhorroId(id);

        metaRepository.deleteById(id);
    }

    @Transactional
    public MetaAhorroResponseDTO actualizarMeta(Long id, MetaAhorroRequestDTO dto, Long usuarioId) {
        MetaAhorro meta = metaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para editar esta meta");
        }

        BasicMetaAhorroUpdater.update(meta, dto);

        // Verificar si el nuevo objetivo ya se cumplió con el monto actual
        if (meta.getMontoActual().compareTo(meta.getMontoObjetivo()) >= 0) {
            meta.setEstado(MetaAhorro.EstadoMeta.COMPLETADA);
        } else if (meta.getEstado() == MetaAhorro.EstadoMeta.COMPLETADA) {
            // Si estaba completada pero ahora el objetivo es mayor, reactivar
            meta.setEstado(MetaAhorro.EstadoMeta.ACTIVA);
        }

        MetaAhorro updatedMeta = metaRepository.save(meta);
        return metaMapper.toResponseDTO(updatedMeta);
    }

    // Clase auxiliar interna para evitar duplicidad, o podríamos hacerlo manual set
    // por set
    private static class BasicMetaAhorroUpdater {
        static void update(MetaAhorro meta, MetaAhorroRequestDTO dto) {
            meta.setNombre(dto.getNombre());
            meta.setMontoObjetivo(dto.getMontoObjetivo());
            meta.setFechaLimite(dto.getFechaLimite());
            meta.setIcono(dto.getIcono());
            meta.setColor(dto.getColor());
        }
    }

    // ==================== ABONOS (AHORROS) ====================

    @Transactional
    public AhorroResponseDTO realizarAbono(AhorroRequestDTO dto, Long usuarioId) {
        // 1. Validar Meta
        MetaAhorro meta = metaRepository.findById(dto.getMetaAhorroId())
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos sobre esta meta");
        }

        if (meta.getEstado() == MetaAhorro.EstadoMeta.COMPLETADA ||
                meta.getEstado() == MetaAhorro.EstadoMeta.CANCELADA) {
            throw new IllegalStateException("No se pueden hacer abonos a una meta " + meta.getEstado());
        }

        // 2. Crear y Guardar Abono
        Ahorro ahorro = ahorroMapper.toDomain(dto, usuarioId);
        Ahorro savedAhorro = ahorroRepository.save(ahorro);

        // 3. Vincular con Cuota si aplica
        if (dto.getCuotaId() != null) {
            cuotaRepository.findById(dto.getCuotaId()).ifPresent(cuota -> {
                if (cuota.getMetaAhorroId().equals(meta.getId())) {
                    cuota.setEstado(CuotaAhorro.EstadoCuota.PAGADA);
                    cuota.setMontoEsperado(dto.getMonto()); // Actualizar al monto real pagado
                    cuota.setAhorroId(savedAhorro.getId());
                    cuotaRepository.save(cuota);
                }
            });
        }

        // 4. Actualizar Progreso de la Meta (Recalcula cuotas internamente)
        actualizarProgresoMeta(meta, dto.getMonto());

        return ahorroMapper.toResponseDTO(savedAhorro);
    }

    @Transactional(readOnly = true)
    public List<AhorroResponseDTO> listarAbonosPorMeta(Long metaId, Long usuarioId) {
        // Validar propiedad de la meta
        MetaAhorro meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos");
        }

        return ahorroRepository.findAllByMetaAhorroId(metaId).stream()
                .map(ahorroMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CuotaAhorroResponseDTO> listarCuotasPorMeta(Long metaId, Long usuarioId) {
        MetaAhorro meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos");
        }

        return cuotaRepository.findAllByMetaAhorroId(metaId).stream()
                .map(ahorroMapper::toCuotaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarAbono(Long id, Long usuarioId) {
        Ahorro abono = ahorroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado"));

        MetaAhorro meta = metaRepository.findById(abono.getMetaAhorroId())
                .orElseThrow(() -> new IllegalArgumentException("Meta asociada no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para eliminar este abono");
        }

        // Revertir estado de cuota si estaba vinculada
        if (id != null) {
            // Buscar cuota con este ahorroId (Necesitamos método en repositorio o iterar?)
            // Mejor añadimos método findByAhorroId en CuotaRepository
            // Por ahora, asumimos que no tenemos ese índex, pero podemos buscar en lista de
            // la meta?
            // findAllByMetaAhorroId ya existe.
            List<CuotaAhorro> cuotas = cuotaRepository.findAllByMetaAhorroId(meta.getId());
            cuotas.stream()
                    .filter(c -> id.equals(c.getAhorroId()))
                    .findFirst()
                    .ifPresent(c -> {
                        c.setAhorroId(null);
                        c.setEstado(CuotaAhorro.EstadoCuota.PENDIENTE);
                        cuotaRepository.save(c);
                    });
        }

        // Restar el monto eliminado
        actualizarProgresoMeta(meta, abono.getMonto().negate());

        ahorroRepository.deleteById(id);
    }

    @Transactional
    public AhorroResponseDTO actualizarAbono(Long id, AhorroRequestDTO dto, Long usuarioId) {
        Ahorro abono = ahorroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Abono no encontrado"));

        MetaAhorro meta = metaRepository.findById(abono.getMetaAhorroId())
                .orElseThrow(() -> new IllegalArgumentException("Meta asociada no encontrada"));

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para editar este abono");
        }

        // Calcular diferencia para ajustar el total de la meta
        BigDecimal montoAnterior = abono.getMonto();
        BigDecimal nuevoMonto = dto.getMonto();
        BigDecimal diferencia = nuevoMonto.subtract(montoAnterior);

        // Actualizar abono
        abono.setMonto(nuevoMonto);
        abono.setDescripcion(dto.getDescripcion());
        // No permitimos cambiar la meta padre por ahora para simplificar lógica

        Ahorro updatedAbono = ahorroRepository.save(abono);

        // Actualizar meta con la diferencia
        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            actualizarProgresoMeta(meta, diferencia);
        }

        return ahorroMapper.toResponseDTO(updatedAbono);
    }

    // ==================== LÓGICA PRIVADA ====================

    private void actualizarProgresoMeta(MetaAhorro meta, BigDecimal montoAbono) {
        BigDecimal nuevoMonto = meta.getMontoActual() != null ? meta.getMontoActual().add(montoAbono) : montoAbono;

        meta.setMontoActual(nuevoMonto);

        // Verificar si se completó
        if (nuevoMonto.compareTo(meta.getMontoObjetivo()) >= 0) {
            meta.setEstado(MetaAhorro.EstadoMeta.COMPLETADA);
        } else if (meta.getEstado() == MetaAhorro.EstadoMeta.PAUSADA) {
            // Si estaba pausada y recibe abono, se reactiva
            meta.setEstado(MetaAhorro.EstadoMeta.ACTIVA);
        }

        metaRepository.save(meta);

        // Recalcular cuotas futuras
        recalcularCuotasFuturas(meta);
    }

    private void recalcularCuotasFuturas(MetaAhorro meta) {
        List<CuotaAhorro> cuotas = cuotaRepository.findAllByMetaAhorroId(meta.getId());

        // Solo recalculamos las pendientes
        List<CuotaAhorro> pendientes = cuotas.stream()
                .filter(c -> c.getEstado() == CuotaAhorro.EstadoCuota.PENDIENTE)
                .collect(Collectors.toList());

        if (pendientes.isEmpty())
            return;

        BigDecimal saldoRestante = meta.getMontoObjetivo().subtract(meta.getMontoActual());

        // Si ya se pasó, el restante es 0
        if (saldoRestante.compareTo(BigDecimal.ZERO) < 0) {
            saldoRestante = BigDecimal.ZERO;
        }

        int cantidad = pendientes.size();
        // Usamos CEILING para asegurar cobertura
        BigDecimal nuevoMonto = saldoRestante.divide(BigDecimal.valueOf(cantidad), 0, RoundingMode.CEILING);

        pendientes.forEach(c -> c.setMontoEsperado(nuevoMonto));

        cuotaRepository.saveAll(pendientes);
    }

    private void generarCuotas(MetaAhorro meta) {
        List<CuotaAhorro> cuotas = new ArrayList<>();

        // Usamos LocalDate para cálculos de fechas sin hora
        LocalDate inicio = meta.getFechaInicio().toLocalDate();
        LocalDate fin = meta.getFechaLimite().toLocalDate();
        FrecuenciaAhorro frecuencia = meta.getFrecuencia();
        BigDecimal montoTotal = meta.getMontoObjetivo();

        List<LocalDate> fechas = calcularFechasCuotas(inicio, fin, frecuencia);

        if (fechas.isEmpty())
            return;

        int cantidadCuotas = fechas.size();
        BigDecimal montoPorCuota = montoTotal.divide(BigDecimal.valueOf(cantidadCuotas), 0, RoundingMode.CEILING);
        // CEILING para asegurar que suma >= objetivo (el usuario puede ajustar la
        // última o pagar menos)
        // O mejor, ajustamos la última cuota con el remanente exacto?
        // Por simplicidad, usamos división simple por ahora.

        for (int i = 0; i < cantidadCuotas; i++) {
            CuotaAhorro cuota = new CuotaAhorro();
            cuota.setMetaAhorroId(meta.getId());
            cuota.setNumeroCuota(i + 1);
            cuota.setFechaProgramada(fechas.get(i));
            cuota.setMontoEsperado(montoPorCuota);
            cuota.setEstado(CuotaAhorro.EstadoCuota.PENDIENTE);
            cuotas.add(cuota);
        }

        cuotaRepository.saveAll(cuotas);
    }

    private List<LocalDate> calcularFechasCuotas(LocalDate inicio, LocalDate fin, FrecuenciaAhorro frecuencia) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate fechaActual = inicio;

        // La primera cuota podría ser el día de inicio o un periodo después?
        // Asumimos que el usuario empieza a ahorrar DESDE el inicio.
        // Si es "Diario", hoy paga. Si es "Mensual", hoy paga (o a fin de mes?).
        // Regla: Primera cuota = Fecha Inicio.

        while (!fechaActual.isAfter(fin)) {
            fechas.add(fechaActual);

            switch (frecuencia) {
                case DIARIO -> fechaActual = fechaActual.plusDays(1);
                case SEMANAL -> fechaActual = fechaActual.plusWeeks(1);
                case QUINCENAL -> fechaActual = fechaActual.plusDays(15);
                case MENSUAL -> fechaActual = fechaActual.plusMonths(1);
                case TRIMESTRAL -> fechaActual = fechaActual.plusMonths(3);
                case SEMESTRAL -> fechaActual = fechaActual.plusMonths(6);
                case ANUAL -> fechaActual = fechaActual.plusYears(1);
            }
        }
        return fechas;
    }
}
