/**
 * Service: AuthService (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - RECIBE: Credenciales desde componentes Login/Register
 * - LLAMA A: Supabase Auth SDK para autenticación
 * - ALMACENA: Sesión gestionada por Supabase (localStorage)
 * - PROVEE: Estado de autenticación a toda la aplicación via signals
 *
 * RESPONSABILIDAD:
 * Gestiona toda la lógica de autenticación usando Supabase Auth.
 * Maneja la sesión de Supabase (tokens, refresh, persistencia).
 * Expone signals reactivos para estado de auth.
 *
 * MÉTODOS PRINCIPALES:
 * - login(): Autenticar usuario via Supabase
 * - register(): Crear nueva cuenta en Supabase Auth
 * - logout(): Cerrar sesión en Supabase
 * - isAuthenticated(): Verificar si hay sesión activa
 * - getToken(): Obtener access_token actual de Supabase
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */
import { Injectable, signal, computed, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { supabase } from '../config/supabase.client';
import { LoginRequest, RegistroRequest, UsuarioToken, RolUsuario } from '../models/auth.model';
import { Session, AuthChangeEvent, Subscription } from '@supabase/supabase-js';

@Injectable({
  providedIn: 'root',
})
export class AuthService implements OnDestroy {
  // Signal reactivo para la sesión de Supabase
  private sessionSignal = signal<Session | null>(null);

  // Subscription al listener de auth state
  private authSubscription: Subscription | null = null;

  // Computed: verificar si está autenticado
  public isAuthenticated = computed(() => !!this.sessionSignal());

  // Computed: obtener datos del usuario de la sesión
  public currentUser = computed<UsuarioToken | null>(() => {
    const session = this.sessionSignal();
    if (!session) return null;
    return this.extractUserFromSession(session);
  });

  constructor(private router: Router) {
    this.initializeAuth();
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
  }

  // ==================== INICIALIZACIÓN ====================

  /**
   * Inicializa la autenticación:
   * 1. Recupera la sesión existente (si hay una)
   * 2. Escucha cambios de estado de auth (login, logout, refresh)
   */
  private async initializeAuth(): Promise<void> {
    // 1. Recuperar sesión existente
    const {
      data: { session },
    } = await supabase.auth.getSession();
    this.sessionSignal.set(session);

    // 2. Escuchar cambios de auth state
    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((event: AuthChangeEvent, session: Session | null) => {
      this.sessionSignal.set(session);

      // Si la sesión expiró o se cerró, redirigir a login
      if (event === 'SIGNED_OUT') {
        this.router.navigate(['/login']);
      }
    });
    this.authSubscription = subscription;
  }

  // ==================== MÉTODOS PÚBLICOS ====================

  /**
   * Inicia sesión con Supabase Auth.
   *
   * FLUJO:
   * 1. Envía credenciales a Supabase Auth
   * 2. Supabase valida y retorna sesión con JWT
   * 3. El onAuthStateChange actualiza el sessionSignal
   *
   * @param credentials Email y password
   * @returns Promise con resultado de auth
   * @throws Error si las credenciales son inválidas
   */
  async login(credentials: LoginRequest): Promise<{ success: boolean; error?: string }> {
    const { data, error } = await supabase.auth.signInWithPassword({
      email: credentials.email,
      password: credentials.password,
    });

    if (error) {
      console.error('Error de login Supabase:', error.message);
      return {
        success: false,
        error: this.translateSupabaseError(error.message),
      };
    }

    return { success: true };
  }

  /**
   * Registra un nuevo usuario en Supabase Auth.
   *
   * FLUJO:
   * 1. Crea usuario en Supabase Auth con email/password
   * 2. Envía datos adicionales como user_metadata
   * 3. El trigger en la BD sincroniza a public.usuarios
   * 4. El onAuthStateChange actualiza el sessionSignal
   *
   * @param userData Datos del nuevo usuario
   * @returns Promise con resultado de registro
   */
  async register(userData: RegistroRequest): Promise<{ success: boolean; error?: string }> {
    const { data, error } = await supabase.auth.signUp({
      email: userData.email,
      password: userData.password,
      options: {
        data: {
          // Estos datos se guardan en auth.users.raw_user_meta_data
          // El trigger handle_new_user() los usa para crear el registro en public.usuarios
          nombre: userData.nombre,
          apellido: userData.apellido,
          telefono: userData.telefono || null,
          tipologia: userData.tipologia || 'OTRO',
          profesion: userData.profesion || null,
          institucion: userData.institucion || null,
        },
      },
    });

    if (error) {
      console.error('Error de registro Supabase:', error.message);
      return {
        success: false,
        error: this.translateSupabaseError(error.message),
      };
    }

    // Verificar si requiere confirmación de email
    if (data.user && !data.session) {
      return {
        success: true,
        error: 'Se ha enviado un email de confirmación. Revisa tu bandeja de entrada.',
      };
    }

    return { success: true };
  }

  /**
   * Cierra la sesión actual en Supabase Auth.
   * Supabase limpia automáticamente el localStorage.
   * El onAuthStateChange detecta SIGNED_OUT y redirige a login.
   */
  async logout(): Promise<void> {
    await supabase.auth.signOut();
    // sessionSignal se actualiza via onAuthStateChange
  }

  /**
   * Obtiene el access_token actual de Supabase.
   * Usado por el interceptor HTTP.
   *
   * @returns Token JWT de Supabase o null
   */
  getToken(): string | null {
    return this.sessionSignal()?.access_token ?? null;
  }

  // ==================== MÉTODOS PRIVADOS ====================

  /**
   * Extrae información del usuario de la sesión de Supabase.
   * Combina datos del token con user_metadata.
   */
  private extractUserFromSession(session: Session): UsuarioToken {
    const user = session.user;
    const metadata = user.user_metadata || {};

    return {
      email: user.email || '',
      publicId: user.id, // UUID de Supabase = supabase_uid
      rol: (metadata['rol'] as RolUsuario) || 'USER',
      userId: user.id,
    };
  }

  /**
   * Traduce errores de Supabase a mensajes en español.
   */
  private translateSupabaseError(error: string): string {
    const errorMap: Record<string, string> = {
      'Invalid login credentials': 'Credenciales incorrectas',
      'Email not confirmed': 'Email no confirmado. Revisa tu bandeja de entrada.',
      'User already registered': 'Este email ya está registrado',
      'Password should be at least 6 characters': 'La contraseña debe tener al menos 6 caracteres',
      'Signup requires a valid password': 'Se requiere una contraseña válida',
      'Unable to validate email address: invalid format': 'Formato de email inválido',
      'Email rate limit exceeded': 'Demasiados intentos. Espera unos minutos.',
    };

    return errorMap[error] || `Error de autenticación: ${error}`;
  }
}
