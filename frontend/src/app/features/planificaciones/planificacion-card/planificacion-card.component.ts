import { Component, Input, signal, computed, inject, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

// Core
import { Planificacion, PlanificacionUtils } from '../../../core/models/planificacion.model';
import { PlanificacionService } from '../../../core/services/planificacion.service';
import { ConfirmationService } from 'primeng/api';

/**
 * Component: PlanificacionCard
 *
 * FLUJO DE DATOS:
 * - RECIBE: Datos de presupuesto (Planificacion)
 * - RENDERIZA: UI card con información del presupuesto
 * - EMITE: Eventos de interacción
 *
 * RESPONSABILIDAD:
 * Card visual para mostrar información de una planificación de presupuesto.
 * Muestra progreso de gasto, estado y acciones rápidas.
 *
 * CARACTERÍSTICAS:
 * - Visualización de progreso con colores dinámicos
 * - Estados: ACTIVO, EXCEDIDO, INACTIVO
 * - Acciones rápidas: Editar, Desactivar, Eliminar
 * - Diseño consistente con MetaAhorroCard
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Component({
  selector: 'app-planificacion-card',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ProgressBarModule,
    TagModule,
    TooltipModule,
    ConfirmDialogModule,
  ],
  templateUrl: './planificacion-card.component.html',
  styleUrl: './planificacion-card.component.scss',
})
export class PlanificacionCardComponent {
  // ========= INPUTS =========
  @Input() presupuesto!: Planificacion;
  @Input() editable: boolean = true;
  @Input() mostrarAcciones: boolean = true;

  // ========= OUTPUTS =========
  @Output() onEditar = new EventEmitter<Planificacion>();
  @Output() onDesactivar = new EventEmitter<Planificacion>();
  @Output() onEliminar = new EventEmitter<Planificacion>();

  // ========= SERVICES =========
  private planificacionService = inject(PlanificacionService);
  private confirmationService = inject(ConfirmationService);

  // ========= SIGNALS =========
  loading = signal(false);

  // ========= COMPUTED =========

  // Estados con colores específicos para planificaciones
  estadoColor = computed(() => {
    return PlanificacionUtils.getEstadoColor(this.presupuesto.estado);
  });

  // Icono de frecuencia
  frecuenciaIcono = computed(() => {
    return PlanificacionUtils.getFrecuenciaIcono(this.presupuesto.frecuencia);
  });

  // Icono de estado
  estadoIcono = computed(() => {
    return PlanificacionUtils.getEstadoIcono(this.presupuesto.estado);
  });

  // Texto de estado
  estadoTexto = computed(() => {
    return PlanificacionUtils.getEstadoTexto(this.presupuesto.estado);
  });

  // Días restantes con color
  diasRestantesColor = computed(() => {
    return PlanificacionUtils.getDiasRestantesColor(this.presupuesto.diasRestantes);
  });

  // Porcentaje de utilización con color
  porcentajeColor = computed(() => {
    return PlanificacionUtils.getPorcentajeColor(this.presupuesto.porcentajeUtilizacion);
  });

  // Severity para p-tag (success, warn, danger, secondary)
  estadoSeverity(): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (this.presupuesto.estado) {
      case 'ACTIVA':
        return this.presupuesto.porcentajeUtilizacion > 80 ? 'warn' : 'success';
      case 'EXCEDIDA':
        return 'danger';
      case 'INACTIVA':
        return 'secondary';
      default:
        return 'secondary';
    }
  }

  // ========= MÉTODOS =========

  // Formatear moneda
  formatearMonto(monto: number): string {
    return PlanificacionUtils.formatearMonto(monto);
  }

  // Editar planificación
  editarPlanificacion(): void {
    this.onEditar.emit(this.presupuesto);
  }

  // Desactivar planificación
  desactivarPlanificacion(): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de desactivar la planificación "${this.presupuesto.categoriaNombre}"?`,
      header: 'Confirmar Desactivación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.loading.set(true);
        this.planificacionService.desactivarPlanificacion(this.presupuesto.publicId).subscribe({
          next: () => {
            this.loading.set(false);
            this.onDesactivar.emit(this.presupuesto);
          },
          error: (err) => {
            console.error('Error desactivando presupuesto:', err);
            this.loading.set(false);
          },
        });
      },
      reject: () => {
        // Acción cancelada, no hacer nada
      },
    });
  }

  // Eliminar planificación
  eliminarPlanificacion(): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar permanentemente la planificación "${this.presupuesto.categoriaNombre}"? Esta acción no se puede deshacer.`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.loading.set(true);
        // Note: The service doesn't have an eliminar method, so we'll just emit the event
        this.onEliminar.emit(this.presupuesto);
        this.loading.set(false);
      },
      reject: () => {
        // Acción cancelada, no hacer nada
      },
    });
  }

  // Abrir calendario para ver período
  mostrarPeriodo(): void {
    const inicio = new Date(this.presupuesto.fechaInicio);
    const fin = new Date(this.presupuesto.fechaFin);

    this.confirmationService.confirm({
      message: `Período: ${inicio.toLocaleDateString('es-CO')} - ${fin.toLocaleDateString('es-CO')}`,
      header: 'Período de Presupuesto',
      icon: 'pi pi-calendar',
      accept: () => {
        // Cerrar mensaje, no acción adicional
      },
      reject: () => {
        // Acción cancelada
      },
    });
  }
}
