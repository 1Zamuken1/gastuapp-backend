/**
 * Configuración de la Aplicación: AppConfig
 *
 * FLUJO DE DATOS:
 * - PROVEE: Configuración global de Angular
 * - CONFIGURA: Routing, animaciones, PrimeNG, HttpClient
 * - USADO POR: main.ts en bootstrapApplication()
 *
 * RESPONSABILIDAD:
 * Define los providers globales de la aplicación.
 * Configura PrimeNG con tema Aura y efectos visuales.
 * Configura HttpClient con interceptor JWT.
 *
 * PROVIDERS INCLUIDOS:
 * - provideZoneChangeDetection: Optimización de detección de cambios
 * - provideRouter: Sistema de routing Angular
 * - provideAnimationsAsync: Animaciones asíncronas
 * - provideHttpClient: Cliente HTTP con interceptor
 * - providePrimeNG: Tema y configuración de PrimeNG
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.1
 * @since 2026-01-21
 */
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Optimización de detección de cambios
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Sistema de routing
    provideRouter(routes),

    // Animaciones asíncronas (mejor rendimiento)
    provideAnimationsAsync(),

    // Cliente HTTP con interceptor JWT
    provideHttpClient(withInterceptors([authInterceptor])),

    // Configuración de PrimeNG
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.dark-mode',
        },
      },
      ripple: true,
    }),
  ],
};
