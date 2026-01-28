import {
  Component,
  OnInit,
  signal,
  inject,
  computed,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ProgressBarModule } from 'primeng/progressbar';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { SkeletonModule } from 'primeng/skeleton';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { MultiSelectModule } from 'primeng/multiselect';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

// Core
import { Planificacion, PlanificacionUtils } from '../../../core/models/planificacion.model';
import { ThemeService } from '../../../core/services/theme.service';
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';

// Child Components

/**
 * Component: PlanificacionListComponent
 *
 * FLUJO DE DATOS:
 * - RECIBE: Lista de planificaciones desde el componente padre
 * - RENDERIZA: Lista filtrada y paginada
 * - EMITE: Eventos de acciones (editar, desactivar, eliminar)
 */
@Component({
  selector: 'app-planificacion-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    TableModule,
    TagModule,
    TooltipModule,
    SkeletonModule,
    CheckboxModule,
    SelectModule,
    DatePickerModule,
    MultiSelectModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    ProgressBarModule,
  ],
  templateUrl: './planificacion-list.component.html',
  styleUrl: './planificacion-list.component.scss',
})
export class PlanificacionListComponent implements OnInit {
  // ========= INPUTS =========
  @Input() set planificaciones(data: Planificacion[]) {
    this._planificaciones.set(data);
    this.totalRecords.set(data.length);
  }
  @Input() loading = false;

  // ========= OUTPUTS =========
  @Output() editar = new EventEmitter<Planificacion>();
  @Output() desactivar = new EventEmitter<Planificacion>();
  @Output() eliminar = new EventEmitter<Planificacion>();
  @Output() verDetalle = new EventEmitter<Planificacion>();

  // ========= SIGNALS =========
  private _planificaciones = signal<Planificacion[]>([]);

  // Filtros
  searchTerm = signal('');
  estadoFiltro = signal<string | null>(null);
  categoriaFiltro = signal<string | null>(null);
  frecuenciaFiltro = signal<string | null>(null);
  fechaInicioFiltro = signal<Date | null>(null);
  fechaFinFiltro = signal<Date | null>(null);
  autoRenovarFiltro = signal<boolean | null>(null);

  // Sort
  sortField = signal('nombre-asc');
  sortDirection = signal<'asc' | 'desc'>('asc');

  // Selection
  selectedPlanificaciones = signal<Set<string>>(new Set());
  selectAll = signal<boolean>(false);

  // Pagination
  first = signal(0);
  rows = signal(10);
  totalRecords = signal(0);

  // UI State
  mostrarAcciones = true;
  categorias = computed(() => {
    // Extraer categorías únicas de las planificaciones
    const uniqueMap = new Map();
    this._planificaciones().forEach((p) => {
      uniqueMap.set(p.categoriaId, { id: p.categoriaId, nombre: p.categoriaNombre });
    });
    return Array.from(uniqueMap.values());
  });

  // ========= SERVICES =========
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private themeService = inject(ThemeService);

  // ========= OPTIONS =========
  sortOptions = [
    { label: 'Nombre (A-Z)', value: 'nombre-asc' },
    { label: 'Nombre (Z-A)', value: 'nombre-desc' },
    { label: 'Fecha Inicio (Más reciente)', value: 'fechaInicio-desc' },
    { label: 'Fecha Inicio (Más antiguo)', value: 'fechaInicio-asc' },
    { label: 'Monto Tope (Mayor)', value: 'montoTope-desc' },
    { label: 'Monto Tope (Menor)', value: 'montoTope-asc' },
    { label: 'Porcentaje Uso (Mayor)', value: 'porcentaje-desc' },
    { label: 'Porcentaje Uso (Menor)', value: 'porcentaje-asc' },
    { label: 'Fecha Fin (Más reciente)', value: 'fechaFin-desc' },
    { label: 'Fecha Fin (más antiguo)', value: 'fechaFin-asc' },
  ];

