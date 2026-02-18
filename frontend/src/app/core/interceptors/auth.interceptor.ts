/**
 * HTTP Interceptor: AuthInterceptor (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - INTERCEPTA: Todas las peticiones HTTP salientes al backend
 * - MODIFICA: Agrega header Authorization con access_token de Supabase
 * - EXCLUYE: Endpoints públicos (/auth/health)
 *
 * RESPONSABILIDAD:
 * Interceptor funcional (Angular 21 style) que agrega automáticamente
 * el token JWT de Supabase a todas las peticiones que requieren autenticación.
 *
 * NOTA: Las llamadas a Supabase Auth (signIn, signUp) se hacen
 * directamente via el SDK de Supabase, NO via HttpClient, por lo
 * que este interceptor NO las afecta.
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * URLs que NO requieren token de autenticación
 */
const PUBLIC_URLS = ['/auth/health', '/health'];

/**
 * Interceptor funcional para agregar JWT de Supabase a peticiones.
 *
 * USO:
 * Se registra en app.config.ts con provideHttpClient(withInterceptors([authInterceptor]))
 *
 * EJEMPLO:
 * Petición original: GET /api/transacciones
 * Petición modificada: GET /api/transacciones + Header: Authorization: Bearer eyJhbc...
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  // Inyectar AuthService
  const authService = inject(AuthService);

  // Verificar si la URL es pública
  const isPublicUrl = PUBLIC_URLS.some((url) => req.url.includes(url));

  if (isPublicUrl) {
    // Dejar pasar sin modificar
    return next(req);
  }

  // Obtener token de Supabase (access_token)
  const token = authService.getToken();

  if (!token) {
    // Sin token, dejar pasar (el backend retornará 401)
    return next(req);
  }

  // Clonar petición con header Authorization
  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq);
};
