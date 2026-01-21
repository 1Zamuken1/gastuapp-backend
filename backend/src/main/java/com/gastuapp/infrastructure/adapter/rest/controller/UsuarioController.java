package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.CrearHijoRequestDTO;
import com.gastuapp.application.dto.response.UsuarioResponseDTO;
import com.gastuapp.application.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller: UsuarioController
 *
 * FLUJO DE DATOS:
 * - RECIBE: HTTP Requests (JSON) con token JWT
 * - LLAMA A: UsuarioService (Application Layer)
 * - RETORNA: HTTP Responses (JSON) con datos de usuarios
 *
 * RESPONSABILIDAD:
 * Maneja endpoints protegidos para gestión de usuarios.
 * Requiere autenticación JWT (JwtAuthenticationFilter).
 * Permite a usuarios gestionar su perfil y relaciones padre-hijo.
 *
 * ENDPOINTS:
 * - GET /api/usuarios/me → Obtener perfil del usuario autenticado
 * - PUT /api/usuarios/me → Actualizar perfil del usuario autenticado
 * - GET /api/usuarios/{publicId} → Obtener usuario por publicId
 * - GET /api/usuarios/hijos → Listar hijos del usuario autenticado
 * - POST /api/usuarios/hijo → Crear hijo supervisado (solo USER)
 *
 * SEGURIDAD:
 * - Todos los endpoints requieren JWT válido
 * - Usuario solo puede ver/editar su propio perfil
 * - Padre puede ver perfiles de sus hijos
 * - Hijo NO puede ver perfil del padre
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ============ Perfil Propio ============
    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * FLUJO:
     * Cliente → GET /api/usuarios/me (con JWT)
     * → JwtFilter (extrae email) → [ESTE MÉTODO] → UsuarioService
     *
     * RESPONSE (200 OK):
     * {
     * "publicId": "550e8400-...",
     * "nombre": "Juan",
     * "email": "juan@example.com",
     * "rol": "USER",
     * "tipologia": "TRABAJADOR",
     * "activo": true
     * }
     *
     * @return ResponseEntity con UsuarioResponseDTO
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * FLUJO:
     * Cliente → PUT /api/usuarios/me (con JWT + datos)
     * → [ESTE MÉTODO] → UsuarioService → BD
     *
     * REQUEST BODY:
     * {
     * "nombre": "Juan Carlos",
     * "apellido": "Pérez Gómez",
     * "telefono": "3001234567"
     * }
     *
     * CAMPOS ACTUALIZABLES:
     * - nombre, apellido, telefono
     *
     * CAMPOS NO ACTUALIZABLES:
     * - email, password, rol, publicId
     *
     * @param nombre   Nuevo nombre (opcional)
     * @param apellido Nuevo apellido (opcional)
     * @param telefono Nuevo teléfono (opcional)
     * @return ResponseEntity con UsuarioResponseDTO actualizado
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizarPerfil(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String telefono) {
        Long usuarioId = obtenerUsuarioIdAutenticado();

        // Actualizar directamente con el id
        UsuarioResponseDTO actualizado = usuarioService.actualizarUsuario(
                usuarioId,
                nombre,
                apellido,
                telefono);

        return ResponseEntity.ok(actualizado);
    }

    // ============ Ver otros usuarios ============
    /**
     * Obtiene un usuario por su publicId.
     *
     * SEGURIDAD:
     * - Usuario solo puede ver su propio perfil
     * - Padre puede ver perfiles de sus hijos
     * - Hijo NO puede ver perfil del padre
     *
     * FLUJO:
     * Cliente → GET /api/usuarios/{publicId} (con JWT)
     * → [ESTE MÉTODO] → validar permisos → UsuarioService
     *
     * @param publicId UUID del usuario a consultar
     * @return ResponseEntity con UsuarioResponseDTO
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable String publicId) {
        Long usuarioIdAutenticado = obtenerUsuarioIdAutenticado();
        UsuarioResponseDTO usuarioAutenticado = usuarioService.buscarPorId(usuarioIdAutenticado);
        UsuarioResponseDTO usuarioSolicitado = usuarioService.buscarPorPublicId(publicId);

        // Validar permisos
        if (!puedeVerPerfil(usuarioAutenticado, usuarioSolicitado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioSolicitado);
    }

    // ============ Relación Padre-hijo ============
    /**
     * Lista todos los hijos del usuario autenticado.
     *
     * FLUJO:
     * Cliente (padre) → GET /api/usuarios/hijos (con JWT)
     * → [ESTE MÉTODO] → UsuarioService → Lista de hijos
     *
     * RESPONSE (200 OK):
     * [
     * {
     * "publicId": "...",
     * "nombre": "María",
     * "rol": "USER_HIJO",
     * "tutorId": 123
     * }
     * ]
     *
     * @return ResponseEntity con lista de UsuarioResponseDTO
     */
    @GetMapping("/hijos")
    public ResponseEntity<List<UsuarioResponseDTO>> listarHijos() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);

        // Validar que sea USER (Puede tener hijos)
        if (!usuario.getRol().name().equals("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UsuarioResponseDTO> hijos = usuarioService.listarHijosDeTutor(usuarioId);
        return ResponseEntity.ok(hijos);
    }

    /**
     * Crea un usuario hijo supervisado por el usuario autenticado.
     *
     * FLUJO:
     * Cliente (padre) → POST /api/usuarios/hijo (con JWT + datos)
     * → [ESTE MÉTODO] → UsuarioService → Crear hijo con rol USER_HIJO
     *
     * REQUEST BODY:
     * {
     * "nombre": "María",
     * "apellido": "Pérez",
     * "email": "maria@example.com",
     * "password": "password123",
     * "profesion": "Estudiante de secundaria",
     * "institucion": "Colegio San José"
     * }
     *
     * ROL ASIGNADO: USER_HIJO (automático)
     * TUTOR: Usuario autenticado (padre)
     *
     * @param dto CrearHijoRequestDTO con datos del hijo
     * @return ResponseEntity con UsuarioResponseDTO (201 Created)
     */
    @PostMapping("/hijo")
    public ResponseEntity<UsuarioResponseDTO> crearHijo(@Valid @RequestBody CrearHijoRequestDTO dto) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);

        // Validar que sea USER (puede tener hijos)
        if (!usuario.getRol().name().equals("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        // Crear hijo con tutorId del usuario autenticado
        UsuarioResponseDTO hijo = usuarioService.crearHijo(dto, usuarioId);
        return new ResponseEntity<>(hijo, HttpStatus.CREATED);
    }

    // ============ Utilidades ============
    /**
     * Obtiene el email del usuario autenticado desde el SecurityContext.
     *
     * @return Email del usuario autenticado
     */
    private Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName(); // Ahora es userId como String
        return Long.parseLong(userIdStr);
    }

    /**
     * Valida si un usuario puede ver el perfil de otro.
     *
     * REGLAS:
     * - Usuario puede ver su propio perfil
     * - Padre puede ver perfil de sus hijos
     * - Hijo NO puede ver perfil del padre
     *
     * @param solicitante Usuario que solicita ver el perfil
     * @param objetivo    Usuario cuyo perfil se quiere ver
     * @return true si puede ver, false si no
     */
    private boolean puedeVerPerfil(UsuarioResponseDTO solicitante, UsuarioResponseDTO objetivo) {
        // Caso 1: Es su propio perfil
        if (solicitante.getPublicId().equals(objetivo.getPublicId())) {
            return true;
        }

        // Caso 2: Es padre y objetivo es su hijo
        if (objetivo.getTutorId() != null &&
                objetivo.getTutorId().equals(solicitante.getId())) {
            return true;
        }

        // Caso 3: NO permitido (hijo viendo padre u otro usuario)
        return false;
    }
}
