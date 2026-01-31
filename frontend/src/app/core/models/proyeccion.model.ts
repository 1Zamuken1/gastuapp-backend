export type TipoTransaccion = 'INGRESO' | 'EGRESO';
export type Frecuencia =
  | 'SEMANAL'
  | 'QUINCENAL'
  | 'MENSUAL'
  | 'BIMESTRAL'
  | 'SEMESTRAL'
  | 'ANUAL'
  | 'UNICA';

export interface Proyeccion {
  id?: number;
  monto: number;
  tipo: TipoTransaccion;
  categoriaId: number;
  usuarioId?: number;
  frecuencia: Frecuencia;
  fechaInicio: string; // ISO Date string
  ultimaEjecucion?: string;
  proximoCobro?: string;
  activo?: boolean;

  // Enriquecimiento UI
  nombreCategoria?: string;
  iconoCategoria?: string;
}
