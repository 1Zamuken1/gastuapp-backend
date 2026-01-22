/**
 * Component: DashboardComponent
 *
 * FLUJO DE DATOS:
 * - RECIBE: Datos de TransaccionService (resumen, balance)
 * - RENDERIZA: Cards con información financiera
 * - PROVEE: Vista principal del usuario
 *
 * RESPONSABILIDAD:
 * Vista principal que muestra el resumen financiero.
 * Presenta balance actual, total de ingresos/egresos del mes.
 * Utiliza signals para reactividad.
 *
 * CARDS MOSTRADAS:
 * - Balance actual (ingresos - egresos)
 * - Total ingresos del mes
 * - Total egresos del mes
 * - Cantidad de transacciones
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';

// Services & Models
import { TransaccionService } from '../../core/services/transaccion.service';
import { ResumenFinanciero } from '../../core/models/transaccion.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CardModule, ButtonModule, SkeletonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  // Signals para el estado
  loading = signal(true);
  error = signal<string | null>(null);

  // Datos del resumen
  resumen = signal<ResumenFinanciero>({
    totalIngresos: 0,
    totalEgresos: 0,
    balance: 0,
    cantidadTransacciones: 0,
  });

  constructor(private transaccionService: TransaccionService) {}

  ngOnInit(): void {
    this.cargarResumen();
  }

  /**
   * Carga el resumen financiero desde el backend
   */
  cargarResumen(): void {
    this.loading.set(true);
    this.error.set(null);

    this.transaccionService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando resumen:', err);
        // Si no hay datos, mostramos valores vacíos (no error para demo)
        this.loading.set(false);
        // Comentado para demo sin backend:
        // this.error.set('Error al cargar el resumen financiero');
      },
    });
  }

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
}
