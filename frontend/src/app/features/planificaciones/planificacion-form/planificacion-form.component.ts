import { Component, EventEmitter, Input, Output, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { ColorPickerModule } from 'primeng/colorpicker';
import { CheckboxModule } from 'primeng/checkbox';
import { ToggleSwitchModule } from 'primeng/toggleswitch';

// Core
import { PlanificacionService } from '../../../core/services/planificacion.service';
import {
  Planificacion,
  FrecuenciaPlanificacion,
  PlanificacionUtils,
} from '../../../core/models/planificacion.model';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria } from '../../../core/models/categoria.model';

// Shared Components
import { CategorySelectorModal } from '../../../shared/components/category-selector-modal/category-selector-modal';

@Component({
  selector: 'app-planificacion-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    ColorPickerModule,
    CheckboxModule,
    ToggleSwitchModule,
    CategorySelectorModal,
  ],
  templateUrl: './planificacion-form.component.html',
  styleUrl: './planificacion-form.component.scss',
})
export class PlanificacionFormComponent implements OnInit {
  @Input() visible = false;
  @Input() planificacionToEdit: Planificacion | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() planificacionCreated = new EventEmitter<Planificacion>();
  @Output() planificacionUpdated = new EventEmitter<Planificacion>();

  private planificacionService = inject(PlanificacionService);
  private categoriaService = inject(CategoriaService);

  // Form model
  categoriaSeleccionada: Categoria | null = null;
  montoTope: number | null = null;
  montoGastado: number = 0; // Se calcula automáticamente
  fechaInicio: Date | null = new Date(); // Default primer día del mes
  fechaFin: Date | null = null; // Default último día del mes
  frecuenciaSeleccionada: FrecuenciaPlanificacion | null = FrecuenciaPlanificacion.MENSUAL;
  autoRenovar = true; // Default true para presupuestos
  colorSeleccionado = '#6366f1'; // Color por defecto para planificaciones
  nombrePersonalizado = ''; // Campo para nombre personalizado opcional

  saving = signal(false);
  loadingCategorias = signal(false);
  showCategorySelector = signal(false);
  categorias: Categoria[] = [];

  // Fecha mínima (hoy)
  minDate = new Date();

  // Círculo de Colores (12 segmentos - arcoíris)
  coloresRueda = [
    '#f87171', // Red
    '#fb923c', // Orange
    '#fbbf24', // Amber
    '#a3e635', // Lime
    '#4ade80', // Green
    '#34d399', // Emerald
    '#22d3ee', // Cyan
    '#60a5fa', // Blue
    '#818cf8', // Indigo
    '#a78bfa', // Violet
    '#e879f9', // Fuchsia
    '#fb7185', // Rose
  ];

  frecuencias: { label: string; value: FrecuenciaPlanificacion }[] = [
    { label: 'Semanal', value: FrecuenciaPlanificacion.SEMANAL },
    { label: 'Quincenal', value: FrecuenciaPlanificacion.QUINCENAL },
    { label: 'Mensual', value: FrecuenciaPlanificacion.MENSUAL },
    { label: 'Bimestral', value: FrecuenciaPlanificacion.BIMESTRAL },
    { label: 'Trimestral', value: FrecuenciaPlanificacion.TRIMESTRAL },
    { label: 'Semestral', value: FrecuenciaPlanificacion.SEMESTRAL },
    { label: 'Anual', value: FrecuenciaPlanificacion.ANUAL },
  ];

  ngOnInit(): void {
    this.cargarCategorias();
    this.setFechasPorDefecto();
  }

  ngOnChanges(): void {
    if (this.visible && this.planificacionToEdit) {
      this.cargarDatosEdicion();
    } else if (this.visible && !this.planificacionToEdit) {
      this.resetForm();
      this.setFechasPorDefecto();
    }
  }

