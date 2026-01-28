import { Component, OnInit, signal, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { ChartModule } from 'primeng/chart';

// Services & Models
import { TransaccionService } from '../../core/services/transaccion.service';
import { PlanificacionService } from '../../core/services/planificacion.service';
import { AhorroService } from '../../core/services/ahorro.service';
import { ResumenFinanciero, Transaccion } from '../../core/models/transaccion.model';
import { Planificacion } from '../../core/models/planificacion.model';
import { MetaAhorro } from '../../core/models/ahorro.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CardModule, ButtonModule, SkeletonModule, ChartModule],
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

  // Chart Data & Options
  lineChartData: any;
  lineChartOptions: any;

  barChartData: any;
  barChartOptions: any;

  stackedBarData: any;
  stackedBarOptions: any;

  doughnutData: any;
  doughnutOptions: any;

  private transaccionService = inject(TransaccionService);
  private planificacionService = inject(PlanificacionService);
  private ahorroService = inject(AhorroService);
  private platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  /**
   * Carga todos los datos necesarios para el dashboard
   */
  cargarDatosDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    // Usamos forkJoin para cargar todo en paralelo
    forkJoin({
      resumen: this.transaccionService.obtenerResumen(),
      transacciones: this.transaccionService.listarTodas(), // Para gráfico de líneas y dona
      planificaciones: this.planificacionService.listarActivas(),
      metasAhorro: this.ahorroService.listarMetas(),
    }).subscribe({
      next: (data) => {
        this.resumen.set(data.resumen);

        if (isPlatformBrowser(this.platformId)) {
          this.initLineChart(data.transacciones);
          this.initPlanificacionChart(data.planificaciones);
          this.initAhorroChart(data.metasAhorro);
          this.initGastosChart(data.transacciones);
        }

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando datos del dashboard:', err);
        this.loading.set(false);
        // this.error.set('Error al cargar información del dashboard.');
      },
    });
  }

  // 1. Gráfico de Líneas: Ingresos vs Egresos
  initLineChart(transacciones: Transaccion[]) {
    // Agrupar por mes (últimos 6 meses)
    const meses = [
      'Ene',
      'Feb',
      'Mar',
      'Abr',
      'May',
      'Jun',
      'Jul',
      'Ago',
      'Sep',
      'Oct',
      'Nov',
      'Dic',
    ];
    const currentMonth = new Date().getMonth();

    // Simplificación: Mostramos datos ficticios o procesados reales
    // Para el ejemplo real, deberíamos procesar las transacciones por fecha.
    // Aquí haremos un procesamiento básico de los últimos 6 meses.

    const labels = [];
    const ingresosData = [];
    const egresosData = [];

    // Generar labels para últimos 6 meses
    for (let i = 5; i >= 0; i--) {
      const d = new Date();
      d.setMonth(d.getMonth() - i);
      labels.push(meses[d.getMonth()]);

      // Filtrar transacciones para este mes/año (lógica simplificada)
      const transaccionesMes = transacciones.filter((t) => {
        const tDate = new Date(t.fecha);
        return tDate.getMonth() === d.getMonth() && tDate.getFullYear() === d.getFullYear();
      });

      const ing = transaccionesMes
        .filter((t) => t.tipo === 'INGRESO')
        .reduce((sum, t) => sum + t.monto, 0);

      const egr = transaccionesMes
        .filter((t) => t.tipo === 'EGRESO')
        .reduce((sum, t) => sum + t.monto, 0);

      ingresosData.push(ing);
      egresosData.push(egr);
    }

    this.lineChartData = {
      labels: labels,
      datasets: [
        {
          label: 'Ingresos',
          data: ingresosData,
          fill: true,
          borderColor: '#22c55e', // green-500
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          tension: 0.4,
        },
        {
          label: 'Egresos',
          data: egresosData,
          fill: true,
          borderColor: '#f97316', // orange-500
          backgroundColor: 'rgba(249, 115, 22, 0.1)',
          tension: 0.4,
        },
      ],
    };

    this.lineChartOptions = {
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        legend: { labels: { color: '#94a3b8' } },
      },
      scales: {
        x: {
          ticks: { color: '#94a3b8' },
          grid: { color: 'rgba(148, 163, 184, 0.1)' },
        },
        y: {
          ticks: { color: '#94a3b8', callback: (value: any) => '$' + value },
          grid: { color: 'rgba(148, 163, 184, 0.1)' },
        },
      },
    };
  }

  // 2. Gráfico Barras Horizontales: Planificaciones (Presupuesto)
  initPlanificacionChart(planificaciones: Planificacion[]) {
    // Extraer datos
    const labels = planificaciones.map(
      (p) => p.categoriaNombre || p.nombrePersonalizado || 'Sin nombre',
    );
    const consumido = planificaciones.map((p) => p.montoGastado);
    // Para mostrar el "tope", podríamos usar una barra de fondo o un dataset comparativo.
    // Aquí mostraremos el % consumido vs restante o simplemente el monto gastado.
    // El requerimiento dice: "cuánto es el tope y cuanto porcentaje (y monto) lleva".
    // Usaremos monto gastado y una tooltip avanzada o dataset de "Restante" stacked.

    // Mejor enfoque: Barras horizontales simples mostrando el progreso

    const colors = planificaciones.map((p) => p.color || '#6366f1'); // Usar color del modelo

    this.barChartData = {
      labels: labels,
      datasets: [
        {
          label: 'Gastado',
          data: consumido,
          backgroundColor: colors,
          borderColor: colors,
          borderWidth: 1,
          barPercentage: 0.6,
        },
        // Dataset opcional para el tope visual (fondo gris claro?)
        // Por ahora mantenemos simple.
      ],
    };

    this.barChartOptions = {
      indexAxis: 'y',
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        legend: { display: false }, // Colores variables, leyenda no tiene sentido único
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const p = planificaciones[context.dataIndex];
              const percent = p.porcentajeUtilizacion.toFixed(1);
              return `Gastado: ${this.formatCurrency(p.montoGastado)} / ${this.formatCurrency(p.montoTope)} (${percent}%)`;
            },
          },
        },
      },
      scales: {
        x: {
          ticks: { color: '#94a3b8' },
          grid: { color: 'rgba(148, 163, 184, 0.1)' },
        },
        y: {
          ticks: { color: '#94a3b8' },
          grid: { display: false },
        },
      },
    };
  }

  // 3. Stacked Bar: Ahorros (Metas y estado)
  initAhorroChart(metas: MetaAhorro[]) {
    const labels = metas.map((m) => m.nombre);
    const actual = metas.map((m) => m.montoActual);
    const restante = metas.map((m) => Math.max(0, m.montoObjetivo - m.montoActual));
    const colors = metas.map((m) => m.color || '#3b82f6'); // Color específico

    this.stackedBarData = {
      labels: labels,
      datasets: [
        {
          type: 'bar',
          label: 'Ahorrado',
          backgroundColor: colors,
          data: actual,
        },
        {
          type: 'bar',
          label: 'Faltante',
          backgroundColor: 'rgba(148, 163, 184, 0.2)', // Gris suave
          data: restante,
        },
      ],
    };

    this.stackedBarOptions = {
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        tooltip: {
          mode: 'index',
          intersect: false,
        },
        legend: { display: true, labels: { color: '#94a3b8' } },
      },
      scales: {
        x: {
          stacked: true,
          ticks: { color: '#94a3b8' },
          grid: { display: false },
        },
        y: {
          stacked: true,
          ticks: { color: '#94a3b8' },
          grid: { color: 'rgba(148, 163, 184, 0.1)' },
        },
      },
    };
  }

  // 4. Dona: Principales Egresos
  initGastosChart(transacciones: Transaccion[]) {
    // Filtrar egresos y agrupar por categoría
    const egresos = transacciones.filter((t) => t.tipo === 'EGRESO');
    const gastosPorCategoria = new Map<string, number>();

    egresos.forEach((t) => {
      const cat = t.categoriaNombre || 'Otros';
      const current = gastosPorCategoria.get(cat) || 0;
      gastosPorCategoria.set(cat, current + t.monto);
    });

    // Ordenar y tomar top 5
    const sorted = [...gastosPorCategoria.entries()].sort((a, b) => b[1] - a[1]).slice(0, 5);

    const labels = sorted.map((e) => e[0]);
    const data = sorted.map((e) => e[1]);
    const bgColors = [
      '#ef4444', // red
      '#f97316', // orange
      '#eab308', // yellow
      '#84cc16', // lime
      '#06b6d4', // cyan
      '#6366f1', // indigo
    ];

    this.doughnutData = {
      labels: labels,
      datasets: [
        {
          data: data,
          backgroundColor: bgColors,
          hoverBackgroundColor: bgColors,
        },
      ],
    };

    this.doughnutOptions = {
      plugins: {
        legend: {
          position: 'right',
          labels: { color: '#94a3b8', usePointStyle: true },
        },
      },
      cutout: '60%',
    };
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }
}
