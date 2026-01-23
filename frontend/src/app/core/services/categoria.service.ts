/**
 * Service: CategoriaService
 *
 * Mange endpoints related to Categories.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-22
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, TipoCategoria } from '../models/categoria.model';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/categorias`;

  /**
   * Obtiene todas las categorías predefinidas
   */
  listarPredefinidas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  /**
   * Obtiene categorías por tipo (INGRESO/EGRESO)
   */
  listarPorTipo(tipo: TipoCategoria): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/tipo/${tipo}`);
  }

  /**
   * Obtiene una categoría por ID
   */
  obtenerPorId(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.apiUrl}/${id}`);
  }
}
