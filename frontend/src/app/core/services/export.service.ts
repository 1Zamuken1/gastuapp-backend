/**
 * Servicio de Exportación
 *
 * RESPONSABILIDAD:
 * Generar y descargar archivos en formatos CSV, Excel y PDF.
 * Transforma los datos crudos según las columnas definidas.
 *
 * DEPENDENCIAS:
 * - xlsx (Excel/CSV)
 * - jspdf (PDF)
 * - jspdf-autotable (Tablas en PDF)
 */
import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { ExportColumn } from '../../shared/models/export.model';

@Injectable({
  providedIn: 'root',
})
export class ExportService {
  constructor() {}

  /**
   * Genera y descarga un archivo Excel (.xlsx)
   */
  exportToExcel(data: any[], columns: ExportColumn[], filename: string): void {
    const exportData = this.prepareDataForExport(data, columns);

    // Crear hoja de trabajo
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);

    // Crear libro de trabajo
    const workbook: XLSX.WorkBook = {
      Sheets: { data: worksheet },
      SheetNames: ['data'],
    };

    // Descargar
    XLSX.writeFile(workbook, `${filename}.xlsx`);
  }

  /**
   * Genera y descarga un archivo CSV (.csv)
   */
  exportToCSV(data: any[], columns: ExportColumn[], filename: string): void {
    const exportData = this.prepareDataForExport(data, columns);

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
    const csvOutput = XLSX.utils.sheet_to_csv(worksheet);

    // Crear blob y descargar
    const blob = new Blob([csvOutput], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Genera y descarga un archivo PDF (.pdf) con tabla
   */
  exportToPDF(data: any[], columns: ExportColumn[], filename: string, title: string): void {
    const doc = new jsPDF();

    // Título
    doc.setFontSize(18);
    doc.text(title, 14, 22);
    doc.setFontSize(11);
    doc.setTextColor(100);
    doc.text(`Fecha de generación: ${new Date().toLocaleDateString()}`, 14, 30);

    // Preparar columnas y filas para autotable
    const head = [columns.map((c) => c.header)];
    const body = data.map((item) => columns.map((c) => this.formatValue(item[c.field], c.type)));

    // Generar tabla
    autoTable(doc, {
      head: head,
      body: body,
      startY: 35,
      theme: 'grid',
      styles: { fontSize: 9 },
      headStyles: { fillColor: [22, 163, 74] }, // Un verde genérico, podría ser dinámico
    });

    doc.save(`${filename}.pdf`);
  }

  // ==================== HELPERS ====================

  // Mapea los datos crudos a objetos planos con headers como keys
  private prepareDataForExport(data: any[], columns: ExportColumn[]): any[] {
    return data.map((item) => {
      const row: any = {};
      columns.forEach((col) => {
        row[col.header] = this.formatValue(item[col.field], col.type);
      });
      return row;
    });
  }

  // Formatea valores según su tipo
  private formatValue(value: any, type: string): any {
    if (value === null || value === undefined) return '';

    switch (type) {
      case 'currency':
        return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(value);
      case 'date':
        return new Date(value).toLocaleDateString('es-CO');
      case 'percent':
        return `${value}%`;
      default:
        return value;
    }
  }
}
