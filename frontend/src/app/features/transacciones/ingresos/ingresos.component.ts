/**
 * Component: IngresosComponent
 *
 * FLUJO DE DATOS:
 * - RECIBE: Lista de ingresos desde TransaccionService
 * - RENDERIZA: Tabla PrimeNG con datos de ingresos
 * - PROVEE: CRUD de ingresos (listar, crear, editar, eliminar)
 *
 * RESPONSABILIDAD:
 * Vista para gestionar transacciones de tipo INGRESO.
 * Muestra tabla con filtros y paginación.
 * Permite crear, editar y eliminar ingresos.
 *
 * ENDPOINT CONSUMIDO:
 * GET /api/transacciones/tipo/INGRESO
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
  selector: 'app-ingresos',
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
  templateUrl: './ingresos.component.html',
  styleUrl: './ingresos.component.scss',
})
export class IngresosComponent implements OnInit {
  // ==================== SIGNALS ====================
  // Lista de ingresos
  ingresos = signal<Transaccion[]>([]);

  // Estado de carga
  loading = signal(true);

  // Total de ingresos (para mostrar en header)
  totalIngresos = signal(0);

  constructor(
    private transaccionService: TransaccionService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.cargarIngresos();
  }

  // ==================== MÉTODOS DE CARGA ====================

  /**
   * Carga los ingresos desde el backend.
   * Filtra por tipo 'INGRESO' automáticamente.
   */
  cargarIngresos(): void {
    this.loading.set(true);

    this.transaccionService.listarPorTipo('INGRESO').subscribe({
      next: (data) => {
        this.ingresos.set(data);
        this.calcularTotal(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando ingresos:', err);
        this.loading.set(false);
        // Para demo, dejamos vacío en lugar de mostrar error
      },
    });
  }

  /**
   * Calcula el total de ingresos
   */
  private calcularTotal(data: Transaccion[]): void {
    const total = data.reduce((sum, t) => sum + t.monto, 0);
    this.totalIngresos.set(total);
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
  nuevoIngreso(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Próximamente',
      detail: 'La creación de ingresos se implementará pronto',
    });
  }

  /**
   * Abre dialog para editar ingreso
   * TODO: Implementar dialog de edición
   */
  editarIngreso(ingreso: Transaccion): void {
    console.log('Editar ingreso:', ingreso);
    this.messageService.add({
      severity: 'info',
      summary: 'Próximamente',
      detail: 'La edición de ingresos se implementará pronto',
    });
  }

  /**
   * Confirma y elimina un ingreso
   */
  confirmarEliminar(ingreso: Transaccion): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar "${ingreso.descripcion}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.eliminarIngreso(ingreso),
    });
  }

  /**
   * Elimina un ingreso
   */
  private eliminarIngreso(ingreso: Transaccion): void {
    this.transaccionService.eliminar(ingreso.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Eliminado',
          detail: 'El ingreso fue eliminado correctamente',
        });
        this.cargarIngresos(); // Recargar lista
      },
      error: (err) => {
        console.error('Error eliminando:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar el ingreso',
        });
      },
    });
  }
}
