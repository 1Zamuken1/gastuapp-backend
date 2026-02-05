import { Component, OnInit, OnDestroy, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { MenuModule } from 'primeng/menu';

// Core
import { ProyeccionService } from '../../core/services/proyeccion.service';
import { Proyeccion } from '../../core/models/proyeccion.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ThemeService } from '../../core/services/theme.service';

// Child Components
import { ProyeccionModalComponent } from './proyeccion-modal/proyeccion-modal.component';
import { ProyeccionDetalleComponent } from './proyeccion-detalle/proyeccion-detalle.component';

@Component({
  selector: 'app-proyecciones',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    TagModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    SkeletonModule,
    TooltipModule,
    SelectModule,
    IconFieldModule,
    InputIconModule,
    MenuModule,
    ProyeccionModalComponent,
    ProyeccionDetalleComponent,
  ],
  templateUrl: './proyecciones.component.html',
  styleUrl: './proyecciones.component.scss',
  providers: [MessageService, ConfirmationService],
})
export class ProyeccionesComponent implements OnInit, OnDestroy {
  // ========= SIGNALS =========
  proyecciones = signal<Proyeccion[]>([]);
  loading = signal(true);

  // Search & Sort
  searchTerm = signal('');
  sortOption = signal('nombre-asc');

  sortOptions = [
    { label: 'Nombre (A-Z)', value: 'nombre-asc' },
    { label: 'Nombre (Z-A)', value: 'nombre-desc' },
    { label: 'Monto (Mayor)', value: 'monto-desc' },
    { label: 'Monto (Menor)', value: 'monto-asc' },
    { label: 'Frecuencia', value: 'frecuencia' },
  ];

  // Computed: Totales separados por tipo
  totalIngresosProyectados = computed(() => {
    return this.proyecciones()
      .filter((p) => p.activo && p.tipo === 'INGRESO')
      .reduce((acc, p) => acc + p.monto, 0);
  });

  totalEgresosProyectados = computed(() => {
    return this.proyecciones()
      .filter((p) => p.activo && p.tipo === 'EGRESO')
      .reduce((acc, p) => acc + p.monto, 0);
  });

  // Computed: Filtered & Sorted
  proyeccionesFiltradas = computed(() => {
    let result = [...this.proyecciones()];

    // Filter by search
    const term = this.searchTerm().toLowerCase();
    if (term) {
      result = result.filter((p) => (p.nombreCategoria || '').toLowerCase().includes(term));
    }

    // Sort
    const [field, direction] = this.sortOption().split('-');
    result.sort((a, b) => {
      let comparison = 0;
      switch (field) {
        case 'nombre':
          comparison = (a.nombreCategoria || '').localeCompare(b.nombreCategoria || '');
          break;
        case 'monto':
          comparison = a.monto - b.monto;
          break;
        case 'frecuencia':
          comparison = a.frecuencia.localeCompare(b.frecuencia);
          break;
      }
      return direction === 'desc' ? -comparison : comparison;
    });

    return result;
  });

  // Modal states
  mostrarModalCrear = signal(false);
  mostrarModalDetalle = signal(false);
  proyeccionToEdit: Proyeccion | null = null;
  proyeccionSeleccionada: Proyeccion | null = null; // Para detalle

  // ========= SERVICES =========
  private proyeccionService = inject(ProyeccionService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private themeService = inject(ThemeService);

  // ========= LIFECYCLE =========
  ngOnInit(): void {
    // Activar tema azul
    this.themeService.setProyeccionTheme();
    this.cargarProyecciones();
  }

  ngOnDestroy(): void {
    // Restaurar tema
    this.themeService.resetTheme();
  }

  // ========= DATA =========
  cargarProyecciones(): void {
    this.loading.set(true);
    this.proyeccionService.listarProyecciones().subscribe({
      next: (data) => {
        this.proyecciones.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando proyecciones', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail:
            'No se pudieron cargar las proyecciones. ' +
            (err.status === 403 ? 'Acceso denegado.' : ''),
        });
        this.loading.set(false);
      },
    });
  }

  // ========= ACTIONS =========
  openCrear(): void {
    this.proyeccionToEdit = null;
    this.mostrarModalCrear.set(true);
  }

  verDetalle(proyeccion: Proyeccion, event?: Event): void {
    if (event) event.stopPropagation();
    this.proyeccionSeleccionada = proyeccion;
    this.mostrarModalDetalle.set(true);
  }

  onEjecucionRealizada(): void {
    this.cargarProyecciones();
  }

  editar(proyeccion: Proyeccion, event: Event): void {
    event.stopPropagation();
    this.proyeccionToEdit = proyeccion;
    this.mostrarModalCrear.set(true);
  }

  eliminar(proyeccion: Proyeccion, event: Event): void {
    event.stopPropagation();
    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar la proyección "${proyeccion.nombreCategoria || 'Sin categoría'}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.proyeccionService.eliminarProyeccion(proyeccion.id!).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Eliminado',
              detail: 'Proyección eliminada correctamente',
            });
            this.cargarProyecciones();
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo eliminar la proyección',
            });
          },
        });
      },
    });
  }

  ejecutar(proyeccion: Proyeccion, event: Event): void {
    event.stopPropagation();
    this.confirmationService.confirm({
      message: `¿Ejecutar la proyección "${proyeccion.nombreCategoria || 'Sin categoría'}" y crear la transacción?`,
      header: 'Confirmar Ejecución',
      icon: 'pi pi-bolt',
      accept: () => {
        this.proyeccionService.ejecutarProyeccion(proyeccion.id!).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Ejecutado',
              detail: 'Transacción creada exitosamente',
            });
            this.cargarProyecciones(); // Recargar para actualizar ultimaEjecucion
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Error al ejecutar la proyección',
            });
          },
        });
      },
    });
  }

  onProyeccionGuardada(): void {
    this.cargarProyecciones();
    this.mostrarModalCrear.set(false);
  }

  // ========= HELPERS =========
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(value);
  }

  getSeverity(activo: boolean): 'success' | 'secondary' {
    return activo ? 'success' : 'secondary';
  }

  onSearchChange(value: string): void {
    this.searchTerm.set(value);
  }

  onSortChange(value: string): void {
    this.sortOption.set(value);
  }
}
