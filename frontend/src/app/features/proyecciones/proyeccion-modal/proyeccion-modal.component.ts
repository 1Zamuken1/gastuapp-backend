import {
  Component,
  EventEmitter,
  Output,
  Input,
  inject,
  signal,
  computed,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageService } from 'primeng/api';
import { ProyeccionService } from '../../../core/services/proyeccion.service';
import { Proyeccion, TipoTransaccion } from '../../../core/models/proyeccion.model';
import { Categoria } from '../../../core/models/categoria.model';
import { CategorySelectorModal } from '../../../shared/components/category-selector-modal/category-selector-modal';

@Component({
  selector: 'app-proyeccion-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    DatePickerModule,
    CategorySelectorModal,
  ],
  templateUrl: './proyeccion-modal.component.html',
  styleUrl: './proyeccion-modal.component.scss',
})
export class ProyeccionModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() proyeccionToEdit: Proyeccion | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() proyeccionCreada = new EventEmitter<void>();
  @Output() proyeccionGuardada = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private proyeccionService = inject(ProyeccionService);
  private messageService = inject(MessageService);

  form: FormGroup;
  loading = signal(false);

  // Signal para la categoría seleccionada (display only)
  selectedCategory = signal<Partial<Categoria> | null>(null);

  tipos = [
    { label: 'Ingreso', value: 'INGRESO' },
    { label: 'Egreso', value: 'EGRESO' },
  ];

  frecuencias = [
    { label: 'Semanal', value: 'SEMANAL' },
    { label: 'Quincenal', value: 'QUINCENAL' },
    { label: 'Mensual', value: 'MENSUAL' },
    { label: 'Bimestral', value: 'BIMESTRAL' },
    { label: 'Semestral', value: 'SEMESTRAL' },
    { label: 'Anual', value: 'ANUAL' },
    { label: 'Única', value: 'UNICA' },
  ];

  categorySelectorVisible = false;

  constructor() {
    this.form = this.fb.group({
      monto: [null, [Validators.required, Validators.min(0.01)]],
      tipo: ['EGRESO', Validators.required],
      categoriaId: [null, Validators.required],
      frecuencia: ['MENSUAL', Validators.required],
      fechaInicio: [new Date(), Validators.required],
    });

    // Escuchar cambios en tipo para limpiar categoría seleccionada si es incompatible (?)
    // Opcional: Si cambia tipo, la categoría actual probablemente ya no sea válida
    this.form.get('tipo')?.valueChanges.subscribe((newTipo) => {
      const currentCat = this.selectedCategory();
      if (currentCat && currentCat.tipo && currentCat.tipo !== newTipo) {
        this.selectedCategory.set(null);
        this.form.patchValue({ categoriaId: null });
      }
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['visible'] && this.visible) {
      if (this.proyeccionToEdit) {
        this.cargarDatosEdicion();
      } else {
        this.resetForm();
      }
    }
  }

  resetForm() {
    this.form.reset({
      tipo: 'EGRESO',
      frecuencia: 'MENSUAL',
      fechaInicio: new Date(),
    });
    this.selectedCategory.set(null);
    this.loading.set(false);
  }

  cargarDatosEdicion() {
    if (!this.proyeccionToEdit) return;

    // Patch values
    this.form.patchValue({
      monto: this.proyeccionToEdit.monto,
      tipo: this.proyeccionToEdit.tipo,
      frecuencia: this.proyeccionToEdit.frecuencia,
      fechaInicio: this.proyeccionToEdit.fechaInicio
        ? new Date(this.proyeccionToEdit.fechaInicio)
        : new Date(),
      categoriaId: this.proyeccionToEdit.categoriaId,
    });

    // Set selected category for display using metadata if available
    if (this.proyeccionToEdit.categoriaId) {
      this.selectedCategory.set({
        id: this.proyeccionToEdit.categoriaId,
        nombre: this.proyeccionToEdit.nombreCategoria || 'Cargando...',
        icono: this.proyeccionToEdit.iconoCategoria || '📋',
        tipo: this.proyeccionToEdit.tipo,
      });
    }
  }

  guardar() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const formVal = this.form.value;

    // Convert Date to ISO string for API
    const fechaISO =
      formVal.fechaInicio instanceof Date
        ? formVal.fechaInicio.toISOString().substring(0, 10)
        : formVal.fechaInicio;

    const proyeccionData: Proyeccion = {
      ...this.proyeccionToEdit, // Preserve ID
      monto: formVal.monto,
      tipo: formVal.tipo,
      categoriaId: formVal.categoriaId,
      frecuencia: formVal.frecuencia,
      fechaInicio: fechaISO,
    };

    const request$ =
      this.proyeccionToEdit && this.proyeccionToEdit.id
        ? this.proyeccionService.actualizarProyeccion(this.proyeccionToEdit.id, proyeccionData)
        : this.proyeccionService.crearProyeccion(proyeccionData);

    request$.subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Proyección ${this.proyeccionToEdit ? 'actualizada' : 'creada'} correctamente`,
        });
        this.loading.set(false);
        this.proyeccionGuardada.emit();
        this.cerrar();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `No se pudo ${this.proyeccionToEdit ? 'actualizar' : 'crear'} la proyección`,
        });
        this.loading.set(false);
      },
    });
  }

  // Open category selector modal
  abrirSelectorCategoria() {
    this.categorySelectorVisible = true;
  }

  // Handle category selection from modal
  onCategoriaSeleccionada(categoria: Categoria) {
    this.form.patchValue({ categoriaId: categoria.id });
    this.selectedCategory.set(categoria);
    this.categorySelectorVisible = false;
  }

  cerrar() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.resetForm();
    this.proyeccionToEdit = null;
  }
}
