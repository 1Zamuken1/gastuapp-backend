import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  MetaAhorro,
  MetaAhorroRequest,
  Ahorro,
  AhorroRequest,
  CuotaAhorro,
} from '../models/ahorro.model';

@Injectable({
  providedIn: 'root',
})
export class AhorroService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/ahorros`;

  // ==================== METAS ====================

  listarMetas(): Observable<MetaAhorro[]> {
    return this.http.get<MetaAhorro[]>(`${this.apiUrl}/metas`);
  }

  crearMeta(meta: MetaAhorroRequest): Observable<MetaAhorro> {
    return this.http.post<MetaAhorro>(`${this.apiUrl}/metas`, meta);
  }

  listarCuotasPorMeta(metaId: number): Observable<CuotaAhorro[]> {
    return this.http.get<CuotaAhorro[]>(`${this.apiUrl}/metas/${metaId}/cuotas`);
  }

  eliminarMeta(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/metas/${id}`);
  }

  actualizarMeta(id: number, meta: MetaAhorroRequest): Observable<MetaAhorro> {
    return this.http.put<MetaAhorro>(`${this.apiUrl}/metas/${id}`, meta);
  }

  // ==================== ABONOS ====================

  registrarAbono(abono: AhorroRequest): Observable<Ahorro> {
    return this.http.post<Ahorro>(`${this.apiUrl}/abonos`, abono);
  }

  listarAbonosPorMeta(metaId: number): Observable<Ahorro[]> {
    return this.http.get<Ahorro[]>(`${this.apiUrl}/metas/${metaId}/abonos`);
  }

  eliminarAbono(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/abonos/${id}`);
  }

  actualizarAbono(id: number, abono: AhorroRequest): Observable<Ahorro> {
    return this.http.put<Ahorro>(`${this.apiUrl}/abonos/${id}`, abono);
  }
}
