/**
 * Model: Planificacion
 *
 * FLUJO DE DATOS:
 * - RECIBE: Datos desde backend REST API
 * - USADO POR: PlanificacionService, PlanificacionCard, PlanificacionList
 * - ENVIADO A: Componentes UI para renderizado
 *
 * RESPONSABILIDAD:
 * Definir la estructura y estado de las planificaciones de presupuesto
 * con lógica de negocio para cálculos y estados automáticos
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */

export enum EstadoPlanificacion {
  ACTIVA = 'ACTIVA',
  EXCEDIDA = 'EXCEDIDA',
  INACTIVA = 'INACTIVA',
}

export enum FrecuenciaPlanificacion {
  SEMANAL = 'SEMANAL',
  QUINCENAL = 'QUINCENAL',
  MENSUAL = 'MENSUAL',
  BIMESTRAL = 'BIMESTRAL',
  TRIMESTRAL = 'TRIMESTRAL',
  SEMESTRAL = 'SEMESTRAL',
  ANUAL = 'ANUAL',
}

export interface Planificacion {
  id: number;
  publicId: string;
  categoriaId: number;
  categoriaNombre: string;
  montoTope: number;
  montoGastado: number;
  fechaInicio: string;
  fechaFin: string;
  frecuencia: FrecuenciaPlanificacion;
  estado: EstadoPlanificacion;
  autoRenovar: boolean;
  color: string;
  nombrePersonalizado?: string;
  
  // Propiedades calculadas (incluidas desde backend)
  porcentajeUtilizacion: number;
  montoRestante: number;
  diasRestantes: number;
  periodoTexto: string;
  
  // Propiedades visuales (calculadas en frontend)
  estadoColor?: string;
  frecuenciaIcono?: string;
  frecuenciaTexto?: string;
  estadoTexto?: string;
  diasRestantesColor?: string;
  porcentajeColor?: string;
}

export interface PlanificacionRequest {
  categoriaId: number;
  montoTope: number;
  fechaInicio: string;
  fechaFin: string;
  frecuencia: FrecuenciaPlanificacion;
  autoRenovar: boolean;
  color: string;
  nombrePersonalizado?: string;
}

export interface PlanificacionResponse {
  data: Planificacion[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface PlanificacionMetrics {
  totalPresupuestos: number;
  totalMontoAsignado: number;
  totalMontoGastado: number;
  totalMontoRestante: number;
  presupuestosActivos: number;
  presupuestosExcedidos: number;
  presupuestosInactivos: number;
  porcentajeUtilizacionPromedio: number;
}

// Métodos de utilidad para Planificacion
export class PlanificacionUtils {
  static getFrecuenciaIcono(frecuencia: FrecuenciaPlanificacion): string {
    switch (frecuencia) {
      case FrecuenciaPlanificacion.SEMANAL:
        return 'pi pi-calendar';
      case FrecuenciaPlanificacion.QUINCENAL:
        return 'pi pi-calendar-plus';
      case FrecuenciaPlanificacion.MENSUAL:
        return 'pi pi-calendar-times';
      case FrecuenciaPlanificacion.BIMESTRAL:
        return 'pi pi-calendar-minus';
      case FrecuenciaPlanificacion.TRIMESTRAL:
        return 'pi pi-calendar';
      case FrecuenciaPlanificacion.SEMESTRAL:
        return 'pi pi-calendar-plus';
      case FrecuenciaPlanificacion.ANUAL:
        return 'pi pi-calendar-times';
      default:
        return 'pi pi-calendar';
    }
  }

  static getFrecuenciaTexto(frecuencia: FrecuenciaPlanificacion): string {
    switch (frecuencia) {
      case FrecuenciaPlanificacion.SEMANAL:
        return 'Semanal';
      case FrecuenciaPlanificacion.QUINCENAL:
        return 'Quincenal';
      case FrecuenciaPlanificacion.MENSUAL:
        return 'Mensual';
      case FrecuenciaPlanificacion.BIMESTRAL:
        return 'Bimestral';
      case FrecuenciaPlanificacion.TRIMESTRAL:
        return 'Trimestral';
      case FrecuenciaPlanificacion.SEMESTRAL:
        return 'Semestral';
      case FrecuenciaPlanificacion.ANUAL:
        return 'Anual';
      default:
        return frecuencia;
    }
  }

  static getEstadoColor(estado: EstadoPlanificacion): string {
    switch (estado) {
      case EstadoPlanificacion.ACTIVA:
        return '#22c55e'; // green-500
      case EstadoPlanificacion.EXCEDIDA:
        return '#ef4444'; // red-500
      case EstadoPlanificacion.INACTIVA:
        return '#64748b'; // slate-500
      default:
        return '#6366f1'; // indigo-500
    }
  }

  static getEstadoIcono(estado: EstadoPlanificacion): string {
    switch (estado) {
      case EstadoPlanificacion.ACTIVA:
        return 'pi pi-check-circle';
      case EstadoPlanificacion.EXCEDIDA:
        return 'pi pi-exclamation-triangle';
      case EstadoPlanificacion.INACTIVA:
        return 'pi pi-times-circle';
      default:
        return 'pi pi-info-circle';
    }
  }

  static getEstadoTexto(estado: EstadoPlanificacion): string {
    switch (estado) {
      case EstadoPlanificacion.ACTIVA:
        return 'ACTIVO';
      case EstadoPlanificacion.EXCEDIDA:
        return 'EXCEDIDO';
      case EstadoPlanificacion.INACTIVA:
        return 'INACTIVO';
      default:
        return estado;
    }
  }

  static getDiasRestantesColor(diasRestantes: number): string {
    if (diasRestantes < 0) {
      return '#ef4444'; // red-500
    } else if (diasRestantes <= 3) {
      return '#f59e0b'; // amber-500
    } else if (diasRestantes <= 7) {
      return '#eab308'; // yellow-500
    } else {
      return '#22c55e'; // green-500
    }
  }

  static getPorcentajeColor(porcentaje: number): string {
    if (porcentaje >= 100) {
      return '#ef4444'; // red-500
    } else if (porcentaje >= 80) {
      return '#f59e0b'; // amber-500
    } else if (porcentaje >= 60) {
      return '#eab308'; // yellow-500
    } else {
      return '#22c55e'; // green-500
    }
  }

  static calcularDiasRestantes(fechaFin: string): number {
    const fin = new Date(fechaFin);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    fin.setHours(0, 0, 0, 0);
    
    const diffTime = fin.getTime() - hoy.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  static formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(monto);
  }

  static calcularProgreso(montoGastado: number, montoTope: number): number {
    if (montoTope === 0) return 0;
    return Math.min((montoGastado / montoTope) * 100, 100);
  }
}