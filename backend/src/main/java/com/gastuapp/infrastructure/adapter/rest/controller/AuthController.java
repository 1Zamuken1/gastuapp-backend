package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.LoginRequestDTO;
import com.gastuapp.application.dto.request.RegistroRequestDTO;
import com.gastuapp.application.dto.response.AuthResponseDTO;
import com.gastuapp.application.service.AuthService;
import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller: AuthController
 *
 * FLUJO DE DATOS:
 * - RECIBE: HTTP Requests (JSON) desde el cliente
 * - LLAMA A: AuthService (Application Layer)
 * - RETORNA: HTTP Responses (JSON) con token JWT
 *
 * RESPONSABILIDAD:
 * Maneja endpoints públicos de autenticación.
 * Convierte requests HTTP en llamadas a AuthService.
 * Retorna respuestas HTTP con tokens JWT y códigos de estado apropiados.
 *
 * ENDPOINTS:
 * - POST /api/auth/register → Registrar nuevo usuario (retorna JWT)
 * - POST /api/auth/login → Iniciar sesión (retorna JWT)
 * - GET /api/auth/health → Health check
 *
 * VALIDACIONES:
 * - Jakarta Validation en DTOs (@Valid)
 * - Validaciones de negocio en AuthService
 *
 * CÓDIGOS HTTP:
 * - 200 OK: Login exitoso
 * - 201 Created: Usuario registrado
 * - 400 Bad Request: Validación fallida
 * - 401 Unauthorized: Credenciales inválidas
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo (Angular)
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepositoryPort usuarioRepository;

    public AuthController(AuthService authService, UsuarioRepositoryPort usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    // ==================== REGISTER ====================

    /**
     * Registra un nuevo usuario y retorna token JWT (auto-login).
     *
     * FLUJO:
     * Cliente → POST /api/auth/register → [ESTE MÉTODO] → AuthService → BD
     * BD → AuthService → [ESTE MÉTODO] → Cliente (con JWT)
     *
     * REQUEST BODY:
     * {
     * "nombre": "Juan",
     * "apellido": "Pérez",
     * "email": "juan@example.com",
     * "telefono": "3001234567",
     * "password": "password123",
     * "tipologia": "TRABAJADOR",
     * "profesion": "Ingeniero",
     * "institucion": "Tech Corp"
     * }
     *
     * RESPONSE (201 Created):
     * {
     * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "type": "Bearer",
     * "publicId": "550e8400-...",
     * "email": "juan@example.com",
     * "rol": "USER"
     * }
     *
     * @param dto RegistroRequestDTO con datos del nuevo usuario
     * @return ResponseEntity con AuthResponseDTO (201 Created)
     */
    /**
     * @deprecated Usar Supabase Auth (supabase.auth.signUp()) desde el frontend.
     *             Este endpoint se mantendrá durante la migración.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegistroRequestDTO dto) {
        AuthResponseDTO response = authService.register(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== LOGIN ====================

    /**
     * Inicia sesión y retorna token JWT.
     *
     * FLUJO:
     * Cliente → POST /api/auth/login → [ESTE MÉTODO] → AuthService → BD
     * BD → AuthService → [ESTE MÉTODO] → Cliente (con JWT)
     *
     * REQUEST BODY:
     * {
     * "email": "juan@example.com",
     * "password": "password123"
     * }
     *
     * RESPONSE (200 OK):
     * {
     * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "type": "Bearer",
     * "publicId": "550e8400-...",
     * "email": "juan@example.com",
     * "rol": "USER"
     * }
     *
     * @param dto LoginRequestDTO con email y password
     * @return ResponseEntity con AuthResponseDTO (200 OK)
     */
    /**
     * @deprecated Usar Supabase Auth (supabase.auth.signInWithPassword()) desde el
     *             frontend.
     *             Este endpoint se mantendrá durante la migración.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    // ==================== HEALTH CHECK ====================

    /**
     * Health check del controller.
     *
     * @return Mensaje de confirmación
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AuthController funcionando correctamente");
    }

    // ==================== SUPABASE AUTH ====================

    /**
     * Retorna la información del usuario autenticado.
     * Funciona con tokens de Supabase Auth.
     *
     * FLUJO:
     * Cliente → GET /api/auth/me (con Bearer token de Supabase)
     * → JwtAuthenticationFilter valida token
     * → SecurityContext contiene el supabaseUid
     * → [ESTE MÉTODO] busca el usuario por supabaseUid
     * → Retorna datos del usuario
     *
     * @return AuthResponseDTO con datos del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String principal = auth.getName();

        // Intentar buscar por supabaseUid (UUID format)
        Usuario usuario = null;
        try {
            java.util.UUID.fromString(principal);
            usuario = usuarioRepository.findBySupabaseUid(principal).orElse(null);
        } catch (IllegalArgumentException e) {
            // No es UUID, intentar como userId legado
            try {
                Long userId = Long.parseLong(principal);
                usuario = usuarioRepository.findById(userId).orElse(null);
            } catch (NumberFormatException ignored) {
                // No es ni UUID ni Long
            }
        }

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        AuthResponseDTO response = new AuthResponseDTO(
                null, // No generamos token, el frontend ya lo tiene de Supabase
                "Bearer",
                usuario.getPublicId(),
                usuario.getEmail(),
                usuario.getRol().name());

        return ResponseEntity.ok(response);
    }
}