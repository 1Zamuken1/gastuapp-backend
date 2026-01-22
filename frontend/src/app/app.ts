/**
 * Componente Principal: App
 *
 * FLUJO DE DATOS:
 * - BOOTSTRAP: Componente raíz de la aplicación Angular
 * - RENDERIZA: Template app.html con router-outlet
 * - PROVEE: Contenedor principal para toda la aplicación
 *
 * RESPONSABILIDAD:
 * Componente raíz que sirve como contenedor principal.
 * Actualmente muestra una vista de prueba del sistema de diseño.
 * Será reemplazado por el layout principal cuando se implemente routing.
 *
 * IMPORTS:
 * - RouterOutlet: Para navegación entre componentes
 * - ButtonModule: Botones de PrimeNG
 * - CardModule: Cards de PrimeNG
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// Importaciones de PrimeNG
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonModule, CardModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('GastuApp');
}