  estadosFiltro = [
    { label: 'Todos', value: null },
    { label: 'Activo', value: 'ACTIVA' },
    { label: 'Excedido', value: 'EXCEDIDA' },
    { label: 'Inactivo', value: 'INACTIVA' },
  ];

  frecuenciasFiltro = [
    { label: 'Todas', value: null },
    { label: 'Semanal', value: 'SEMANAL' },
    { label: 'Quincenal', value: 'QUINCENAL' },
    { label: 'Mensual', value: 'MENSUAL' },
    { label: 'Trimestral', value: 'TRIMESTRAL' },
    { label: 'Semestral', value: 'SEMESTRAL' },
    { label: 'Anual', value: 'ANUAL' },
  ];

  siNoFiltrosActivos = computed(() => {
    return (
      !this.searchTerm() &&
      !this.estadoFiltro() &&
      !this.categoriaFiltro() &&
      !this.frecuenciaFiltro() &&
      !this.fechaInicioFiltro() &&
      !this.fechaFinFiltro() &&
      this.autoRenovarFiltro() === null
    );
  });

  // ========= DATOS FILTRADOS =========
  // Filtrar y ordenar (sin paginación)
  private _filteredAndSorted = computed(() => {
    let data = [...this._planificaciones()];

    // 1. Filtrar
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      data = data.filter(
        (p) =>
          p.categoriaNombre.toLowerCase().includes(term) ||
          (p.nombrePersonalizado && p.nombrePersonalizado.toLowerCase().includes(term)),
      );
    }

    if (this.estadoFiltro()) {
      data = data.filter((p) => String(p.estado) === this.estadoFiltro());
    }

    if (this.categoriaFiltro()) {
      data = data.filter((p) => p.categoriaId === Number(this.categoriaFiltro()));
    }

    if (this.frecuenciaFiltro()) {
      data = data.filter((p) => String(p.frecuencia) === this.frecuenciaFiltro());
    }

    if (this.autoRenovarFiltro() !== null) {
      data = data.filter((p) => p.autoRenovar === this.autoRenovarFiltro());
    }

    // 2. Ordenar
    const sort = this.sortField();
    const [field, dir] = sort.split('-');
    const multiplier = dir === 'asc' ? 1 : -1;

    data.sort((a, b) => {
      let valA: any, valB: any;

      switch (field) {
        case 'nombre':
          valA = a.nombrePersonalizado || a.categoriaNombre;
          valB = b.nombrePersonalizado || b.categoriaNombre;
          break;
        case 'fechaInicio':
          valA = new Date(a.fechaInicio).getTime();
          valB = new Date(b.fechaInicio).getTime();
          break;
        case 'fechaFin':
          valA = new Date(a.fechaFin).getTime();
          valB = new Date(b.fechaFin).getTime();
          break;
        case 'montoTope':
          valA = a.montoTope;
          valB = b.montoTope;
          break;
        case 'porcentaje':
          valA = a.porcentajeUtilizacion;
          valB = b.porcentajeUtilizacion;
          break;
        default:
          return 0;
      }

      if (valA < valB) return -1 * multiplier;
      if (valA > valB) return 1 * multiplier;
      return 0;
    });

    return data;
  });

  // Total de registros filtrados (sin paginar)
  totalRecordsFiltered = computed(() => this._filteredAndSorted().length);

  // Datos paginados para mostrar
  planificacionesFiltradas = computed(() => {
    const data = this._filteredAndSorted();
    const start = this.first();
    const end = start + this.rows();
    return data.slice(start, end);
  });

  // ==================== LIFECYCLE ====================
  ngOnInit(): void {
    this.setearTemaMorado();
    // No cargamos datos aquí, vienen por Input
  }

  // ==================== FILTROS ACTIONS ====================
  limpiarFiltros(): void {
    this.searchTerm.set('');
    this.estadoFiltro.set(null);
    this.categoriaFiltro.set(null);
    this.frecuenciaFiltro.set(null);
    this.fechaInicioFiltro.set(null);
    this.fechaFinFiltro.set(null);
    this.autoRenovarFiltro.set(null);
    this.first.set(0);
  }

  // ==================== SEARCH & SORT ====================
  onSearchChange(value: string): void {
    this.searchTerm.set(value);
    this.first.set(0);
  }

  onSortChange(field: string, direction: string): void {
    this.sortField.set(field);
    this.sortDirection.set(direction as 'asc' | 'desc');
  }

  // ==================== SELECTION ====================
  toggleSelectAll(): void {
    this.selectAll.set(!this.selectAll());
    if (this.selectAll()) {
      this.selectedPlanificaciones.set(new Set(this._planificaciones().map((p) => p.publicId)));
    } else {
      this.selectedPlanificaciones.set(new Set());
    }
  }

  limpiarSeleccion(): void {
    this.selectedPlanificaciones.set(new Set());
    this.selectAll.set(false);
  }

  toggleSelection(id: string): void {
    const currentSelection = this.selectedPlanificaciones();
    const newSelection = new Set(currentSelection);

    if (newSelection.has(id)) {
      newSelection.delete(id);
    } else {
      newSelection.add(id);
    }

    this.selectedPlanificaciones.set(newSelection);
  }

  isSelected(id: string): boolean {
    return this.selectedPlanificaciones().has(id);
  }

  // Helper para Math.min en template
  min(a: number, b: number): number {
    return Math.min(a, b);
  }

  // ==================== ACTIONS ====================
  onEditar(presupuesto: Planificacion): void {
    this.editar.emit(presupuesto);
  }

  onDesactivar(presupuesto: Planificacion): void {
    this.desactivar.emit(presupuesto);
  }

  onEliminar(presupuesto: Planificacion): void {
    this.eliminar.emit(presupuesto);
  }

  exportarDatos(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Información',
      detail: 'Funcionalidad de exportación en desarrollo',
    });
  }

  eliminarSeleccionados(): void {
    const ids = Array.from(this.selectedPlanificaciones());
    if (ids.length === 0) {
      this.messageService.add({
        severity: 'info',
        summary: 'Información',
        detail: 'No hay planificaciones seleccionadas',
      });
      return;
    }

    this.confirmationService.confirm({
      message: `¿Estás seguro de eliminar las ${ids.length} planificaciones seleccionadas?`,
      header: 'Confirmar Eliminación Masiva',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        // Emitir evento por cada uno o manejar en lote?
        // Por ahora no soportado por API de lote en este refactor rápido
        // TODO: Emitir evento masivo o iterar
        ids.forEach((id) => {
          const p = this._planificaciones().find((p) => p.publicId === id);
          if (p) this.eliminar.emit(p);
        });
        this.limpiarSeleccion();
      },
    });
  }

  // ==================== PAGINACIÓN ====================
  nextPage(): void {
    if (this.first() + this.rows() < this.totalRecords()) {
      this.first.set(this.first() + this.rows());
    }
  }

  previousPage(): void {
    if (this.first() - this.rows() >= 0) {
      this.first.set(Math.max(0, this.first() - this.rows()));
    }
  }

  // ==================== TEMA ====================
  private setearTemaMorado(): void {
    this.themeService.setPlanificacionTheme();
  }

  // ==================== HELPERS ====================
  onVerDetalle(presupuesto: Planificacion): void {
    this.verDetalle.emit(presupuesto);
  }

  getEstadoSeverity(presupuesto: Planificacion): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (presupuesto.estado) {
      case 'ACTIVA':
        return presupuesto.porcentajeUtilizacion > 80 ? 'warn' : 'success';
      case 'EXCEDIDA':
        return 'danger';
      case 'INACTIVA':
        return 'secondary';
      default:
        return 'secondary';
    }
  }

  formatCurrency(amount: number): string {
    return PlanificacionUtils.formatearMonto(amount);
  }
}
