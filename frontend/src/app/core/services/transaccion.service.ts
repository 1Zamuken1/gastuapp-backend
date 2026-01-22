/**
 * Service: TransaccionService
 *
 * FLUJO DE DATOS:
 * - RECIBE: Peticiones desde componentes Ingresos/Egresos/Dashboard
 * - LLAMA A: Backend /api/transacciones/* endpoints
 * - RETORNA: Datos de transacciones tipados
 *
 * RESPONSABILIDAD:
 * Gestiona todas las operaciones CRUD de transacciones.
 * Provee métodos para filtrar por tipo, categoría, fechas.
 * Calcula balance y resumen financiero.
 *
 * ENDPOINTS CONSUMIDOS:
 * - GET /transacciones - Listar todas
 * - GET /transacciones/tipo/{tipo} - Filtrar por tipo
 * - GET /transacciones/balance - Obtener balance
 * - GET /transacciones/resumen - Resumen financiero
 * - POST /transacciones - Crear
 * - PUT /transacciones/{id} - Actualizar
 * - DELETE /transacciones/{id} - Eliminar
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Transaccion,
  TransaccionRequest,
  TipoTransaccion,
  ResumenFinanciero,
} from '../models/transaccion.model';

@Injectable({
  providedIn: 'root',
})
export class TransaccionService {
  // URL base del API
  private readonly apiUrl = `${environment.apiUrl}/transacciones`;

  constructor(private http: HttpClient) {}

  // ==================== LISTAR ====================

  /**
   * Lista todas las transacciones del usuario.
   * El backend filtra automáticamente por userId del token.
   *
   * @returns Observable con array de transacciones
   */
  listarTodas(): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(this.apiUrl);
  }

  /**
   * Lista transacciones por tipo (INGRESO o EGRESO).
   *
   * EJEMPLO DE USO:
   * - En IngresosComponent: listarPorTipo('INGRESO')
   * - En EgresosComponent: listarPorTipo('EGRESO')
   *
   * @param tipo 'INGRESO' | 'EGRESO'
   * @returns Observable con transacciones del tipo
   */
  listarPorTipo(tipo: TipoTransaccion): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/tipo/${tipo}`);
  }

  /**
   * Lista transacciones por categoría.
   *
   * @param categoriaId ID de la categoría
   * @returns Observable con transacciones de la categoría
   */
  listarPorCategoria(categoriaId: number): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/categoria/${categoriaId}`);
  }

  /**
   * Lista transacciones en un rango de fechas.
   *
   * @param fechaInicio Fecha inicio (YYYY-MM-DD)
   * @param fechaFin Fecha fin (YYYY-MM-DD)
   * @returns Observable con transacciones en el rango
   */
  listarPorRango(fechaInicio: string, fechaFin: string): Observable<Transaccion[]> {
    const params = new HttpParams().set('fechaInicio', fechaInicio).set('fechaFin', fechaFin);
    return this.http.get<Transaccion[]>(`${this.apiUrl}/rango`, { params });
  }

  // ==================== OBTENER UNO ====================

  /**
   * Obtiene una transacción por su ID.
   *
   * @param id ID de la transacción
   * @returns Observable con la transacción
   */
  obtenerPorId(id: number): Observable<Transaccion> {
    return this.http.get<Transaccion>(`${this.apiUrl}/${id}`);
  }

  // ==================== CREAR / ACTUALIZAR / ELIMINAR ====================

  /**
   * Crea una nueva transacción.
   *
   * @param transaccion Datos de la nueva transacción
   * @returns Observable con la transacción creada
   */
  crear(transaccion: TransaccionRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(this.apiUrl, transaccion);
  }

  /**
   * Actualiza una transacción existente.
   *
   * @param id ID de la transacción
   * @param transaccion Nuevos datos
   * @returns Observable con la transacción actualizada
   */
  actualizar(id: number, transaccion: TransaccionRequest): Observable<Transaccion> {
    return this.http.put<Transaccion>(`${this.apiUrl}/${id}`, transaccion);
  }

  /**
   * Elimina una transacción.
   *
   * @param id ID de la transacción
   * @returns Observable void
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ==================== RESUMEN Y BALANCE ====================

  /**
   * Calcula el balance actual del usuario.
   * Balance = Total Ingresos - Total Egresos
   *
   * @returns Observable con el balance numérico
   */
  calcularBalance(): Observable<{ balance: number }> {
    return this.http.get<{ balance: number }>(`${this.apiUrl}/balance`);
  }

  /**
   * Obtiene el resumen financiero completo.
   * Incluye totales de ingresos, egresos, balance y cantidad.
   *
   * @returns Observable con resumen financiero
   */
  obtenerResumen(): Observable<ResumenFinanciero> {
    return this.http.get<ResumenFinanciero>(`${this.apiUrl}/resumen`);
  }
}
