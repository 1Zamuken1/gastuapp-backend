import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject,
  signal,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ProyeccionService } from '../../../core/services/proyeccion.service';
import { Proyeccion } from '../../../core/models/proyeccion.model';
import { Transaccion } from '../../../core/models/transaccion.model';

@Component({
  selector: 'app-proyeccion-detalle',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    DialogModule,
    TableModule,
    TooltipModule,
    ConfirmDialogModule,
    DatePipe,
    CurrencyPipe,
  ],
  templateUrl: './proyeccion-detalle.component.html',
  providers: [ConfirmationService],
})
export class ProyeccionDetalleComponent implements OnChanges {
  @Input() visible = false;
  @Input() proyeccion: Proyeccion | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() onEjecucion = new EventEmitter<void>();

  historial = signal<Transaccion[]>([]);
  loadingHistorial = signal(false);
  executing = signal(false);

  private proyeccionService = inject(ProyeccionService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.proyeccion) {
      this.cargarHistorial();
    }
  }

  cargarHistorial() {
    if (!this.proyeccion?.id) return;

    this.loadingHistorial.set(true);
    this.proyeccionService.getHistorial(this.proyeccion.id).subscribe({
      next: (data) => {
        this.historial.set(data);
        this.loadingHistorial.set(false);
      },
      error: () => {
        this.loadingHistorial.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el historial',
        });
      },
    });
  }

  confirmarEjecucion() {
    const categoriaNombre = this.proyeccion?.nombreCategoria || 'esta proyección';
    this.confirmationService.confirm({
      message: `¿Ejecutar la proyección "${categoriaNombre}" y crear la transacción?`,
      header: 'Confirmar Ejecución',
      icon: 'pi pi-play',
      acceptLabel: 'Sí, ejecutar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.ejecutarProyeccion();
      },
    });
  }

  ejecutarProyeccion() {
    if (!this.proyeccion?.id) return;

    this.executing.set(true);
    this.proyeccionService.ejecutar(this.proyeccion.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Proyección ejecutada correctamente',
        });
        this.executing.set(false);
        this.cargarHistorial(); // Recargar historial
        this.onEjecucion.emit(); // Notificar al padre para actualizar lista
      },
      error: () => {
        this.executing.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Falló la ejecución de la proyección',
        });
      },
    });
  }

  cerrar() {
    this.visible = false;
    this.visibleChange.emit(false);
  }
}
