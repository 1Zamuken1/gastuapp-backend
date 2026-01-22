/**
 * Routes Configuration: app.routes.ts
 *
 * FLUJO DE DATOS:
 * - DEFINE: Todas las rutas de la aplicación
 * - PROTEGE: Rutas autenticadas con authGuard
 * - ORGANIZA: Layout principal con rutas hijas
 *
 * RESPONSABILIDAD:
 * Configuración central del sistema de routing.
 * Define qué componente se muestra para cada URL.
 * Protege rutas que requieren autenticación.
 *
 * ESTRUCTURA DE RUTAS:
 * /login         → LoginComponent (público)
 * /              → Redirige a /dashboard
 * /dashboard     → DashboardComponent (protegido)
 * /ingresos      → IngresosComponent (protegido)
 * /egresos       → EgresosComponent (protegido) [TODO: crear]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Routes } from '@angular/router';

// Guards
import { authGuard } from './core/guards/auth.guard';

// Components
import { LayoutComponent } from './layout/layout.component';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { IngresosComponent } from './features/transacciones/ingresos/ingresos.component';
import { EgresosComponent } from './features/transacciones/egresos/egresos.component';

export const routes: Routes = [
  // ==================== RUTAS PÚBLICAS ====================
  {
    path: 'login',
    component: LoginComponent,
    title: 'Iniciar Sesión - GastuApp',
  },

  // ==================== RUTAS PROTEGIDAS ====================
  // Todas las rutas dentro del Layout requieren autenticación
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // Redirigir raíz a dashboard
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      // Dashboard
      {
        path: 'dashboard',
        component: DashboardComponent,
        title: 'Dashboard - GastuApp',
      },
      // Ingresos
      {
        path: 'ingresos',
        component: IngresosComponent,
        title: 'Ingresos - GastuApp',
      },
      // Egresos
      {
        path: 'egresos',
        component: EgresosComponent,
        title: 'Egresos - GastuApp',
      },
    ],
  },

  // ==================== FALLBACK ====================
  // Redirigir rutas no encontradas a dashboard
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
