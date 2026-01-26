export interface MetaAhorro {
  id: number;
  usuarioId: number;
  nombre: string;
  montoObjetivo: number;
  montoActual: number;
  porcentajeProgreso: number;
  fechaLimite: string; // ISO date string
  fechaInicio?: string;
  frecuencia?: FrecuenciaAhorro;
  color: string;
  icono: string;
  estado: 'ACTIVA' | 'COMPLETADA' | 'PAUSADA' | 'CANCELADA';
  fechaCreacion: string;
}

export type FrecuenciaAhorro =
  | 'DIARIO'
  | 'SEMANAL'
  | 'QUINCENAL'
  | 'MENSUAL'
  | 'TRIMESTRAL'
  | 'SEMESTRAL'
  | 'ANUAL';

export interface CuotaAhorro {
  id: number;
  numeroCuota: number;
  fechaProgramada: string;
  montoEsperado: number;
  estado: 'PENDIENTE' | 'PAGADA' | 'VENCIDA' | 'CANCELADA';
  ahorroId?: number;
}

export interface MetaAhorroRequest {
  nombre: string;
  montoObjetivo: number;
  fechaLimite: string;
  fechaInicio?: string;
  frecuencia?: string;
  color: string;
  icono: string;
}

export interface Ahorro {
  id: number;
  metaAhorroId: number;
  usuarioId: number;
  monto: number;
  descripcion: string;
  fecha: string;
}

export interface AhorroRequest {
  metaAhorroId: number;
  monto: number;
  descripcion: string;
  fecha?: string;
  cuotaId?: number;
}
