import {
  Component,
  EventEmitter,
  Input,
  Output,
  signal,
  inject,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ProgressBarModule } from 'primeng/progressbar';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';

// Core
import { AhorroService } from '../../../core/services/ahorro.service';
import { MetaAhorro, Ahorro, AhorroRequest, CuotaAhorro } from '../../../core/models/ahorro.model';

@Component({
  selector: 'app-detalle-meta-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DialogModule,
    ButtonModule,
    TableModule,
    ProgressBarModule,
    InputNumberModule,
    InputTextModule,
    TagModule,
    DividerModule,
    TagModule,
    DividerModule,
    TooltipModule,
    ConfirmDialogModule,
  ],
  templateUrl: './detalle-meta-modal.component.html',
  styleUrl: './detalle-meta-modal.component.scss',
  providers: [ConfirmationService],
})
export class DetalleMetaModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() meta: MetaAhorro | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() abonoRegistrado = new EventEmitter<void>();
  @Output() metaDeleted = new EventEmitter<void>();
  @Output() metaEditRequested = new EventEmitter<MetaAhorro>();
  @Output() abonoDeleted = new EventEmitter<void>();

  private ahorroService = inject(AhorroService);
  private confirmationService = inject(ConfirmationService);

  // Estados
  // Estados
  abonos = signal<Ahorro[]>([]);
  cuotas = signal<CuotaAhorro[]>([]);
  activeTab = signal<'plan' | 'historial'>('plan'); // 'plan' | 'historial'
  loadingData = signal(false);
  savingAbono = signal(false);

  // Editar Abono
  mostrarEditarAbono = signal(false);
  abonoAEditar: Ahorro | null = null;
  abonoEditMonto: number | null = null;
  abonoEditDescripcion = '';
  savingEditAbono = signal(false);

  // Form abono
  nuevoAbonoMonto: number | null = null;
  nuevoAbonoDescripcion = '';
  nuevoAbonoCuotaId: number | null = null;
  mostrarFormAbono = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['meta'] && this.meta) {
      this.cargarDatos();
    }
  }

  cargarDatos(): void {
    if (!this.meta) return;
    this.loadingData.set(true);

    // Siempre cargar historial de abonos
    this.ahorroService.listarAbonosPorMeta(this.meta.id).subscribe({
      next: (data) => {
        this.abonos.set(data);
        if (!this.meta?.frecuencia) this.loadingData.set(false);
      },
      error: (err) => {
        console.error('Error cargando abonos', err);
        if (!this.meta?.frecuencia) this.loadingData.set(false);
      },
    });

    // Cargar Cuotas si tiene frecuencia
    if (this.meta.frecuencia) {
      this.activeTab.set('plan');
      this.ahorroService.listarCuotasPorMeta(this.meta.id).subscribe({
        next: (data) => {
          this.cuotas.set(data);
          this.loadingData.set(false);
        },
        error: (err) => {
          console.error('Error cargando cuotas', err);
          this.loadingData.set(false);
        },
      });
    }
  }

  closeModal(): void {
    this.visible = false;
    this.visibleChange.emit(false);
    this.resetFormAbono();
  }

  // Called when PrimeNG dialog changes visible state (e.g., clicking mask)
  onVisibleChange(isVisible: boolean): void {
    if (!isVisible) {
      this.visibleChange.emit(false);
    }
  }

  toggleFormAbono(): void {
    this.mostrarFormAbono = !this.mostrarFormAbono;
    if (!this.mostrarFormAbono) {
      this.resetFormAbono();
    }
  }

  guardarAbono(): void {
    if (!this.meta || !this.nuevoAbonoMonto) return;

    this.savingAbono.set(true);

    const request: AhorroRequest = {
      metaAhorroId: this.meta.id,
      monto: this.nuevoAbonoMonto,
      descripcion: this.nuevoAbonoDescripcion || 'Abono',
      cuotaId: this.nuevoAbonoCuotaId || undefined,
    };

    this.ahorroService.registrarAbono(request).subscribe({
      next: () => {
        this.savingAbono.set(false);
        this.resetFormAbono();
        this.cargarDatos(); // Refresh inteligente
        this.abonoRegistrado.emit();
      },
      error: (err) => {
        console.error('Error registrando abono', err);
        this.savingAbono.set(false);
      },
    });
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

  editarMeta(): void {
    if (this.meta) {
      this.metaEditRequested.emit(this.meta);
      this.closeModal();
    }
  }

  confirmarEliminarMeta(): void {
    this.confirmationService.confirm({
      message: '¿Estás seguro de eliminar esta meta? Se perderá todo el historial.',
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.eliminarMeta(),
    });
  }

  private eliminarMeta(): void {
    if (!this.meta) return;
    this.ahorroService.eliminarMeta(this.meta.id).subscribe({
      next: () => {
        this.metaDeleted.emit();
        this.closeModal();
      },
      error: (err) => console.error('Error eliminando meta', err),
    });
  }

  confirmarEliminarAbono(abono: Ahorro): void {
    this.confirmationService.confirm({
      message: '¿Eliminar este abono? El saldo de la meta se ajustará.',
      header: 'Confirmar Eliminación',
      icon: 'pi pi-info-circle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger p-button-text',
      accept: () => this.eliminarAbono(abono.id),
    });
  }

  private eliminarAbono(id: number): void {
    this.ahorroService.eliminarAbono(id).subscribe({
      next: () => {
        this.cargarAbonos();
        this.abonoDeleted.emit(); // Para refrescar la meta padre si fuera necesario
        // Pero como estamos dentro del modal, necesitamos refrescar la meta localmente?
        // El @Input meta no cambia automáticamente. Deberíamos pedir al padre que refresque.
        // O mejor: emitir evento para que el padre recargue todo.
        this.abonoRegistrado.emit(); // Reutilizamos este evento para indicar "cambio en saldos"
      },
      error: (err) => console.error('Error eliminando abono', err),
    });
  }

  abrirEditarAbono(abono: Ahorro): void {
    this.abonoAEditar = abono;
    this.abonoEditMonto = abono.monto;
    this.abonoEditDescripcion = abono.descripcion;
    this.mostrarEditarAbono.set(true);
  }

  cancelarEdicionAbono(): void {
    this.mostrarEditarAbono.set(false);
    this.abonoAEditar = null;
    this.abonoEditMonto = null;
    this.abonoEditDescripcion = '';
  }

  guardarEdicionAbono(): void {
    if (!this.abonoAEditar || !this.abonoEditMonto) return;

    this.savingEditAbono.set(true);
    const request: AhorroRequest = {
      metaAhorroId: this.meta!.id, // No cambiamos la meta
      monto: this.abonoEditMonto,
      descripcion: this.abonoEditDescripcion || 'Abono',
    };

    this.ahorroService.actualizarAbono(this.abonoAEditar.id, request).subscribe({
      next: () => {
        this.savingEditAbono.set(false);
        this.cancelarEdicionAbono();
        this.cargarAbonos();
        this.abonoRegistrado.emit(); // Para actualizar saldos padre
      },
      error: (err) => {
        console.error('Error actualizando abono', err);
        this.savingEditAbono.set(false);
      },
    });
  }

  private resetFormAbono(): void {
    this.nuevoAbonoMonto = null;
    this.nuevoAbonoDescripcion = '';
    this.nuevoAbonoCuotaId = null;
    this.mostrarFormAbono = false;
  }

  // Lógica Cuotas
  inicioPagoCuota(cuota: CuotaAhorro): void {
    this.nuevoAbonoCuotaId = cuota.id;
    this.nuevoAbonoMonto = cuota.montoEsperado;
    this.nuevoAbonoDescripcion = `Pago Cuota #${cuota.numeroCuota}`;
    this.mostrarFormAbono = true;
  }

  isCuotaActiva(cuota: CuotaAhorro): boolean {
    if (cuota.estado !== 'PENDIENTE') return false;

    // Solo habilitar cuotas cuya fecha programada esté dentro de los próximos 7 días
    const hoy = new Date();
    const fechaCuota = new Date(cuota.fechaProgramada);
    const diferenciaDias = Math.floor(
      (fechaCuota.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24),
    );

    // Habilitar si la fecha está dentro de 7 días (o ya pasó)
    return diferenciaDias <= 7;
  }

  pagarCuotaDesdeTabla(cuota: CuotaAhorro): void {
    if (!this.meta || !cuota.montoEsperado) return;

    // Usamos el mismo estado de carga, o podríamos usar uno específico si quisiéramos spinner en el botón
    this.savingAbono.set(true);

    const request: AhorroRequest = {
      metaAhorroId: this.meta.id,
      monto: cuota.montoEsperado,
      descripcion: `Pago Cuota #${cuota.numeroCuota}`,
      cuotaId: cuota.id,
    };

    this.ahorroService.registrarAbono(request).subscribe({
      next: () => {
        this.savingAbono.set(false);
        this.cargarDatos();
        this.abonoRegistrado.emit();
      },
      error: (err) => {
        console.error('Error pagando cuota', err);
        this.savingAbono.set(false);
      },
    });
  }

  // Reutilizamos cargarAbonos para compatibilidad si se llama extrañamente
  cargarAbonos(): void {
    this.cargarDatos();
  }
}
