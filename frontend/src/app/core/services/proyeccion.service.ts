import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { Proyeccion } from '../models/proyeccion.model';

@Injectable({
  providedIn: 'root',
})
export class ProyeccionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/proyecciones`;

  // Signals para estado (opcional, si queremos caching simple)
  proyecciones = signal<Proyeccion[]>([]);
  loading = signal<boolean>(false);

  listarProyecciones(): Observable<Proyeccion[]> {
    this.loading.set(true);
    return this.http.get<Proyeccion[]>(this.apiUrl).pipe(
      tap((data) => {
        this.proyecciones.set(data);
        this.loading.set(false);
      }),
      catchError((err) => {
        this.loading.set(false);
        return throwError(() => err);
      }),
    );
  }

  crearProyeccion(proyeccion: Proyeccion): Observable<Proyeccion> {
    return this.http.post<Proyeccion>(this.apiUrl, proyeccion);
  }

  actualizarProyeccion(id: number, proyeccion: Proyeccion): Observable<Proyeccion> {
    return this.http.put<Proyeccion>(`${this.apiUrl}/${id}`, proyeccion);
  }

  eliminarProyeccion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  ejecutarProyeccion(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/ejecutar`, {});
  }

  ejecutar(id: number): Observable<any> {
    return this.ejecutarProyeccion(id);
  }

  getHistorial(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/historial`);
  }
}
