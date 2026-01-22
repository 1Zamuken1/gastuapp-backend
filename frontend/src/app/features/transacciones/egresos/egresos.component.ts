/**
 * Component: EgresosComponent
 *
 * FLUJO DE DATOS:
 * - RECIBE: Lista de egresos desde TransaccionService
 * - RENDERIZA: Tabla PrimeNG con datos de egresos
 * - PROVEE: CRUD de egresos (listar, crear, editar, eliminar)
 *
 * RESPONSABILIDAD:
 * Vista para gestionar transacciones de tipo EGRESO.
 * Muestra tabla con filtros y paginación.
 * Permite crear, editar y eliminar egresos.
 *
 * ENDPOINT CONSUMIDO:
 * GET /api/transacciones/tipo/EGRESO
 *
 * NOTA PARA EGRESOS:
 * Este componente sirve como plantilla para crear EgresosComponent.
 * Solo cambia el tipo de transacción ('EGRESO' en lugar de 'INGRESO').
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';

// Services & Models
import { TransaccionService } from '../../../core/services/transaccion.service';
import { Transaccion } from '../../../core/models/transaccion.model';

@Component({
  selector: 'app-egresos',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    CardModule,
    TagModule,
    SkeletonModule,
    TooltipModule,
    ConfirmDialogModule,
    ToastModule,
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './egresos.component.html',
  styleUrl: './egresos.component.scss',
})
export class EgresosComponent implements OnInit {
  // ==================== SIGNALS ====================
  // Lista de egresos
  egresos = signal<Transaccion[]>([]);

  // Estado de carga
  loading = signal(true);

  // Total de egresos (para mostrar en header)
  totalEgresos = signal(0);

  constructor(
    private transaccionService: TransaccionService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.cargarEgresos();
  }

  // ==================== MÉTODOS DE CARGA ====================

  /**
   * Carga los egresos desde el backend.
   * Filtra por tipo 'EGRESO' automáticamente.
   */
  cargarEgresos(): void {
    this.loading.set(true);

    this.transaccionService.listarPorTipo('EGRESO').subscribe({
      next: (data) => {
        this.egresos.set(data);
        this.calcularTotal(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando egresos:', err);
        this.loading.set(false);
        // Para demo, dejamos vacío en lugar de mostrar error
      },
    });
  }

  /**
   * Calcula el total de egresos
   */
  private calcularTotal(data: Transaccion[]): void {
    const total = data.reduce((sum, t) => sum + t.monto, 0);
    this.totalEgresos.set(total);
  }

  // ==================== MÉTODOS DE FORMATO ====================

  /**
   * Formatea un número como moneda colombiana
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }

  /**
   * Formatea una fecha ISO a formato legible
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(date);
  }

  // ==================== MÉTODOS CRUD ====================

  /**
   * Abre dialog para crear nuevo ingreso
   * TODO: Implementar dialog de creación
   */
  nuevoEgreso(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Próximamente',
      detail: 'La creación de egresos se implementará pronto',
    });
  }

  /**
   * Abre dialog para editar egreso
   * TODO: Implementar dialog de edición
   */
  editarEgreso(egreso: Transaccion): void {
    console.log('Editar egreso:', egreso);
    this.messageService.add({
      severity: 'info',
      summary: 'Próximamente',
      detail: 'La edición de egresos se implementará pronto',
    });
  }

  /**
   * Abre dialog para eliminar egreso
   * TODO: Implementar dialog de eliminación
   */
  confirmarEliminar(egreso: Transaccion): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar "${egreso.descripcion}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.eliminarEgreso(egreso),
    });
  }

  /**
   * Elimina un egreso
   */
  private eliminarEgreso(egreso: Transaccion): void {
    this.transaccionService.eliminar(egreso.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Eliminado',
          detail: 'El egreso fue eliminado correctamente',
        });
        this.cargarEgresos(); // Recargar lista
      },
      error: (err) => {
        console.error('Error eliminando:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar el egreso',
        });
      },
    });
  }
}
