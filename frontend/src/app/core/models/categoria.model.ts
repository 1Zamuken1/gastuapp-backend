/**
 * Model: Categoria
 *
 * Representa una categoría de gasto o ingreso.
 */

export interface Categoria {
  id: number;
  nombre: string;
  icono: string; // Emoji o clase de icono
  tipo: TipoCategoria;
  predefinida: boolean;
}

export type TipoCategoria = 'INGRESO' | 'EGRESO';
