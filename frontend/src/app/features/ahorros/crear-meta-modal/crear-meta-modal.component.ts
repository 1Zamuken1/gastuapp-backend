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

// Core
import { AhorroService } from '../../../core/services/ahorro.service';
import { MetaAhorroRequest, MetaAhorro, FrecuenciaAhorro } from '../../../core/models/ahorro.model';
import { IconSelectorComponent } from '../../../shared/components/icon-selector/icon-selector.component';

@Component({
  selector: 'app-crear-meta-modal',
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
    IconSelectorComponent,
  ],
  templateUrl: './crear-meta-modal.component.html',
  styleUrl: './crear-meta-modal.component.scss',
})
export class CrearMetaModalComponent implements OnInit {
  @Input() visible = false;
  @Input() metaToEdit: MetaAhorro | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() metaCreated = new EventEmitter<void>();
  @Output() metaUpdated = new EventEmitter<void>();

  private ahorroService = inject(AhorroService);

  // Form model
  nombre = '';
  montoObjetivo: number | null = null;
  fechaLimite: Date | null = null;
  fechaInicio: Date | null = new Date(); // Default Hoy
  frecuenciaSeleccionada: FrecuenciaAhorro | null = null;
  iconoSeleccionado = 'pi pi-wallet';
  colorSeleccionado = '#f59e0b';

  saving = signal(false);

  // Fecha mínima (hoy)
  minDate = new Date();

  // Selector de ícono
  mostrarSelectorIcono = false;

  onIconSelected(icon: string): void {
    this.iconoSeleccionado = icon;
    this.mostrarSelectorIcono = false;
  }
  iconos = [
    { label: 'Dinero', value: 'pi pi-wallet', icon: 'pi pi-wallet' },
    { label: 'Casa', value: 'pi pi-home', icon: 'pi pi-home' },
    { label: 'Auto', value: 'pi pi-car', icon: 'pi pi-car' },
    { label: 'Viaje', value: 'pi pi-globe', icon: 'pi pi-globe' },
    { label: 'Tech', value: 'pi pi-mobile', icon: 'pi pi-mobile' },
    { label: 'Estudios', value: 'pi pi-book', icon: 'pi pi-book' },
    { label: 'Salud', value: 'pi pi-heart', icon: 'pi pi-heart' },
    { label: 'Regalo', value: 'pi pi-gift', icon: 'pi pi-gift' },
    { label: 'Compras', value: 'pi pi-shopping-cart', icon: 'pi pi-shopping-cart' },
    { label: 'Otro', value: 'pi pi-box', icon: 'pi pi-box' },
  ];

  // Círculo de Colores (12 segmentos)
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

  frecuencias: { label: string; value: FrecuenciaAhorro }[] = [
    { label: 'Diario', value: 'DIARIO' },
    { label: 'Semanal', value: 'SEMANAL' },
    { label: 'Quincenal', value: 'QUINCENAL' },
    { label: 'Mensual', value: 'MENSUAL' },
    { label: 'Trimestral', value: 'TRIMESTRAL' },
    { label: 'Semestral', value: 'SEMESTRAL' },
    { label: 'Anual', value: 'ANUAL' },
  ];

  ngOnInit(): void {}

  ngOnChanges(): void {
    if (this.visible && this.metaToEdit) {
      this.cargarDatosEdicion();
    } else if (this.visible && !this.metaToEdit) {
      this.resetForm();
    }
  }

  private cargarDatosEdicion(): void {
    if (!this.metaToEdit) return;
    this.nombre = this.metaToEdit.nombre;
    this.montoObjetivo = this.metaToEdit.montoObjetivo;
    this.fechaLimite = this.metaToEdit.fechaLimite ? new Date(this.metaToEdit.fechaLimite) : null;
    this.fechaInicio = this.metaToEdit.fechaInicio
      ? new Date(this.metaToEdit.fechaInicio)
      : new Date();
    this.frecuenciaSeleccionada = this.metaToEdit.frecuencia || null;
    this.iconoSeleccionado = this.metaToEdit.icono || 'pi pi-wallet';
    this.colorSeleccionado = this.metaToEdit.color || '#f59e0b';
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

  guardarMeta(): void {
    if (!this.nombre || !this.montoObjetivo) {
      return;
    }

    this.saving.set(true);

    const request: MetaAhorroRequest = {
      nombre: this.nombre,
      montoObjetivo: this.montoObjetivo,
      fechaLimite: this.fechaLimite ? this.fechaLimite.toISOString() : '',
      fechaInicio: this.fechaInicio ? this.fechaInicio.toISOString() : '',
      frecuencia: this.frecuenciaSeleccionada || undefined,
      color: this.colorSeleccionado,
      icono: this.iconoSeleccionado,
    };

    if (this.metaToEdit) {
      this.ahorroService.actualizarMeta(this.metaToEdit.id, request).subscribe({
        next: () => {
          this.saving.set(false);
          this.metaUpdated.emit();
          this.closeModal();
        },
        error: (err) => {
          console.error('Error actualizando meta', err);
          this.saving.set(false);
        },
      });
    } else {
      this.ahorroService.crearMeta(request).subscribe({
        next: () => {
          this.saving.set(false);
          this.metaCreated.emit();
          this.closeModal();
        },
        error: (err) => {
          console.error('Error creando meta', err);
          this.saving.set(false);
        },
      });
    }
  }

  private resetForm(): void {
    this.nombre = '';
    this.montoObjetivo = null;
    this.montoObjetivo = null;
    this.fechaLimite = null;
    this.fechaInicio = new Date();
    this.frecuenciaSeleccionada = null;
    this.iconoSeleccionado = 'pi pi-wallet';
    this.colorSeleccionado = '#f59e0b';
  }
}
