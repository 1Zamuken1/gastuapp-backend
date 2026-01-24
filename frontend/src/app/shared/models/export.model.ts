/**
 * Modelos para el Sistema de Exportación Genérico
 *
 * Define las interfaces para configurar el modal de exportación
 * de manera que pueda ser reutilizado por cualquier módulo.
 */

export interface ExportModalConfig {
  title: string; // Título del modal (ej: "Exportar Ingresos")
  moduleType: string; // Identificador del módulo (ej: 'transacciones')
  fields: ExportField[]; // Campos dinámicos del formulario
  columns: ExportColumn[]; // Definición de columnas para el archivo final
  data: any[]; // Datos crudos a filtrar y exportar
}

export interface ExportField {
  type: 'checkbox-group' | 'radio' | 'daterange' | 'select';
  key: string; // Clave única para identificar el campo (ej: 'categorias')
  label: string; // Etiqueta visual (ej: "Categorías")

  // Para checkboxes y radios
  options?: ExportOption[];
}

export interface ExportOption {
  label: string;
  value: any;
  checked?: boolean; // Estado inicial
}

export interface ExportColumn {
  header: string; // Título de la columna en el archivo (ej: "Fecha Transacción")
  field: string; // Propiedad del objeto data (ej: "fecha" o "categoria.nombre")
  type: 'text' | 'currency' | 'date' | 'percent';
}

// Estructura de los filtros aplicados que devuelve el modal
export interface ExportFilterResult {
  formato: 'csv' | 'excel' | 'pdf';
  detalle: 'resumen' | 'detallado';
  filtros: { [key: string]: any }; // { categorias: [1, 2], fechas: {start, end} }
}