  private cargarCategorias(): void {
    this.loadingCategorias.set(true);
    this.categoriaService.listarPorTipo('EGRESO').subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.loadingCategorias.set(false);
      },
      error: (err) => {
        console.error('Error cargando categorías', err);
        this.loadingCategorias.set(false);
      },
    });
  }

  private setFechasPorDefecto(): void {
    const hoy = new Date();
    const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);

    this.fechaInicio = primerDia;
    this.fechaFin = ultimoDia;
  }

  onFrecuenciaChange(): void {
    // Ajustar fechas según la frecuencia seleccionada
    if (!this.fechaInicio) return;

    const inicio = new Date(this.fechaInicio);
    let fin: Date;

    switch (this.frecuenciaSeleccionada) {
      case 'SEMANAL':
        fin = new Date(inicio.getTime() + 6 * 24 * 60 * 60 * 1000); // 7 días
        break;
      case 'QUINCENAL':
        fin = new Date(inicio.getTime() + 14 * 24 * 60 * 60 * 1000); // 15 días
        break;
      case 'MENSUAL':
        fin = new Date(inicio.getFullYear(), inicio.getMonth() + 1, 0);
        break;
      case 'BIMESTRAL':
        fin = new Date(inicio.getFullYear(), inicio.getMonth() + 2, 0);
        break;
      case 'TRIMESTRAL':
        fin = new Date(inicio.getFullYear(), inicio.getMonth() + 3, 0);
        break;
      case 'SEMESTRAL':
        fin = new Date(inicio.getFullYear(), inicio.getMonth() + 6, 0);
        break;
      case 'ANUAL':
        fin = new Date(inicio.getFullYear() + 1, inicio.getMonth(), 0);
        break;
      default:
        fin = new Date(inicio.getFullYear(), inicio.getMonth() + 1, 0);
    }

    this.fechaFin = fin;
  }

  private cargarDatosEdicion(): void {
    if (!this.planificacionToEdit) return;

    // Buscar categoría por nombre
    this.categoriaSeleccionada =
      this.categorias.find((c) => c.nombre === this.planificacionToEdit!.categoriaNombre) || null;

    this.montoTope = this.planificacionToEdit.montoTope;
    this.montoGastado = this.planificacionToEdit.montoGastado || 0;
    this.fechaInicio = this.planificacionToEdit.fechaInicio
      ? new Date(this.planificacionToEdit.fechaInicio)
      : null;
    this.fechaFin = this.planificacionToEdit.fechaFin
      ? new Date(this.planificacionToEdit.fechaFin)
      : null;
    this.frecuenciaSeleccionada =
      this.planificacionToEdit.frecuencia || FrecuenciaPlanificacion.MENSUAL;
    this.autoRenovar = this.planificacionToEdit.autoRenovar || false;
    this.colorSeleccionado = this.planificacionToEdit.color || '#6366f1';
    this.nombrePersonalizado = this.planificacionToEdit.nombrePersonalizado || '';
  }

  closeModal(): void {
    this.resetForm();
    this.visible = false;
    this.visibleChange.emit(false);
  }

  // Called when PrimeNG dialog changes visible state (e.g., clicking mask)
  onVisibleChange(isVisible: boolean): void {
    if (!isVisible) {
      this.visibleChange.emit(false);
    }
  }

  guardarPlanificacion(): void {
    if (!this.categoriaSeleccionada || !this.montoTope || !this.fechaInicio || !this.fechaFin) {
      return;
    }

    this.saving.set(true);

    const request = {
      categoriaId: this.categoriaSeleccionada.id,
      montoTope: this.montoTope,
      fechaInicio: this.fechaInicio.toISOString(),
      fechaFin: this.fechaFin.toISOString(),
      frecuencia: this.frecuenciaSeleccionada,
      autoRenovar: this.autoRenovar,
      color: this.colorSeleccionado,
      nombrePersonalizado: this.nombrePersonalizado.trim() || undefined,
    };

    if (this.planificacionToEdit) {
      this.planificacionService
        .actualizarPlanificacion(this.planificacionToEdit.publicId, request)
        .subscribe({
          next: (planificacion) => {
            this.saving.set(false);
            this.planificacionUpdated.emit(planificacion);
            this.closeModal();
          },
          error: (err) => {
            console.error('Error actualizando planificación', err);
            this.saving.set(false);
          },
        });
    } else {
      this.planificacionService.crearPlanificacion(request).subscribe({
        next: (planificacion) => {
          this.saving.set(false);
          this.planificacionCreated.emit(planificacion);
          this.closeModal();
        },
        error: (err) => {
          console.error('Error creando planificación', err);
          this.saving.set(false);
        },
      });
    }
  }

  private resetForm(): void {
    this.categoriaSeleccionada = null;
    this.montoTope = null;
    this.montoGastado = 0;
    this.fechaInicio = null;
    this.fechaFin = null;
    this.frecuenciaSeleccionada = FrecuenciaPlanificacion.MENSUAL;
    this.autoRenovar = true;
    this.colorSeleccionado = '#6366f1';
    this.nombrePersonalizado = '';
  }

  // Getters para template
  get tituloModal(): string {
    return this.planificacionToEdit ? 'Editar Presupuesto' : 'Crear Nuevo Presupuesto';
  }

  get nombreMostrado(): string {
    if (this.nombrePersonalizado.trim()) {
      return this.nombrePersonalizado.trim();
    }
    return this.categoriaSeleccionada
      ? `Presupuesto: ${this.categoriaSeleccionada.nombre}`
      : 'Nuevo Presupuesto';
  }

  get esFormularioValido(): boolean {
    return !!(
      this.categoriaSeleccionada &&
      this.montoTope &&
      this.montoTope > 0 &&
      this.fechaInicio &&
      this.fechaFin &&
      this.fechaFin > this.fechaInicio
    );
  }

  // Métodos de utilidad usando PlanificacionUtils
  getFrecuenciaIcono(frecuencia: FrecuenciaPlanificacion | null): string {
    return frecuencia ? PlanificacionUtils.getFrecuenciaIcono(frecuencia) : 'pi pi-calendar';
  }

  getFrecuenciaTexto(frecuencia: FrecuenciaPlanificacion | null): string {
    return frecuencia ? PlanificacionUtils.getFrecuenciaTexto(frecuencia) : 'Sin frecuencia';
  }

  // Handler para cuando se selecciona una categoría del modal
  onCategorySelected(categoria: Categoria): void {
    this.categoriaSeleccionada = categoria;
  }
}
