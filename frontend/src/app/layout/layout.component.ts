/**
 * Component: LayoutComponent
 *
 * FLUJO DE DATOS:
 * - CONTIENE: Sidebar + área de contenido principal
 * - RENDERIZA: Componentes hijos vía router-outlet
 * - PROVEE: Navegación consistente en toda la app
 *
 * RESPONSABILIDAD:
 * Layout principal de la aplicación autenticada.
 * Incluye sidebar con navegación y área para contenido dinámico.
 * Maneja el estado del menú (expandido/colapsado).
 *
 * ESTRUCTURA:
 * ┌──────────────────────────────────────┐
 * │  Navbar (nombre usuario, logout)     │
 * ├────────┬─────────────────────────────┤
 * │        │                             │
 * │ Sidebar│     <router-outlet>         │
 * │        │     (Dashboard/Ingresos/..) │
 * │        │                             │
 * └────────┴─────────────────────────────┘
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/services/auth.service';

// PrimeNG Modules
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, TooltipModule],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
})
export class LayoutComponent {
  // Signal para controlar si el sidebar está expandido
  sidebarExpanded = signal(true);

  // Items del menú de navegación
  menuItems = [
    { label: 'Dashboard', icon: 'pi pi-home', route: '/dashboard' },
    { label: 'Ingresos', icon: 'pi pi-arrow-circle-up', route: '/ingresos' },
    { label: 'Egresos', icon: 'pi pi-arrow-circle-down', route: '/egresos' },
  ];

  constructor(public authService: AuthService) {}

  /**
   * Alterna el estado del sidebar (expandido/colapsado)
   */
  toggleSidebar(): void {
    this.sidebarExpanded.update((v) => !v);
  }

  /**
   * Cierra la sesión del usuario
   */
  logout(): void {
    this.authService.logout();
  }
}
