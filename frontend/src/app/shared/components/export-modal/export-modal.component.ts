import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { RadioButtonModule } from 'primeng/radiobutton';
import { DatePickerModule } from 'primeng/datepicker';
import { DividerModule } from 'primeng/divider';

// Core
import { ExportModalConfig } from '../../models/export.model';
import { ExportService } from '../../../core/services/export.service';

@Component({
  selector: 'app-export-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    CheckboxModule,
    RadioButtonModule,
    DatePickerModule,
    DividerModule,
  ],
  templateUrl: './export-modal.component.html',
  styleUrl: './export-modal.component.scss',
})
export class ExportModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() config!: ExportModalConfig;
  @Output() visibleChange = new EventEmitter<boolean>();

  form: FormGroup | undefined;

  private fb = inject(FormBuilder);
  private exportService = inject(ExportService);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] && this.config) {
      this.buildForm();
    }
  }

  close(): void {
    this.visible = false;
    this.visibleChange.emit(false);
  }

  private buildForm(): void {
    const controls: any = {
      format: ['pdf'], // Default format
      detalle: ['detallado'],
    };

    // Crear controles dinámicos según config
    this.config.fields.forEach((field) => {
      if (field.type === 'checkbox-group') {
        // Para checkboxes, creamos un FormGroup anidado donde checkId -> boolean
        const group: any = {};
        field.options?.forEach((opt) => {
          group[opt.value] = [opt.checked !== false]; // Default true
        });
        controls[field.key] = this.fb.group(group);
      } else if (field.type === 'daterange') {
        controls[field.key] = [null];
      } else if (field.type === 'radio') {
        controls[field.key] = [field.options?.[0].value];
      }
    });

    this.form = this.fb.group(controls);
  }

  toggleAll(key: string, state: boolean): void {
    const group = this.form?.get(key) as FormGroup;
    if (group) {
      Object.keys(group.controls).forEach((k) => {
        group.get(k)?.setValue(state);
      });
    }
  }

  onExport(): void {
    if (!this.form || !this.config) return;

    const val = this.form.value;
    const format = val.format;

    // 1. Filtrar Datos
    let filteredData = [...this.config.data];

    // a) Filtrar por categorías (checkboxes)
    // Asumimos que si hay un campo "categorias", filtramos por "categoriaId"
    if (val.categorias) {
      const selectedIds = Object.keys(val.categorias)
        .filter((k) => val.categorias[k]) // Solo los true
        .map(Number); // Convertir keys a numeros

      filteredData = filteredData.filter((item) => selectedIds.includes(item.categoriaId));
    }

    // b) Filtrar por Fechas
    if (val.fechas && val.fechas[0] && val.fechas[1]) {
      const start = new Date(val.fechas[0]);
      const end = new Date(val.fechas[1]);
      // Ajustar end al final del día
      end.setHours(23, 59, 59);

      filteredData = filteredData.filter((item) => {
        const date = new Date(item.fecha);
        return date >= start && date <= end;
      });
    }

    // 2. Ejecutar Exportación
    const filename = `Reporte_${this.config.moduleType}_${new Date().getTime()}`;

    switch (format) {
      case 'excel':
        this.exportService.exportToExcel(filteredData, this.config.columns, filename);
        break;
      case 'csv':
        this.exportService.exportToCSV(filteredData, this.config.columns, filename);
        break;
      case 'pdf':
        this.exportService.exportToPDF(
          filteredData,
          this.config.columns,
          filename,
          this.config.title,
        );
        break;
    }

    this.close();
  }
}
