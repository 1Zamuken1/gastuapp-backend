/**
 * HTTP Interceptor: AuthInterceptor
 *
 * FLUJO DE DATOS:
 * - INTERCEPTA: Todas las peticiones HTTP salientes
 * - MODIFICA: Agrega header Authorization con token JWT
 * - EXCLUYE: Endpoints públicos (/auth/login, /auth/register)
 *
 * RESPONSABILIDAD:
 * Interceptor funcional (Angular 21 style) que agrega automáticamente
 * el token JWT a todas las peticiones que requieren autenticación.
 *
 * FUNCIONAMIENTO:
 * 1. Verifica si la URL es pública (no requiere auth)
 * 2. Si es pública, deja pasar sin modificar
 * 3. Si requiere auth, obtiene token de AuthService
 * 4. Clona la petición agregando header Authorization
 * 5. Envía la petición modificada
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * URLs que NO requieren token de autenticación
 */
const PUBLIC_URLS = ['/auth/login', '/auth/register', '/auth/health', '/health'];

/**
 * Interceptor funcional para agregar JWT a peticiones.
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

  // Obtener token
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
