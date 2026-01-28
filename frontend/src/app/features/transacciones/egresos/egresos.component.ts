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
import { Component, OnInit, signal, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { ConfirmationService, MessageService } from 'primeng/api';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

// Services & Models
import { TransaccionService } from '../../../core/services/transaccion.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import {
  Transaccion,
  TipoTransaccion,
  TransaccionRequest,
} from '../../../core/models/transaccion.model';
import { Categoria } from '../../../core/models/categoria.model';
import { ThemeService } from '../../../core/services/theme.service';
import { ExportModalComponent } from '../../../shared/components/export-modal/export-modal.component';
import { CategorySelectorModal } from '../../../shared/components/category-selector-modal/category-selector-modal';
import { ExportModalConfig } from '../../../shared/models/export.model';

@Component({
  selector: 'app-egresos',
  standalone: true,
  imports: [
    FormsModule,
    IconFieldModule,
    InputIconModule,
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    TagModule,
    SkeletonModule,
    TooltipModule,
    ConfirmDialogModule,
    ToastModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    ExportModalComponent,
    CategorySelectorModal,
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './egresos.component.html',
  styleUrl: './egresos.component.scss',
})
export class EgresosComponent implements OnInit {
  // ==================== SIGNALS ====================
  // Lista de egresos
  egresos = signal<Transaccion[]>([]);
  categorias = signal<Categoria[]>([]);

  // Estado de carga
  loading = signal(true);
  saving = signal(false);
  mostrarDialog = signal(false);

  // Total de egresos (para mostrar en header)
  totalEgresos = signal(0);

  // Formulario
  egresoForm: FormGroup;

  // Estado edición
  esEdicion = signal(false);
  idEdicion = signal<number | null>(null);

  // Modal Detalle
  mostrarDetalleDialog = signal(false);
  selectedCategory = signal<CategoriaAgrupada | null>(null);
  categoriasAgrupadas = signal<CategoriaAgrupada[]>([]);

  // Modal Selector de Categoría
  showCategorySelector = signal(false);

  onCategorySelected(cat: Categoria): void {
    this.egresoForm.patchValue({ categoria: cat });
    this.showCategorySelector.set(false);
  }

  // ==================== TOOLBAR ====================
  sortOption = signal<string>('nombre-asc');
  searchTerm = signal<string>('');

  sortOptions = [
    { label: 'Nombre (A-Z)', value: 'nombre-asc' },
    { label: 'Nombre (Z-A)', value: 'nombre-desc' },
    { label: 'Monto (Mayor)', value: 'monto-desc' },
    { label: 'Monto (Menor)', value: 'monto-asc' },
    { label: 'Registros (Mayor)', value: 'cantidad-desc' },
    { label: 'Registros (Menor)', value: 'cantidad-asc' },
    { label: 'Fecha (Reciente)', value: 'fecha-desc' },
    { label: 'Fecha (Antigua)', value: 'fecha-asc' },
  ];

  // ==================== EXPORTACIÓN ====================
  exportVisible = signal(false);
  exportConfig = signal<ExportModalConfig | null>(null);

  // Theme Service (para cambio dinámico de colores)
  private themeService = inject(ThemeService);

  // MÉTODO PARA ABRIR MODAL DE EXPORTACIÓN
  openExport(): void {
    const config: ExportModalConfig = {
      title: 'Exportar Egresos',
      moduleType: 'transacciones',
      fields: [
        {
          type: 'checkbox-group',
          key: 'categorias',
          label: 'Categorías',
          options: this.categoriasAgrupadas().map((c) => ({
            label: c.nombre,
            value: c.id,
            checked: true,
          })),
        },
        {
          type: 'daterange',
          key: 'fechas',
          label: 'Rango de Fechas',
        },
      ],
      columns: [
        { header: 'Fecha', field: 'fecha', type: 'date' },
        { header: 'Categoría', field: 'categoriaNombre', type: 'text' },
        { header: 'Descripción', field: 'descripcion', type: 'text' },
        { header: 'Monto', field: 'monto', type: 'currency' },
      ],
      data: this.egresos(),
    };

    this.exportConfig.set(config);
    this.exportVisible.set(true);
  }

  constructor(
    private transaccionService: TransaccionService,
    private categoriaService: CategoriaService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private fb: FormBuilder,
  ) {
    this.egresoForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(100)]],
      monto: [null, [Validators.required, Validators.min(1)]],
      fecha: [new Date(), Validators.required],
      categoria: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    // Activar tema naranja para módulo de Egresos
    this.themeService.setExpenseTheme();
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    // Restaurar tema por defecto al salir
    this.themeService.resetTheme();
  }

  // ==================== MÉTODOS DE CARGA ====================

  cargarDatos(): void {
    this.loading.set(true);
    // Pedimos categorías de tipo EGRESO
    this.categoriaService.listarPorTipo('EGRESO').subscribe({
      next: (cats) => {
        this.categorias.set(cats);
        this.cargarEgresos();
      },
      error: (err) => {
        console.error('Error cargando categorías:', err);
        this.loading.set(false);
      },
    });
  }

  /**
   * Carga los egresos desde el backend.
   * Filtra por tipo 'EGRESO' automáticamente.
   */
  cargarEgresos(): void {
    const selectedId = this.selectedCategory()?.id;

    // Pedimos transacciones de tipo EGRESO
    this.transaccionService.listarPorTipo('EGRESO').subscribe({
      next: (data) => {
        this.egresos.set(data);
        this.calcularTotal(data);
        this.agruparCategorias(data);

        // Auto-refresh del modal de detalle
        if (selectedId) {
          const updatedCat = this.categoriasAgrupadas().find((c) => c.id === selectedId);
          if (updatedCat) {
            this.selectedCategory.set(updatedCat);
          } else {
            this.mostrarDetalleDialog.set(false);
            this.selectedCategory.set(null);
          }
        }

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando egresos:', err);
        this.loading.set(false);
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

  /**
   * Agrupa Categorías
   */
  private agruparCategorias(data: Transaccion[]): void {
    const agrupado = new Map<number, CategoriaAgrupada>();
    data.forEach((t) => {
      if (!agrupado.has(t.categoriaId)) {
        agrupado.set(t.categoriaId, {
          id: t.categoriaId,
          nombre: t.categoriaNombre,
          icono: t.categoriaIcono,
          total: 0,
          cantidad: 0,
          transacciones: [],
        });
      }
      const cat = agrupado.get(t.categoriaId)!;
      cat.total += t.monto;
      cat.cantidad++;
      cat.transacciones.push(t);
    });
    const categorias = Array.from(agrupado.values());
    this.categoriasAgrupadas.set(this.sortCategorias(categorias));
  }

  // Ordena las categorías según la opción seleccionada
  private sortCategorias(categorias: CategoriaAgrupada[]): CategoriaAgrupada[] {
    const [field, direction] = this.sortOption().split('-');
    const asc = direction === 'asc' ? 1 : -1;

    return [...categorias].sort((a, b) => {
      switch (field) {
        case 'nombre':
          return asc * a.nombre.localeCompare(b.nombre);
        case 'monto':
          return asc * (a.total - b.total);
        case 'cantidad':
          return asc * (a.cantidad - b.cantidad);
        case 'fecha':
          const fechaA = a.transacciones.length ? new Date(a.transacciones[0].fecha).getTime() : 0;
          const fechaB = b.transacciones.length ? new Date(b.transacciones[0].fecha).getTime() : 0;
          return asc * (fechaA - fechaB);
        default:
          return 0;
      }
    });
  }

  // Callback cuando cambia el sort
  onSortChange(event: { value: string }): void {
    this.sortOption.set(event.value);
    const current = this.categoriasAgrupadas();
    this.categoriasAgrupadas.set(this.sortCategorias(current));
  }

  // callback cuando cambia la búsqueda
  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  // Getter: Filtra categorías por término de búsqueda
  get categoriasFiltradas(): CategoriaAgrupada[] {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return this.categoriasAgrupadas();

    return this.categoriasAgrupadas().filter(
      (cat) =>
        cat.nombre.toLowerCase().includes(term) ||
        cat.transacciones.some((t) => t.descripcion.toLowerCase().includes(term)),
    );
  }

  // ==================== NAVEGACIÓN ====================
  verDetalleCategoria(categoria: CategoriaAgrupada): void {
    this.selectedCategory.set(categoria);
    this.mostrarDetalleDialog.set(true);
  }

  // ==================== MÉTODOS DE FORMATO Y HELPERS ====================

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
  // Helper crítico para fechas
  formatDate(dateString: string): string {
    const [year, month, day] = dateString.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return new Intl.DateTimeFormat('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(date);
  }

  // Helper crítico para guardar sin timezone
  private toLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // ==================== MÉTODOS CRUD ====================

  /**
   * Abre dialog para crear nuevo ingreso
   * TODO: Implementar dialog de creación
   */
  nuevoEgreso(): void {
    this.esEdicion.set(false);
    this.idEdicion.set(null);

    // Lógica inteligente de contexto
    const categoriaActiva = this.mostrarDetalleDialog() ? this.selectedCategory() : null;
    const categoriaParaForm = categoriaActiva
      ? this.categorias().find((c) => c.id === categoriaActiva.id) || null
      : null;

    let initialValues = {
      fecha: new Date(),
      monto: null as number | null,
      descripcion: '',
      categoria: categoriaParaForm,
    };

    // Clonar último registro
    if (categoriaActiva && categoriaActiva.transacciones.length > 0) {
      const ultimaTransaccion = categoriaActiva.transacciones[0];
      initialValues.monto = ultimaTransaccion.monto;
      initialValues.descripcion = ultimaTransaccion.descripcion;
    }

    this.egresoForm.reset(initialValues);
    this.mostrarDialog.set(true);
  }

  guardarEgreso(): void {
    if (this.egresoForm.invalid) {
      this.egresoForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const formValues = this.egresoForm.value;
    const transaccionDTO: TransaccionRequest = {
      monto: formValues.monto,
      tipo: 'EGRESO' as TipoTransaccion, // <--- IMPORTANTE: Tipo EGRESO
      descripcion: formValues.descripcion,
      fecha: this.toLocalDateString(formValues.fecha), // <--- Helper de fecha
      categoriaId: formValues.categoria.id,
    };
    const request =
      this.esEdicion() && this.idEdicion()
        ? this.transaccionService.actualizar(this.idEdicion()!, transaccionDTO)
        : this.transaccionService.crear(transaccionDTO);
    request.subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Egreso ${this.esEdicion() ? 'actualizado' : 'creado'} correctamente`,
        });
        this.mostrarDialog.set(false);
        this.cargarEgresos();
        this.saving.set(false);
      },
      error: (err) => {
        console.error('Error guardando:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo guardar el egreso',
        });
        this.saving.set(false);
      },
    });
  }

  /**
   * Abre dialog para editar egreso
   * TODO: Implementar dialog de edición
   */
  editarEgreso(egreso: Transaccion): void {
    const cat = this.categorias().find((c) => c.id === egreso.categoriaId);
    this.esEdicion.set(true);
    this.idEdicion.set(egreso.id);

    // Ajuste de fecha para que el picker la lea bien
    // (Si toLocalDateString guardó 'YYYY-MM-DD', new Date() al leerla podría fallar por zona horaria
    // al setear el valor en el control, pero el DatePicker suele manejar Dates nativos.
    // Lo ideal es parsear igual que en el helper inverso o dejar que new Date lo intente,
    // pero como ya viene formateada del backend string, asegurate que new Date(string) sea correcto localmente)
    // Truco: new Date(egreso.fecha + 'T00:00:00') fuerza local si no hay Z.
    const [y, m, d] = egreso.fecha.toString().split('-').map(Number);
    const fechaCorrecta = new Date(y, m - 1, d);
    this.egresoForm.patchValue({
      descripcion: egreso.descripcion,
      monto: egreso.monto,
      fecha: fechaCorrecta,
      categoria: cat,
    });
    this.mostrarDialog.set(true);
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
          detail: 'Egreso eliminado',
        });
        this.cargarEgresos();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar',
        });
      },
    });
  }
}
export interface CategoriaAgrupada {
  id: number;
  nombre: string;
  icono: string;
  total: number;
  cantidad: number;
  transacciones: Transaccion[];
}
