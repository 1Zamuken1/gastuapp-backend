/**
 * ThemeService - Dynamic PrimeNG Theme Switching
 *
 * RESPONSABILIDAD:
 * Permite cambiar el tema (primary color) de PrimeNG en runtime.
 * Cada módulo puede activar su propio tema al inicializarse.
 *
 * USO:
 * - Inyectar en componentes
 * - Llamar setIncomeTheme() / setExpenseTheme() en ngOnInit
 * - Llamar resetTheme() en ngOnDestroy
 *
 * @author Juan Esteban Barrios Portela
 * @since 2026-01-22
 */
import { Injectable, inject } from '@angular/core';
import { PrimeNG } from 'primeng/config';
import {
  IncomePreset,
  ExpensePreset,
  SavingsPreset,
  PlanificacionPreset,
  DefaultPreset,
} from '../config/theme-presets';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private primeng = inject(PrimeNG);

  /**
   * Activa el tema verde para el módulo de Ingresos
   */
  setIncomeTheme(): void {
    this.primeng.theme.set({
      preset: IncomePreset,
      options: {
        darkModeSelector: '.dark-mode',
      },
    });
  }

  /**
   * Activa el tema naranja para el módulo de Egresos
   */
  setExpenseTheme(): void {
    this.primeng.theme.set({
      preset: ExpensePreset,
      options: {
        darkModeSelector: '.dark-mode',
      },
    });
  }

  /**
   * Activa el tema ámbar para el módulo de Ahorros
   */
  setSavingsTheme(): void {
    this.primeng.theme.set({
      preset: SavingsPreset,
      options: {
        darkModeSelector: '.dark-mode',
      },
    });
  }

  /**
   * Activa el tema violeta para el módulo de Planificaciones
   */
  setPlanificacionTheme(): void {
    this.primeng.theme.set({
      preset: PlanificacionPreset,
      options: {
        darkModeSelector: '.dark-mode',
      },
    });
  }

  /**
   * Restaura el tema por defecto (Aura verde esmeralda)
   */
  resetTheme(): void {
    this.primeng.theme.set({
      preset: DefaultPreset,
      options: {
        darkModeSelector: '.dark-mode',
      },
    });
  }
}
