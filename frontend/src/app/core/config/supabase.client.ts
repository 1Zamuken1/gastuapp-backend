/**
 * Supabase Client Singleton
 *
 * RESPONSABILIDAD:
 * Crea y exporta una única instancia del cliente de Supabase
 * para usar en toda la aplicación Angular.
 *
 * CONFIGURACIÓN:
 * - URL y Anon Key se obtienen del archivo de environment
 * - persistSession: true → mantiene la sesión en localStorage
 * - autoRefreshToken: true → renueva tokens automáticamente
 *
 * USO:
 * import { supabase } from '../../config/supabase.client';
 * const { data, error } = await supabase.auth.signInWithPassword({...});
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-02-12
 */
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../../environments/environment';

/**
 * Instancia singleton del cliente Supabase.
 * Se inicializa una sola vez y se reutiliza en toda la app.
 */
export const supabase: SupabaseClient = createClient(
  environment.supabaseUrl,
  environment.supabaseAnonKey,
  {
    auth: {
      persistSession: true, // Mantener sesión en localStorage
      autoRefreshToken: true, // Renovar token antes de que expire
      detectSessionInUrl: true, // Detectar tokens en URL (OAuth callbacks)
      storageKey: 'gastuapp_supabase_auth', // Key para localStorage
      // Lock personalizado para evitar NavigatorLockAcquireTimeoutError en Angular (Zone.js)
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      lock: async (_name: string, _acquireTimeout: number, fn: () => Promise<any>) => {
        return await fn();
      },
    },
  },
);
