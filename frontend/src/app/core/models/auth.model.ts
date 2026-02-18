/**
 * Modelo: Usuario y Auth (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Supabase Auth SDK y Backend API
 * - USADO POR: AuthService, componentes de usuario
 *
 * RESPONSABILIDAD:
 * Define las estructuras de autenticación y usuario en el frontend.
 * Adaptado para trabajar con tokens de Supabase Auth.
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */

/**
 * Roles disponibles en el sistema
 */
export type RolUsuario = 'ADMIN' | 'USER' | 'USER_HIJO';

/**
 * Respuesta del backend GET /auth/me
 * Corresponde a AuthResponseDTO del backend
 */
export interface AuthResponse {
  token: string | null;
  type: string; // 'Bearer'
  publicId: string;
  email: string;
  rol: RolUsuario;
}

/**
 * DTO para login (Supabase Auth)
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * DTO para registro (Supabase Auth)
 * Los campos adicionales se envían como metadata de usuario
 */
export interface RegistroRequest {
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
  password: string;
  tipologia?: string;
  profesion?: string;
  institucion?: string;
}

/**
 * Información del usuario autenticado
 * Extraída de la sesión de Supabase + datos del backend
 */
export interface UsuarioToken {
  email: string;
  publicId: string;
  rol: RolUsuario;
  userId: string; // Cambiado de number a string (Supabase UUID)
}
