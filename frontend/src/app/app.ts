/**
 * Componente Principal: App
 *
 * FLUJO DE DATOS:
 * - BOOTSTRAP: Componente raíz de la aplicación Angular
 * - RENDERIZA: Router outlet para navegación
 * - PROVEE: Punto de entrada para toda la aplicación
 *
 * RESPONSABILIDAD:
 * Componente raíz minimalista que solo renderiza el router-outlet.
 * La navegación y layout se manejan en app.routes.ts y LayoutComponent.
 *
 * ESTRUCTURA DE NAVEGACIÓN:
 * App (router-outlet)
 *   ├── LoginComponent (ruta /login)
 *   └── LayoutComponent (rutas protegidas)
 *       ├── DashboardComponent (/dashboard)
 *       ├── IngresosComponent (/ingresos)
 *       └── EgresosComponent (/egresos)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.1
 * @since 2026-01-21
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: [],
})
export class App {}
