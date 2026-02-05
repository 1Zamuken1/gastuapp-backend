import { Component, EventEmitter, Input, Output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';

// Core
import { PlanificacionService } from '../../../core/services/planificacion.service';
import {
  Planificacion,
  EstadoPlanificacion,
  PlanificacionUtils,
} from '../../../core/models/planificacion.model';

@Component({
  selector: 'app-planificacion-modal',
  standalone: true,
  imports: [CommonModule, DialogModule, ButtonModule],
  templateUrl: './planificacion-modal.component.html',
  styleUrl: './planificacion-modal.component.scss',
})
export class PlanificacionModalComponent {
  @Input() visible = false;
  @Input() modalType: 'delete' | 'deactivate' | 'confirm' = 'confirm';
  @Input() planificacion: Planificacion | null = null;
  @Input() title = '';
  @Input() message = '';
  @Input() confirmButtonLabel = 'Confirmar';
  @Input() confirmButtonClass = 'p-button-danger';

  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() confirmed = new EventEmitter<void>();

  private planificacionService = inject(PlanificacionService);

  loading = signal(false);

  get modalTitle(): string {
    if (this.title) return this.title;

    switch (this.modalType) {
      case 'delete':
        return 'Eliminar Presupuesto';
      case 'deactivate':
        return 'Desactivar Presupuesto';
      case 'confirm':
        return 'Confirmar Acción';
      default:
        return 'Confirmar';
    }
  }

  get modalMessage(): string {
    if (this.message) return this.message;

    if (!this.planificacion) return '';

    switch (this.modalType) {
      case 'delete':
        return `¿Estás seguro que deseas eliminar el presupuesto "${this.planificacion.nombrePersonalizado || this.planificacion.categoriaNombre}"? Esta acción no se puede deshacer.`;
      case 'deactivate':
        return `¿Estás seguro que deseas desactivar el presupuesto "${this.planificacion.nombrePersonalizado || this.planificacion.categoriaNombre}"? Podrás volver a activarlo más tarde.`;
      case 'confirm':
        return `¿Deseas realizar esta acción sobre el presupuesto "${this.planificacion.nombrePersonalizado || this.planificacion.categoriaNombre}"?`;
      default:
        return '¿Deseas continuar con esta acción?';
    }
  }

  get getConfirmButtonLabel(): string {
    return this.confirmButtonLabel;
  }

  get getConfirmButtonClass(): string {
    return this.confirmButtonClass;
  }

  get getIconClass(): string {
    switch (this.modalType) {
      case 'delete':
        return 'pi pi-trash text-red-500';
      case 'deactivate':
        return 'pi pi-pause-circle text-amber-500';
      case 'confirm':
        return 'pi pi-question-circle text-blue-500';
      default:
        return 'pi pi-info-circle text-blue-500';
    }
  }

  closeModal(): void {
    this.visible = false;
    this.visibleChange.emit(false);
  }

  // Called when PrimeNG dialog changes visible state (e.g., clicking mask)
  onVisibleChange(isVisible: boolean): void {
    if (!isVisible) {
      this.visibleChange.emit(false);
    }
  }

  onConfirm(): void {
    if (!this.planificacion) return;

    this.loading.set(true);

    switch (this.modalType) {
      case 'delete':
        this.planificacionService.desactivarPlanificacion(this.planificacion.publicId).subscribe({
          next: () => {
            this.loading.set(false);
            this.confirmed.emit();
            this.closeModal();
          },
          error: (err) => {
            console.error('Error eliminando presupuesto', err);
            this.loading.set(false);
          },
        });
        break;

      case 'deactivate':
        this.planificacionService.desactivarPlanificacion(this.planificacion.publicId).subscribe({
          next: () => {
            this.loading.set(false);
            this.confirmed.emit();
            this.closeModal();
          },
          error: (err) => {
            console.error('Error desactivando presupuesto', err);
            this.loading.set(false);
          },
        });
        break;

      case 'confirm':
      default:
        this.loading.set(false);
        this.confirmed.emit();
        this.closeModal();
        break;
    }
  }

  // Métodos de utilidad usando PlanificacionUtils
  getEstadoColor(estado: EstadoPlanificacion): string {
    return PlanificacionUtils.getEstadoColor(estado);
  }

  getEstadoIcono(estado: EstadoPlanificacion): string {
    return PlanificacionUtils.getEstadoIcono(estado);
  }

  getEstadoTexto(estado: EstadoPlanificacion): string {
    return PlanificacionUtils.getEstadoTexto(estado);
  }

  getFrecuenciaTexto(frecuencia: string): string {
    return PlanificacionUtils.getFrecuenciaTexto(frecuencia as any);
  }

  formatearMonto(monto: number): string {
    return PlanificacionUtils.formatearMonto(monto);
  }

  calcularProgreso(montoGastado: number, montoTope: number): number {
    return PlanificacionUtils.calcularProgreso(montoGastado, montoTope);
  }

  calcularDiasRestantes(fechaFin: string): number {
    return PlanificacionUtils.calcularDiasRestantes(fechaFin);
  }

  getProgressColor(porcentaje: number): string {
    return PlanificacionUtils.getPorcentajeColor(porcentaje);
  }
}
