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
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
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
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select'; // PrimeNG v18+
import { ConfirmationService, MessageService } from 'primeng/api';

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
  selector: 'app-ingresos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    InputNumberModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    ExportModalComponent,
    CategorySelectorModal,
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './ingresos.component.html',
  styleUrl: './ingresos.component.scss',
})
export class IngresosComponent implements OnInit {
  // ==================== SIGNALS ====================
  // Lista de ingresos y categorías
  ingresos = signal<Transaccion[]>([]);
  categorias = signal<Categoria[]>([]);

  // Estados de carga y dialog
  loading = signal(true);
  saving = signal(false);
  mostrarDialog = signal(false);

  // Total de ingresos
  totalIngresos = signal(0);

  // Formulario
  ingresoForm: FormGroup;

  // Estado edición
  esEdicion = signal(false);
  idEdicion = signal<number | null>(null);

  // ==================== ESTADO VISTA ====================
  // Ya no necesitamos viewMode explícito si usamos dialog, pero podemos mantenerlo o simplificar.
  // Usaremos un signal booleano para el dialog de detalle.
  mostrarDetalleDialog = signal(false);
  selectedCategory = signal<CategoriaAgrupada | null>(null);

  // Categorías agrupadas (Solo las que tienen registros)
  categoriasAgrupadas = signal<CategoriaAgrupada[]>([]);

  // Modal Selector de Categoría
  showCategorySelector = signal(false);

  onCategorySelected(cat: Categoria): void {
    this.ingresoForm.patchValue({ categoria: cat });
    this.showCategorySelector.set(false);
  }

  // ==================== TOOLBAR: ORDENAMIENTO Y BÚSQUEDA ====================
  sortOption = signal<string>('nombre-asc');
  searchTerm = signal<string>('');

  // Opciones de ordenamiento para el dropdown
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

  // ... constructor ...

