// Services de planificaciones

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { Planificacion } from '../../core/models/planificacion.model';

/**
 * Service: PlanificacionService
 *
 * FLUJO DE DATOS:
 * - RECIBE: Llamadas desde Components
 * - LLAMA A: Backend endpoints (/api/presupuestos-planificaciones)
 * - RETORNA: Observables con datos de planificaciones
 *
 * RESPONSABILIDAD:
 * Orquesta operaciones CRUD para planificaciones.
 * Maneja errores de comunicación con el backend.
 * Provee métodos especializados para la integración con el frontend.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Injectable({
  providedIn: 'root'
})
export class PlanificacionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/presupuestos-planificaciones`;

  constructor() {}

  /**
   * Lista todas las planificaciones del usuario autenticado.
   */
  listarPlanificaciones(): Observable<Planificacion[]> {
    return this.http.get<Planificacion[]>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Error listando planificaciones:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Obtiene una planificación por su ID público.
   */
  obtenerPorId(publicId: string): Observable<Planificacion> {
    return this.http.get<Planificacion>(`${this.apiUrl}/${publicId}`).pipe(
      catchError(error => {
        console.error('Error obteniendo planificación:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Crea una nueva planificación.
   */
  crearPlanificacion(presupuesto: any): Observable<Planificacion> {
    return this.http.post<Planificacion>(this.apiUrl, presupuesto).pipe(
      catchError(error => {
        console.error('Error creando planificación:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Actualiza una planificación existente.
   */
  actualizarPlanificacion(publicId: string, presupuesto: any): Observable<Planificacion> {
    return this.http.put<Planificacion>(`${this.apiUrl}/${publicId}`, presupuesto).pipe(
      catchError(error => {
        console.error('Error actualizando planificación:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Desactiva una planificación.
   */
  desactivarPlanificacion(publicId: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${publicId}/desactivar`, {}).pipe(
      catchError(error => {
        console.error('Error desactivando planificación:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Lista solo las planificaciones activas del usuario.
   */
  listarActivas(): Observable<Planificacion[]> {
    return this.http.get<Planificacion[]>(`${this.apiUrl}/activas`).pipe(
      catchError(error => {
        console.error('Error listando planificaciones activas:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Lista planificaciones cercanas a exceder.
   * Por defecto, 80% de utilización.
   */
  listarCercanosExceder(porcentaje: number = 80): Observable<Planificacion[]> {
    return this.http.get<Planificacion[]>(`${this.apiUrl}/cercanos?porcentaje=${porcentaje}`).pipe(
      catchError(error => {
        console.error('Error listando planificaciones cercanas a exceder:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Sincroniza montos gastados de todas las planificaciones del usuario.
   */
  sincronizarMontosGastados(): Observable<any> {
    return this.http.post(`${this.apiUrl}/actualizar-montos`, {}).pipe(
      catchError(error => {
        console.error('Error sincronizando montos gastados:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Obtiene métricas de planificaciones para el dashboard.
   */
  obtenerMetricas(): Observable<any> {
    return this.http.get(`${this.apiUrl}/metricas`).pipe(
      map((response: any) => ({
        total: response.total || 0,
        activas: response.activas || 0,
        excedidas: response.excedidas || 0,
        inactivas: response.inactivas || 0
      })),
      catchError(error => {
        console.error('Error obteniendo métricas:', error);
        return throwError(() => error);
      })
    );
  }
}