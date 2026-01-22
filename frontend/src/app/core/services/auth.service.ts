/**
 * Service: AuthService
 *
 * FLUJO DE DATOS:
 * - RECIBE: Credenciales desde componentes Login/Register
 * - LLAMA A: Backend /api/auth/* endpoints
 * - ALMACENA: Token JWT en localStorage
 * - PROVEE: Estado de autenticación a toda la aplicación
 *
 * RESPONSABILIDAD:
 * Gestiona toda la lógica de autenticación.
 * Maneja tokens JWT (almacenar, recuperar, eliminar).
 * Expone signals reactivos para estado de auth.
 *
 * MÉTODOS PRINCIPALES:
 * - login(): Autenticar usuario
 * - register(): Crear nueva cuenta
 * - logout(): Cerrar sesión
 * - isAuthenticated(): Verificar si hay sesión activa
 * - getToken(): Obtener token actual
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegistroRequest, UsuarioToken } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // URL base del API
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  // Key para localStorage
  private readonly tokenKey = environment.tokenKey;

  // Signal reactivo para el token
  private tokenSignal = signal<string | null>(this.getStoredToken());

  // Computed: verificar si está autenticado
  public isAuthenticated = computed(() => !!this.tokenSignal());

  // Computed: obtener datos del usuario del token
  public currentUser = computed<UsuarioToken | null>(() => {
    const token = this.tokenSignal();
    if (!token) return null;
    return this.decodeToken(token);
  });

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  // ==================== MÉTODOS PÚBLICOS ====================

  /**
   * Inicia sesión con email y password.
   *
   * FLUJO:
   * 1. Envía credenciales al backend
   * 2. Recibe token JWT
   * 3. Almacena token en localStorage
   * 4. Actualiza signal de autenticación
   *
   * @param credentials Email y password
   * @returns Observable con respuesta de auth
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => this.handleAuthSuccess(response)),
      catchError((error) => this.handleAuthError(error)),
    );
  }

  /**
   * Registra un nuevo usuario.
   * El backend hace auto-login y retorna token.
   *
   * @param userData Datos del nuevo usuario
   * @returns Observable con respuesta de auth
   */
  register(userData: RegistroRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData).pipe(
      tap((response) => this.handleAuthSuccess(response)),
      catchError((error) => this.handleAuthError(error)),
    );
  }

  /**
   * Cierra la sesión actual.
   * Elimina token y redirige a login.
   */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.tokenSignal.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Obtiene el token actual.
   * Usado por el interceptor HTTP.
   *
   * @returns Token JWT o null
   */
  getToken(): string | null {
    return this.tokenSignal();
  }

  // ==================== MÉTODOS PRIVADOS ====================

  /**
   * Recupera token almacenado en localStorage
   */
  private getStoredToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Maneja respuesta exitosa de autenticación
   */
  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    this.tokenSignal.set(response.token);
  }

  /**
   * Maneja errores de autenticación
   */
  private handleAuthError(error: any): Observable<never> {
    console.error('Error de autenticación:', error);
    return throwError(() => error);
  }

  /**
   * Decodifica el payload del token JWT
   * NOTA: No valida la firma, solo extrae datos
   */
  private decodeToken(token: string): UsuarioToken | null {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return {
        email: decoded.sub,
        publicId: decoded.publicId,
        rol: decoded.rol,
        userId: decoded.userId,
      };
    } catch {
      return null;
    }
  }
}
