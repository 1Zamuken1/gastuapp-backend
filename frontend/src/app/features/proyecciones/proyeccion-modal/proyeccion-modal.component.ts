import { Component, EventEmitter, Output, Input, inject, signal } from '@angular/core';
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
import { CategoriaService } from '../../../core/services/categoria.service';
import { Proyeccion, TipoTransaccion } from '../../../core/models/proyeccion.model';
import { Categoria } from '../../../core/models/categoria.model';

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
  ],
  templateUrl: './proyeccion-modal.component.html',
  styleUrl: './proyeccion-modal.component.scss',
})
export class ProyeccionModalComponent {
  @Input() visible = false;
  @Input() proyeccionToEdit: Proyeccion | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() proyeccionCreada = new EventEmitter<void>();
  @Output() proyeccionGuardada = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private proyeccionService = inject(ProyeccionService);
  private categoriaService = inject(CategoriaService);
  private messageService = inject(MessageService);

  form: FormGroup;
  loading = signal(false);

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

  categorias = signal<Categoria[]>([]);
  categoriasFiltradas = signal<any[]>([]);

  // Effect helper to handle changes to proyeccionToEdit since it's an Input
  ngOnChanges() {
    if (this.visible && this.proyeccionToEdit) {
      this.cargarDatosEdicion();
    } else if (this.visible && !this.proyeccionToEdit) {
      this.form.reset({
        tipo: 'EGRESO',
        frecuencia: 'MENSUAL',
        fechaInicio: new Date().toISOString().substring(0, 10),
      });
    }
  }

  cargarDatosEdicion() {
    if (!this.proyeccionToEdit) return;

    // Patch simple values
    this.form.patchValue({
      nombre: this.proyeccionToEdit.nombre,
      monto: this.proyeccionToEdit.monto,
      tipo: this.proyeccionToEdit.tipo,
      frecuencia: this.proyeccionToEdit.frecuencia,
      fechaInicio: this.proyeccionToEdit.fechaInicio
        ? new Date(this.proyeccionToEdit.fechaInicio)
        : new Date(),
      categoriaId: this.proyeccionToEdit.categoriaId,
    });

    // Ensure filtering triggers
    this.filtrarCategorias(this.proyeccionToEdit.tipo);
  }

  constructor() {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      monto: [null, [Validators.required, Validators.min(0.01)]],
      tipo: ['EGRESO', Validators.required],
      categoriaId: [null, Validators.required],
      frecuencia: ['MENSUAL', Validators.required],
      fechaInicio: [new Date(), Validators.required],
    });

    // Cargar categorías al inicio
    this.cargarCategorias();

    // Escuchar cambios en tipo para filtrar categorías
    this.form.get('tipo')?.valueChanges.subscribe((tipo) => {
      this.filtrarCategorias(tipo);
      this.form.patchValue({ categoriaId: null });
    });
  }

  cargarCategorias() {
    this.categoriaService.listarPredefinidas().subscribe((cats: Categoria[]) => {
      this.categorias.set(cats);
      this.filtrarCategorias(this.form.get('tipo')?.value);
    });
  }

  filtrarCategorias(tipo: TipoTransaccion) {
    const filtradas = this.categorias()
      .filter((c) => c.tipo === tipo)
      .map((c) => ({
        label: c.nombre,
        value: c.id,
        icon: c.icono,
      }));
    this.categoriasFiltradas.set(filtradas);
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
      ...this.proyeccionToEdit, // Preserve ID and other fields if editing
      nombre: formVal.nombre,
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

  cerrar() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.form.reset({
      tipo: 'EGRESO',
      frecuencia: 'MENSUAL',
      fechaInicio: new Date(),
    });
    this.proyeccionToEdit = null;
  }
}
