/**
 * Modelo: Usuario y Auth
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: Backend API (AuthResponseDTO, UsuarioResponseDTO)
 * - USADO POR: AuthService, componentes de usuario
 *
 * RESPONSABILIDAD:
 * Define las estructuras de autenticación y usuario en el frontend.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-21
 */

/**
 * Roles disponibles en el sistema
 */
export type RolUsuario = 'ADMIN' | 'USER' | 'USER_HIJO';

/**
 * Respuesta del login/register
 * Corresponde a AuthResponseDTO del backend
 */
export interface AuthResponse {
  token: string;
  type: string; // 'Bearer'
  publicId: string;
  email: string;
  rol: RolUsuario;
}

/**
 * DTO para login
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * DTO para registro
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
 * Información del usuario decodificada del JWT
 */
export interface UsuarioToken {
  email: string;
  publicId: string;
  rol: RolUsuario;
  userId: number;
}
