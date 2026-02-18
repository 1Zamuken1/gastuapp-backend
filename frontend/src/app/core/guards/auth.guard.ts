/**
 * Guard: AuthGuard (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - VERIFICA: Estado de autenticación antes de navegar
 * - REDIRIGE: A /login si no está autenticado
 * - PERMITE: Acceso si hay sesión activa de Supabase
 *
 * RESPONSABILIDAD:
 * Guard funcional (Angular 21 style) que protege rutas
 * que requieren autenticación.
 *
 * USO:
 * En app.routes.ts:
 * { path: 'dashboard', canActivate: [authGuard], component: DashboardComponent }
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Guard funcional para proteger rutas.
 *
 * FUNCIONAMIENTO:
 * 1. Verifica si hay sesión activa de Supabase
 * 2. Si existe, permite el acceso (retorna true)
 * 3. Si no existe, redirige a /login (retorna false)
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirigir a login
  router.navigate(['/login']);
  return false;
};