  // MÉTODO PARA ABRIR MODAL DE EXPORTACIÓN
  openExport(): void {
    const config: ExportModalConfig = {
      title: 'Exportar Ingresos',
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
      data: this.ingresos(),
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
    // Inicializar formulario
    this.ingresoForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(100)]],
      monto: [null, [Validators.required, Validators.min(1)]],
      fecha: [new Date(), Validators.required],
      categoria: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    // Activar tema verde para módulo de Ingresos
    this.themeService.setIncomeTheme();
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    // Restaurar tema por defecto al salir
    this.themeService.resetTheme();
  }

  // ==================== MÉTODOS DE CARGA ====================

  cargarDatos(): void {
    this.loading.set(true);

    // Cargar categorías primero, luego ingresos
    this.categoriaService.listarPorTipo('INGRESO').subscribe({
      next: (cats) => {
        this.categorias.set(cats);
        this.cargarIngresos();
      },
      error: (err) => {
        console.error('Error cargando categorías:', err);
        this.loading.set(false);
      },
    });
  }

  cargarIngresos(): void {
    // Guardamos el ID seleccionado para restaurarlo tras recargar
    const selectedId = this.selectedCategory()?.id;

    this.transaccionService.listarPorTipo('INGRESO').subscribe({
      next: (data) => {
        this.ingresos.set(data);
        this.calcularTotal(data);
        this.agruparCategorias(data);

        // Auto-refresh: Actualizar la categoría seleccionada con los nuevos datos
        if (selectedId) {
          const updatedCat = this.categoriasAgrupadas().find((c) => c.id === selectedId);
          if (updatedCat) {
            this.selectedCategory.set(updatedCat);
          } else {
            // Si la categoría ya no existe (ej: borró el último registro), cerrar modal
            this.mostrarDetalleDialog.set(false);
            this.selectedCategory.set(null);
          }
        }

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando ingresos:', err);
        this.loading.set(false);
      },
    });
  }

  private calcularTotal(data: Transaccion[]): void {
    const total = data.reduce((sum, t) => sum + t.monto, 0);
    this.totalIngresos.set(total);
  }

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

    // Aplicar ordenamiento según sortOption
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
          // Ordenar por la fecha más reciente de las transacciones en cada categoría
          const fechaA = a.transacciones.length ? new Date(a.transacciones[0].fecha).getTime() : 0;
          const fechaB = b.transacciones.length ? new Date(b.transacciones[0].fecha).getTime() : 0;
          return asc * (fechaA - fechaB);
        default:
          return 0;
      }
    });
  }

  // Callback cuando cambia el ordenamiento
  onSortChange(event: { value: string }): void {
    this.sortOption.set(event.value);
    // Re-aplicar ordenamiento a las categorías existentes
    const current = this.categoriasAgrupadas();
    this.categoriasAgrupadas.set(this.sortCategorias(current));
  }

  // Callback cuando cambia el texto de búsqueda
  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  // Computed: Categorías filtradas por búsqueda
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

  // ==================== MÉTODOS CRUD ====================

  nuevoIngreso(): void {
    this.esEdicion.set(false);
    this.idEdicion.set(null);

    // Determinar si hay un contexto de categoría activo (desde el modal de detalle)
    // Solo usamos la categoría seleccionada si el dialog de detalle está ABIERTO.
    const categoriaActiva = this.mostrarDetalleDialog() ? this.selectedCategory() : null;

    // Buscar la categoría completa en la lista plana (necesario para el dropdown)
    const categoriaParaForm = categoriaActiva
      ? this.categorias().find((c) => c.id === categoriaActiva.id) || null
      : null;

    // Valores por defecto
    let initialValues = {
      fecha: new Date(),
      monto: null as number | null,
      descripcion: '',
      categoria: categoriaParaForm,
    };

    // LOGICA DE "CLONAR" ÚLTIMO REGISTRO
    // Si estamos en una categoría, buscamos el último ingreso para pre-llenar datos
    if (categoriaActiva && categoriaActiva.transacciones.length > 0) {
      // Asumimos que están ordenadas o buscamos la más reciente.
      // Por defecto la tabla suele mostrar las recientes primero.
      // Tomamos la primera del array de transacciones (que suele ser la última en cronología si viene del backend ordenado)
      const ultimaTransaccion = categoriaActiva.transacciones[0];

      initialValues.monto = ultimaTransaccion.monto;
      initialValues.descripcion = ultimaTransaccion.descripcion;
      // La fecha la dejamos en hoy, suele ser lo esperado al clonar "repetir gasto/ingreso"
    }

    this.ingresoForm.reset(initialValues);
    this.mostrarDialog.set(true);
  }

  guardarIngreso(): void {
    if (this.ingresoForm.invalid) {
      this.ingresoForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValues = this.ingresoForm.value;

    const transaccionDTO: TransaccionRequest = {
      monto: formValues.monto,
      tipo: 'INGRESO' as TipoTransaccion,
      descripcion: formValues.descripcion,
      fecha: this.toLocalDateString(formValues.fecha),
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
          detail: `Ingreso ${this.esEdicion() ? 'actualizado' : 'creado'} correctamente`,
        });
        this.mostrarDialog.set(false);
        this.cargarIngresos();
        this.saving.set(false);

        // Si estamos en detalle, actualizar la selección
        // (Esto se maneja automáticamente en cargarIngresos con el ID seleccionado)
      },
      error: (err) => {
        console.error('Error guardando:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo guardar el ingreso',
        });
        this.saving.set(false);
      },
    });
  }

  private toLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // ... Resto de métodos (editar, borrar) ...

  editarIngreso(ingreso: Transaccion): void {
    // Implementación básica para demo, ya que formatea el formulario
    // Buscar categoría
    const cat = this.categorias().find((c) => c.id === ingreso.categoriaId);

    this.esEdicion.set(true);
    this.idEdicion.set(ingreso.id);
    this.ingresoForm.patchValue({
      descripcion: ingreso.descripcion,
      monto: ingreso.monto,
      fecha: new Date(ingreso.fecha), // Convert string to Date for DatePicker
      categoria: cat,
    });
    this.mostrarDialog.set(true);
  }

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

  private eliminarIngreso(ingreso: Transaccion): void {
    this.transaccionService.eliminar(ingreso.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Eliminado',
          detail: 'El ingreso fue eliminado correctamente',
        });
        this.cargarIngresos();
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

  // ==================== HELPERS ====================

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }

  formatDate(dateString: string): string {
    const [year, month, day] = dateString.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return new Intl.DateTimeFormat('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(date);
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
