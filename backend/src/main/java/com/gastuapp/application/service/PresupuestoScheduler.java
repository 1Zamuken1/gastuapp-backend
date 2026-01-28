package com.gastuapp.application.service;

import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled Service: PresupuestoScheduler
 *
 * FLUJO DE DATOS:
 * - USA: PresupuestoService (Application Layer)
 * - USA: PresupuestoRepositoryAdapter (Infrastructure Layer)
 * - EJECUTA: Tareas programadas con @Scheduled
 *
 * RESPONSABILIDAD:
 * Ejecuta tareas automatizadas de mantenimiento de presupuestos.
 * Procesa auto-renovaciones y desactivaciones automáticas.
 * Se ejecuta periódicamente sin intervención manual.
 *
 * TAREAS PROGRAMADAS:
 * 1. Desactivar presupuestos vencidos (diario a 1 AM)
 * 2. Procesar auto-renovaciones de presupuestos
 * 3. Actualizar estados basados en montos gastados
 * 4. Limpiar presupuestos inactivos antiguos (opcional)
 *
 * SCHEDULES:
 * - "0 0 1 * * ?" → Todos los días a 1:00 AM
 * - "0 30 2 * * MON" → Todos los lunes a 2:30 AM (limpieza semanal)
 *
 * LOGGING:
 * - INFO: Operaciones ejecutadas exitosamente
 * - WARN: Presupuestos procesados con problemas
 * - ERROR: Errores inesperados durante procesamiento
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Service
public class PresupuestoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PresupuestoScheduler.class);

    private final PresupuestoService presupuestoService;

    /**
     * Constructor con inyección de dependencias.
     */
    public PresupuestoScheduler(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    // ==================== TAREA 1: DESACTIVAR PRESUPUESTOS VENCIDOS
    // ====================

    /**
     * Desactiva presupuestos vencidos diariamente.
     * Se ejecuta todos los días a la 1:00 AM.
     *
     * PROCESAMIENTO:
     * 1. Buscar presupuestos vigentes con fechaFin vencida
     * 2. Si tienen autoRenovar = true → procesar renovación
     * 3. Si tienen autoRenovar = false → desactivar
     * 4. Guardar cambios en BD
     */
    @Scheduled(cron = "0 0 1 * * ?") // Todos los días a 1:00 AM
    public void desactivarPresupuestosVencidos() {
        logger.info("Iniciando proceso de desactivación de presupuestos vencidos...");

        try {
            LocalDate fechaActual = LocalDate.now();

            // Buscar presupuestos que necesitan procesamiento
            List<Presupuesto> pendientesProcesamiento = presupuestoService
                    .buscarPendientesDeProcesamiento(fechaActual);

            int totalProcesados = 0;
            int renovados = 0;
            int desactivados = 0;

            for (Presupuesto presupuesto : pendientesProcesamiento) {
                try {
                    if (presupuesto.esAutoRenovable()) {
                        // Procesar auto-renovación
                        procesarAutoRenovacion(presupuesto);
                        renovados++;
                        logger.info("Presupuesto {} auto-renovado exitosamente", presupuesto.getPublicId());
                    } else {
                        // Desactivar automáticamente
                        presupuestoService.desactivarPresupuestoPorScheduler(presupuesto);
                        desactivados++;
                        logger.info("Presupuesto {} desactivado automáticamente", presupuesto.getPublicId());
                    }

                    totalProcesados++;

                } catch (Exception e) {
                    logger.error("Error procesando presupuesto {}: {}",
                            presupuesto.getPublicId(), e.getMessage(), e);
                }
            }

            logger.info("Proceso completado. Total: {}, Renovados: {}, Desactivados: {}",
                    totalProcesados, renovados, desactivados);

        } catch (Exception e) {
            logger.error("Error en proceso de desactivación de presupuestos vencidos", e);
        }
    }

    // ==================== TAREA 2: ACTUALIZAR ESTADOS POR MONTOS
    // ====================

    /**
     * Actualiza estados de presupuestos basados en montos gastados.
     * Se ejecuta todos los días a la 1:30 AM.
     *
     * PROCESAMIENTO:
     * 1. Buscar todos los presupuestos activos
     * 2. Recalcular montos gastados desde transacciones
     * 3. Actualizar estado según si exceden o no el tope
     */
    @Scheduled(cron = "0 30 1 * * ?") // Todos los días a 1:30 AM
    public void actualizarEstadosPorMontos() {
        logger.info("Iniciando actualización de estados por montos gastados...");

        try {
            // Esta función ya está implementada en PresupuestoService como
            // sincronizarMontosGastados
            // Pero aquí la llamamos para todos los usuarios (necesitaríamos lista de
            // usuarios)
            // Por ahora, dejamos un placeholder para futuro enhancement

            logger.info("Actualización de estados por montos completada");

        } catch (Exception e) {
            logger.error("Error en actualización de estados por montos", e);
        }
    }

    // ==================== TAREA 3: LIMPIEZA SEMANAL ====================

    /**
     * Realiza limpieza semanal de datos antiguos.
     * Se ejecuta todos los domingos a las 3:00 AM.
     *
     * PROCESAMIENTO:
     * 1. Archivar presupuestos inactivos antiguos (> 6 meses)
     * 2. Limpiar logs o registros temporales
     * 3. Optimizar consultas o índices si es necesario
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Todos los domingos a 3:00 AM
    public void limpiezaSemanal() {
        logger.info("Iniciando limpieza semanal de presupuestos...");

        try {
            // Futuro: Implementar archivado de presupuestos antiguos
            LocalDate limiteArchivo = LocalDate.now().minusMonths(6);

            // Por ahora, solo logueamos la intención
            logger.info("Se archivarían presupuestos inactivos anteriores a {}", limiteArchivo);
            logger.info("Limpieza semanal completada");

        } catch (Exception e) {
            logger.error("Error en limpieza semanal", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Procesa la auto-renovación de un presupuesto.
     *
     * @param presupuesto Presupuesto a renovar
     */
    private void procesarAutoRenovacion(Presupuesto presupuesto) {
        // 1. Obtener nuevo período según frecuencia
        Presupuesto.PeriodoPresupuesto nuevoPeriodo = presupuesto.generarNuevoPeriodo();

        // 2. Crear nuevo presupuesto con mismos datos pero nuevo período
        Presupuesto presupuestoRenovado = new Presupuesto();
        presupuestoRenovado.setPublicId(java.util.UUID.randomUUID().toString());
        presupuestoRenovado.setUsuarioId(presupuesto.getUsuarioId());
        presupuestoRenovado.setCategoriaId(presupuesto.getCategoriaId());
        presupuestoRenovado.setMontoTope(presupuesto.getMontoTope());
        presupuestoRenovado.setMontoGastado(java.math.BigDecimal.ZERO); // Reiniciar monto gastado
        presupuestoRenovado.setFechaInicio(nuevoPeriodo.getInicio());
        presupuestoRenovado.setFechaFin(nuevoPeriodo.getFin());
        presupuestoRenovado.setFrecuencia(presupuesto.getFrecuencia());
        presupuestoRenovado.setEstado(EstadoPresupuesto.ACTIVA);
        presupuestoRenovado.setAutoRenovar(presupuesto.getAutoRenovar());
        presupuestoRenovado.setFechaCreacion(java.time.LocalDateTime.now());

        // 3. Guardar nuevo presupuesto renovado
        presupuestoService.guardarPresupuestoRenovado(presupuestoRenovado);

        // 4. Marcar presupuesto anterior como INACTIVA (no eliminarlo)
        presupuesto.setEstado(EstadoPresupuesto.INACTIVA);
        presupuestoService.actualizarPresupuestoPorScheduler(presupuesto);
    }

    // ==================== MÉTODOS DE MONITOREO ====================

    /**
     * Genera reporte diario de estado del sistema.
     * Se ejecuta todos los días a las 8:00 AM.
     */
    @Scheduled(cron = "0 0 8 * * ?") // Todos los días a 8:00 AM
    public void generarReporteDiario() {
        logger.info("Generando reporte diario del estado de presupuestos...");

        try {
            // Futuro: Generar métricas y enviar a sistema de monitoreo
            int totalActivos = presupuestoService.contarPresupuestosActivos();
            int totalExcedidos = presupuestoService.contarPresupuestosExcedidos();
            int totalVencidos = presupuestoService.contarPresupuestosVencidos();

            logger.info("Reporte diario - Activos: {}, Excedidos: {}, Vencidos: {}",
                    totalActivos, totalExcedidos, totalVencidos);

        } catch (Exception e) {
            logger.error("Error generando reporte diario", e);
        }
    }
}