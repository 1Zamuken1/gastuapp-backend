import { Component, OnInit, OnDestroy, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

// Core
import { AhorroService } from '../../core/services/ahorro.service';
import { MetaAhorro } from '../../core/models/ahorro.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ThemeService } from '../../core/services/theme.service';

// Child Components
import { CrearMetaModalComponent } from './crear-meta-modal/crear-meta-modal.component';
import { DetalleMetaModalComponent } from './detalle-meta-modal/detalle-meta-modal.component';

@Component({
  selector: 'app-ahorros',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    ProgressBarModule,
    TagModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    SkeletonModule,
    TooltipModule,
    SelectModule,
    IconFieldModule,
    InputIconModule,
    CrearMetaModalComponent,
    DetalleMetaModalComponent,
  ],
  templateUrl: './ahorros.component.html',
  styleUrl: './ahorros.component.scss',
  providers: [MessageService, ConfirmationService],
})
export class AhorrosComponent implements OnInit, OnDestroy {
  // ========= SIGNALS =========
  metas = signal<MetaAhorro[]>([]);
  loading = signal(true);

  // Search & Sort
  searchTerm = signal('');
  sortOption = signal('nombre-asc');

  sortOptions = [
    { label: 'Nombre (A-Z)', value: 'nombre-asc' },
    { label: 'Nombre (Z-A)', value: 'nombre-desc' },
    { label: 'Monto Ahorrado (Mayor)', value: 'monto-desc' },
    { label: 'Monto Ahorrado (Menor)', value: 'monto-asc' },
    { label: 'Progreso (Mayor)', value: 'progreso-desc' },
    { label: 'Progreso (Menor)', value: 'progreso-asc' },
  ];

  // Computed: Total Ahorrado
  totalAhorrado = computed(() => {
    return this.metas().reduce((acc, meta) => acc + (meta.montoActual || 0), 0);
  });

  // Computed: Filtered & Sorted Metas
  metasFiltradas = computed(() => {
    let result = [...this.metas()];

    // Filter by search term
    const term = this.searchTerm().toLowerCase();
    if (term) {
      result = result.filter((meta) => meta.nombre.toLowerCase().includes(term));
    }

    // Sort
    const [field, direction] = this.sortOption().split('-');
    result.sort((a, b) => {
      let comparison = 0;
      switch (field) {
        case 'nombre':
          comparison = a.nombre.localeCompare(b.nombre);
          break;
        case 'monto':
          comparison = (a.montoActual || 0) - (b.montoActual || 0);
          break;
        case 'progreso':
          comparison = (a.porcentajeProgreso || 0) - (b.porcentajeProgreso || 0);
          break;
      }
      return direction === 'desc' ? -comparison : comparison;
    });

    return result;
  });

  // Modal states
  mostrarModalCrear = signal(false);
  mostrarModalDetalle = signal(false);
  metaSeleccionada: MetaAhorro | null = null;
  metaToEdit: MetaAhorro | null = null;

  // ========= SERVICES =========
  private ahorroService = inject(AhorroService);
  private themeService = inject(ThemeService);

  // ========= LIFECYCLE =========
  ngOnInit(): void {
    // Activar tema ámbar para módulo de Ahorros
    this.themeService.setSavingsTheme();
    this.cargarMetas();
  }

  ngOnDestroy(): void {
    // Restaurar tema por defecto al salir
    this.themeService.resetTheme();
  }

  // ========= DATA =========
  cargarMetas(): void {
    this.loading.set(true);
    this.ahorroService.listarMetas().subscribe({
      next: (data) => {
        this.metas.set(data);
        // Actualizar referencia en modal detalle si está abierto
        if (this.mostrarModalDetalle() && this.metaSeleccionada) {
          const updated = data.find((m) => m.id === this.metaSeleccionada!.id);
          if (updated) {
            this.metaSeleccionada = updated;
          }
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando metas', err);
        this.loading.set(false);
      },
    });
  }

  // ========= SEARCH & SORT =========
  onSearchChange(value: string): void {
    this.searchTerm.set(value);
  }

  onSortChange(value: string): void {
    this.sortOption.set(value);
  }

  // ========= EXPORT =========
  openExport(): void {
    // TODO: Implementar exportación
    console.log('Abrir modal de exportación');
  }

  // ========= HELPERS =========
  formatCurrency(value: number | undefined): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value || 0);
  }

  getSeverity(
    estado: string,
  ): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' | undefined {
    switch (estado) {
      case 'ACTIVA':
        return 'success';
      case 'COMPLETADA':
        return 'info';
      case 'PAUSADA':
        return 'warn';
      case 'CANCELADA':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  // ========= MODALS =========
  openCrearMeta(): void {
    this.metaToEdit = null;
    this.mostrarModalCrear.set(true);
  }

  onMetaCreated(): void {
    this.cargarMetas();
  }

  verDetalle(meta: MetaAhorro): void {
    this.metaSeleccionada = meta;
    this.mostrarModalDetalle.set(true);
  }

  onAbonoRegistrado(): void {
    this.cargarMetas();
  }

  onMetaDeleted(): void {
    this.cargarMetas();
    this.mostrarModalDetalle.set(false);
    this.metaSeleccionada = null;
  }

  onMetaEditRequested(meta: MetaAhorro): void {
    this.metaToEdit = meta;
    this.mostrarModalCrear.set(true);
    this.mostrarModalDetalle.set(false);
  }
}
