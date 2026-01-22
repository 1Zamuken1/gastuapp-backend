/**
 * Configuración de la Aplicación: AppConfig
 *
 * FLUJO DE DATOS:
 * - PROVEE: Configuración global de Angular
 * - CONFIGURA: Routing, animaciones, PrimeNG
 * - USADO POR: main.ts en bootstrapApplication()
 *
 * RESPONSABILIDAD:
 * Define los providers globales de la aplicación.
 * Configura PrimeNG con tema Aura y efectos visuales.
 * Habilita detección de cambios optimizada.
 *
 * PROVIDERS INCLUIDOS:
 * - provideZoneChangeDetection: Optimización de detección de cambios
 * - provideRouter: Sistema de routing Angular
 * - provideAnimationsAsync: Animaciones asíncronas (mejor rendimiento)
 * - providePrimeNG: Tema y configuración de PrimeNG
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // Optimización de detección de cambios
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Sistema de routing
    provideRouter(routes),

    // Animaciones asíncronas (mejor rendimiento)
    provideAnimationsAsync(),

    // Configuración de PrimeNG
    providePrimeNG({
      theme: {
        preset: Aura, // Tema base: Aura (moderno, limpio)
      },
      ripple: true, // Efecto ripple en botones (Material Design feel)
    }),
  ],
};
