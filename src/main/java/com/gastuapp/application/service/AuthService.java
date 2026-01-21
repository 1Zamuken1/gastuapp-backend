package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.LoginRequestDTO;
import com.gastuapp.application.dto.request.RegistroRequestDTO;
import com.gastuapp.application.dto.response.AuthResponseDTO;
import com.gastuapp.application.dto.response.UsuarioResponseDTO;
import com.gastuapp.application.mapper.UsuarioMapper;
import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import com.gastuapp.infrastructure.security.jwt.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service: AuthService
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: AuthController (Infrastructure Layer)
 * - ENVÍA DATOS A: UsuarioRepositoryPort (Domain Port) → Infrastructure
 * - USA: JwtUtils (Infrastructure), UsuarioService (Application)
 * - RETORNA: AuthResponseDTO con token JWT
 *
 * RESPONSABILIDAD:
 * Orquesta los casos de uso de autenticación y autorización.
 * Maneja la lógica de login, register y generación de tokens JWT.
 * Valida credenciales y estados de usuarios.
 *
 * CASOS DE USO:
 * 1. Login: Validar credenciales → Generar JWT → Retornar token
 * 2. Register: Crear usuario → Generar JWT → Retornar token (auto-login)
 * 3. ValidateToken: Verificar si un JWT es válido
 * 4. GetEmailFromToken: Extraer email de un JWT
 *
 * VALIDACIONES:
 * - Email existe en el sistema
 * - Password coincide con BCrypt
 * - Usuario está activo (no deshabilitado)
 * - Token JWT válido y no expirado
 *
 * SEGURIDAD:
 * - Passwords nunca se retornan al cliente
 * - Mensajes de error genéricos ("Credenciales inválidas")
 * - Validación de usuarios activos
 * - Tokens firmados con secret key de 512 bits
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@Service
@Transactional
public class AuthService {
    private final UsuarioRepositoryPort usuarioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente todas las dependencias.
     *
     * @param usuarioRepository Port para acceso a datos de usuarios
     * @param usuarioService    Servicio para operaciones de usuarios
     * @param usuarioMapper     Mapper entre DTOs y Domain
     * @param jwtUtils          Utilidad para manejo de JWT
     */
    public AuthService(
            UsuarioRepositoryPort usuarioRepository,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            JwtUtils jwtUtils) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ============ Caso de Uso 1: Login ============
    /**
     * Autentica un usuario y genera token JWT.
     *
     * FLUJO:
     * 1. Buscar usuario por email
     * 2. Verificar que esté activo
     * 3. Validar password con BCrypt
     * 4. Generar token JWT
     *
     * @param loginRequest Email y password del usuario
     * @return AuthResponseDTO con token JWT
     * @throws IllegalArgumentException si credenciales inválidas o usuario inactivo
     */
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credenciales inválidas"));

        // 2. Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            throw new IllegalArgumentException(
                    "Usuario inactivo, por favor contacte al administrador");
        }

        // 3. Validar password con BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException(
                    "Credenciales inválidas");
        }

        // 4. Generar token JWT
        String token = jwtUtils.generateToken(
                usuario.getEmail(),
                usuario.getPublicId(),
                usuario.getRol().name());

        // 5. Construir y retornar respuesta
        return new AuthResponseDTO(
                token,
                "Bearer",
                usuario.getPublicId(),
                usuario.getEmail(),
                usuario.getRol().name());
    }

    // ============ Caso de Uso 2: Register ============
    /**
     * Registra un nuevo usuario y genera token JWT (auto-login).
     *
     * FLUJO:
     * 1. Crear usuario (delega a UsuarioService)
     * 2. Generar token JWT
     * 3. Retornar AuthResponseDTO (usuario ya logueado)
     *
     * @param registroRequest Datos del nuevo usuario
     * @return AuthResponseDTO con token JWT
     * @throws IllegalArgumentException si el email ya existe
     */
    public AuthResponseDTO register(RegistroRequestDTO registroRequest) {
        // 1. Registrar usuario (UsuarioService valida duplicados)
        UsuarioResponseDTO usuarioCreado = usuarioService.registrarUsuario(registroRequest);

        // 2. Generar token JWT (auto-login)
        String token = jwtUtils.generateToken(
            usuarioCreado.getEmail(),
            usuarioCreado.getPublicId(),
            usuarioCreado.getRol().name()
        );

        // 3. Construir y retornar respuesta
        return new AuthResponseDTO(
            token,
            "Bearer",
            usuarioCreado.getPublicId(),
            usuarioCreado.getEmail(),
            usuarioCreado.getRol().name()
        );
    }

    // ==================== UTILIDADES ====================

    /**
     * Valida si un token JWT es válido.
     *
     * @param token Token JWT a validar
     * @return true si es válido, false si no
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    /**
     * Extrae el email del usuario desde un token JWT.
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    public String getEmailFromToken(String token) {
        return jwtUtils.getEmailFromToken(token);
    }
}