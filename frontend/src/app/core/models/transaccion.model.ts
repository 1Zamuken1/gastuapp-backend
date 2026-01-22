/**
 * Modelo: Transaccion
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Backend API (TransaccionResponseDTO)
 * - USADO POR: IngresosComponent, EgresosComponent, DashboardComponent
 *
 * RESPONSABILIDAD:
 * Define la estructura de una transacción en el frontend.
 * Mapea 1:1 con TransaccionResponseDTO del backend.
 *
 * CAMPOS:
 * - id: Identificador único
 * - monto: Cantidad en pesos
 * - tipo: 'INGRESO' | 'EGRESO'
 * - descripcion: Detalle de la transacción
 * - fecha: Fecha de la transacción
 * - categoriaId: ID de la categoría
 * - categoriaNombre: Nombre de la categoría (enriquecido)
 * - categoriaIcono: Icono de PrimeIcons (enriquecido)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */

/**
 * Tipo de transacción
 */
export type TipoTransaccion = 'INGRESO' | 'EGRESO';

/**
 * Interface principal de Transacción
 * Corresponde a TransaccionResponseDTO del backend
 */
export interface Transaccion {
  id: number;
  monto: number;
  tipo: TipoTransaccion;
  descripcion: string;
  fecha: string; // ISO date string (YYYY-MM-DD)
  fechaCreacion?: string;
  categoriaId: number;
  categoriaNombre: string;
  categoriaIcono: string;
  esAutomatica?: boolean;
}

/**
 * DTO para crear/actualizar transacciones
 * Corresponde a TransaccionRequestDTO del backend
 */
export interface TransaccionRequest {
  monto: number;
  tipo: TipoTransaccion;
  descripcion: string;
  fecha: string;
  categoriaId: number;
}

/**
 * Resumen financiero del usuario
 */
export interface ResumenFinanciero {
  totalIngresos: number;
  totalEgresos: number;
  balance: number;
  cantidadTransacciones: number;
}
