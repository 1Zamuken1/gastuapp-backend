import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

// Core
import { PlanificacionService } from '../../core/services/planificacion.service';
import { Planificacion, PlanificacionUtils } from '../../core/models/planificacion.model';
import { ThemeService } from '../../core/services/theme.service';

// Components
import { PlanificacionListComponent } from './planificacion-list/planificacion-list.component';
import { PlanificacionFormComponent } from './planificacion-form/planificacion-form.component';
import { PlanificacionModalComponent } from './planificacion-modal/planificacion-modal.component';

@Component({
  selector: 'app-planificaciones',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    ToastModule,
    ConfirmDialogModule,
    PlanificacionListComponent,
    PlanificacionFormComponent,
    PlanificacionModalComponent,
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './planificaciones.component.html',
  styleUrl: './planificaciones.component.scss',
})
export class PlanificacionesComponent implements OnInit {
  private planificacionService = inject(PlanificacionService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  private themeService = inject(ThemeService);

  loading = signal(true);
  planificaciones = signal<Planificacion[]>([]);
  planificacionSeleccionada = signal<Planificacion | null>(null);
  mostrarFormulario = false;
  mostrarModal = false;
  modalConfig = {
    type: 'delete' as 'delete' | 'deactivate' | 'confirm',
    planificacion: null as Planificacion | null,
    title: '',
    message: '',
    confirmButtonLabel: '',
    confirmButtonClass: '',
  };

  ngOnInit(): void {
    // Activar tema violeta para módulo de Planificaciones
    this.themeService.setPlanificacionTheme();
    this.cargarPlanificaciones();
  }

  cargarPlanificaciones(): void {
    this.loading.set(true);
    this.planificacionService.listarPlanificaciones().subscribe({
      next: (planificaciones: Planificacion[]) => {
        // Enriquecer planificaciones con propiedades calculadas
        const planificacionesEnriquecidas = planificaciones.map((planificacion) => ({
          ...planificacion,
          estadoColor: PlanificacionUtils.getEstadoColor(planificacion.estado),
          frecuenciaIcono: PlanificacionUtils.getFrecuenciaIcono(planificacion.frecuencia),
          frecuenciaTexto: PlanificacionUtils.getFrecuenciaTexto(planificacion.frecuencia),
          estadoTexto: PlanificacionUtils.getEstadoTexto(planificacion.estado),
          diasRestantesColor: PlanificacionUtils.getDiasRestantesColor(planificacion.diasRestantes),
          porcentajeColor: PlanificacionUtils.getPorcentajeColor(
            planificacion.porcentajeUtilizacion,
          ),
        }));

        this.planificaciones.set(planificacionesEnriquecidas);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando planificaciones', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las planificaciones',
        });
        this.loading.set(false);
      },
    });
  }

  // Event handlers
  onCrearPlanificacion(): void {
    this.planificacionSeleccionada.set(null);
    this.mostrarFormulario = true;
  }

  onPlanificacionCreada(): void {
    this.mostrarFormulario = false;
    this.cargarPlanificaciones();
    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Presupuesto creado correctamente',
    });
  }

  onPlanificacionActualizada(): void {
    this.mostrarFormulario = false;
    this.cargarPlanificaciones();
    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Presupuesto actualizado correctamente',
    });
  }

  onEditarPlanificacion(planificacion: Planificacion): void {
    this.planificacionSeleccionada.set(planificacion);
    this.mostrarFormulario = true;
  }

  onVerDetalle(planificacion: Planificacion): void {
    // Reutilizamos el formulario de edición para ver detalles
    this.onEditarPlanificacion(planificacion);
  }

  onDesactivarPlanificacion(planificacion: Planificacion): void {
    this.modalConfig = {
      type: 'deactivate',
      planificacion,
      title: '',
      message: '',
      confirmButtonLabel: 'Desactivar',
      confirmButtonClass: 'p-button-warning',
    };
    this.mostrarModal = true;
  }

  onEliminarPlanificacion(planificacion: Planificacion): void {
    this.modalConfig = {
      type: 'delete',
      planificacion,
      title: '',
      message: '',
      confirmButtonLabel: 'Eliminar',
      confirmButtonClass: 'p-button-danger',
    };
    this.mostrarModal = true;
  }

  onModalConfirmado(): void {
    this.mostrarModal = false;
    this.cargarPlanificaciones();

    const mensaje =
      this.modalConfig.type === 'delete'
        ? 'Presupuesto eliminado correctamente'
        : 'Presupuesto desactivado correctamente';

    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: mensaje,
    });
  }

  onCancelarFormulario(): void {
    this.mostrarFormulario = false;
  }

  onCancelarModal(): void {
    this.mostrarModal = false;
  }

  // Navegación
  irAlDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  // Getters para template
  get totalPlanificaciones(): number {
    return this.planificaciones().length;
  }

  get planificacionesActivas(): number {
    return this.planificaciones().filter((p) => p.estado === 'ACTIVA').length;
  }

  get planificacionesExcedidas(): number {
    return this.planificaciones().filter((p) => p.estado === 'EXCEDIDA').length;
  }

  get totalMontoAsignado(): number {
    return this.planificaciones().reduce((total, p) => total + p.montoTope, 0);
  }

  get totalMontoGastado(): number {
    return this.planificaciones().reduce((total, p) => total + p.montoGastado, 0);
  }

  // Métodos de utilidad
  formatearMonto(monto: number): string {
    return PlanificacionUtils.formatearMonto(monto);
  }

  ngOnDestroy(): void {
    // Restaurar tema por defecto al salir
    this.themeService.resetTheme();
  }
}
