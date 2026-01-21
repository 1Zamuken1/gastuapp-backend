package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.AdminCrearUsuarioRequestDTO;
import com.gastuapp.application.dto.request.CrearHijoRequestDTO;
import com.gastuapp.application.dto.request.RegistroRequestDTO;
import com.gastuapp.application.dto.response.UsuarioResponseDTO;
import com.gastuapp.application.mapper.UsuarioMapper;
import com.gastuapp.domain.model.usuario.RolUsuario;
import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application Service: UsuarioService
 *
 * FLUJO DE DATOS:
 * - RECIBE: DTOs desde Controllers
 * - USA: UsuarioRepositoryPort (Domain Port)
 * - USA: UsuarioMapper (conversión DTO ↔ Domain)
 * - RETORNA: DTOs a Controllers
 *
 * RESPONSABILIDAD:
 * Orquesta los casos de uso relacionados con usuarios.
 * Valida reglas de negocio antes de persistir.
 * Coordina entre Domain, Mappers y Repositories.
 *
 * CASOS DE USO:
 * 1. Registrar usuario público (rol USER automático)
 * 2. Crear usuario hijo supervisado (rol USER_HIJO)
 * 3. Crear usuario por admin (rol configurable)
 * 4. Buscar usuario por ID público (UUID)
 * 5. Buscar usuario por email
 * 6. Listar todos los usuarios activos
 * 7. Listar hijos de un tutor
 * 8. Actualizar usuario
 * 9. Eliminar usuario (soft delete)
 *
 * VALIDACIONES:
 * - Email único en el sistema
 * - Teléfono único (si se proporciona)
 * - USER_HIJO debe tener tutor
 * - Solo USER puede tener hijos
 * - Validaciones de Domain (Usuario.validar())
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepositoryPort usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente el repository y el mapper.
     */
    public UsuarioService(
            UsuarioRepositoryPort usuarioRepository,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    // ============ Caso de uso 1: Registro público ============
    /**
     * Registra un nuevo usuario con rol USER (registro público).
     *
     * FLUJO:
     * POST /api/auth/register → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * VALIDACIONES:
     * - Email no debe existir en el sistema
     * - Teléfono no debe existir (si se proporciona)
     * - Todas las validaciones del Domain (Usuario.validar())
     *
     * ROL ASIGNADO: USER (automático)
     *
     * @param dto RegistroRequestDTO del cliente
     * @return UsuarioResponseDTO con el usuario creado
     * @throws IllegalArgumentException si el email o teléfono ya existen
     */
    public UsuarioResponseDTO registrarUsuario(RegistroRequestDTO dto) {
        // 1. Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "El email ya está registrado en el sistema");
        }

        // 2. Validar que el teléfono no exista (en caso de ser proporcionado)
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            if (usuarioRepository.existsByTelefono(dto.getTelefono())) {
                throw new IllegalArgumentException(
                        "El teléfono ya está registrado en el sistema");
            }
        }

        // 3. Convertir DTO -> Domain
        Usuario usuario = usuarioMapper.registroDTOToDomain(dto);

        // 4. Validar reglas de negocio del Domain
        usuario.validar();

        // 5. Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 6. Convertir Domain -> DTO Response
        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }

    // ============ Caso de uso 2: Crear Hijo ============
    /**
     * Crea un usuario hijo supervisado (USER_HIJO).
     * Solo puede ser llamado por un usuario autenticado con rol USER.
     *
     * FLUJO:
     * POST /api/usuarios/hijo → Controller (obtiene tutorId del JWT) → [ESTE
     * MÉTODO] → Repository → BD
     *
     * VALIDACIONES:
     * - Email no debe existir
     * - Teléfono no debe existir (si se proporciona)
     * - El tutor debe existir y ser USER
     *
     * ROL ASIGNADO: USER_HIJO (automático)
     * TIPOLOGÍA: ESTUDIANTE (automático)
     *
     * @param dto     CrearHijoRequestDTO del cliente
     * @param tutorId ID del padre (obtenido del JWT del usuario autenticado)
     * @return UsuarioResponseDTO con el hijo creado
     * @throws IllegalArgumentException si validaciones fallan
     */
    public UsuarioResponseDTO crearHijo(CrearHijoRequestDTO dto, Long tutorId) {
        // 1. Validar que el tutor exista
        Usuario tutor = usuarioRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El tutor no existe"));

        // 2. Validar que el tutor sea USER (Puede tener hijos)
        if (!tutor.puedeTenerHijos()) {
            throw new IllegalArgumentException(
                    "Solo usuarios con rol USER pueden tener hijos supervisados");
        }

        // 3. Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "El email ya está registrado en el sistema");
        }

        // 4. Validar que el teléfono no exista (si se proporciona)
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            if (usuarioRepository.existsByTelefono(dto.getTelefono())) {
                throw new IllegalArgumentException(
                        "El teléfono ya está registrado en el sistema");
            }
        }

        // 5. Convertir DTO -> Domain (con tutorId)
        Usuario hijo = usuarioMapper.crearHijoDTOToDomain(dto, tutorId);

        // 6. Validar reglas de negocio del Domain
        hijo.validar();

        // 7. Guardar en BD
        Usuario hijoGuardado = usuarioRepository.save(hijo);

        // 8. Convertir Domain → DTO Response
        return usuarioMapper.toResponseDTO(hijoGuardado);
    }

    // ============ Caso de uso 3: Admin Crea Usuario ============
    /**
     * Crea un usuario con rol específico (solo ADMIN).
     * El ADMIN puede elegir cualquier rol.
     *
     * FLUJO:
     * POST /api/admin/usuarios → Controller (verifica rol ADMIN) → [ESTE MÉTODO] →
     * Repository → BD
     *
     * VALIDACIONES:
     * - Email no debe existir
     * - Teléfono no debe existir (si se proporciona)
     * - Si rol = USER_HIJO, tutorId debe existir y ser USER
     *
     * @param dto AdminCrearUsuarioRequestDTO del cliente
     * @return UsuarioResponseDTO con el usuario creado
     * @throws IllegalArgumentException si validaciones fallan
     */
    public UsuarioResponseDTO adminCrearUsuario(AdminCrearUsuarioRequestDTO dto) {
        // 1. Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "El email " + dto.getEmail() + " ya está registrado en el sistema");
        }

        // 2. Validar que el teléfono no exista (si se proporciona)
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            if (usuarioRepository.existsByTelefono(dto.getTelefono())) {
                throw new IllegalArgumentException(
                        "El teléfono " + dto.getTelefono() + " ya está registrado en el sistema");
            }
        }

        // 3. Si es USER_HIJO, validar que el tutor exista y sea USER
        if (dto.getRol() == RolUsuario.USER_HIJO && dto.getTutorId() != null) {
            Usuario tutor = usuarioRepository.findById(dto.getTutorId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El tutor con ID " + dto.getTutorId() + " no existe"));

            if (!tutor.puedeTenerHijos()) {
                throw new IllegalArgumentException(
                        "El tutor debe tener rol USER");
            }
        }

        // 4. Convertir DTO → Domain
        Usuario usuario = usuarioMapper.adminCrearDTOToDomain(dto);

        // 5. Validar reglas de negocio del Domain
        usuario.validar();

        // 6. Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 7. Convertir Domain → DTO Response
        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }

    // ============ Caso de uso 4: Buscar por public Id ============
    /**
     * Busca un usuario por su publicId (UUID).
     * Este es el método que deben usar los Controllers públicos.
     *
     * FLUJO:
     * GET /api/usuarios/{publicId} → Controller → [ESTE MÉTODO] → Repository → BD
     *
     * @param publicId UUID del usuario
     * @return UsuarioResponseDTO si existe
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorPublicId(String publicId) {
        Usuario usuario = usuarioRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con publicId no encontrado"));

        return usuarioMapper.toResponseDTO(usuario);
    }

    /**
     * Busca un usuario por su ID interno.
     * Usado para obtener el perfil del usuario autenticado.
     *
     * @param id ID interno del usuario
     * @return UsuarioResponseDTO
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con ID " + id + " no encontrado"));

        return usuarioMapper.toResponseDTO(usuario);
    }

    // ============ Caso de uso 5: Buscar por email ============
    /**
     * Busca un usuario por su email.
     * Usado principalmente para login.
     *
     * @param email Email del usuario
     * @return UsuarioResponseDTO si existe
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con email " + email + " no encontrado"));

        return usuarioMapper.toResponseDTO(usuario);
    }

    // ============ Caso de uso 6: Listar activos ============
    /**
     * Lista todos los usuarios activos.
     * Usado por ADMIN para gestión de usuarios.
     *
     * @return Lista de UsuarioResponseDTO
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosActivos() {
        return usuarioRepository.findAllActivos().stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ============ Caso de uso 7: Listar hijos de un tutor ============
    /**
     * Lista todos los hijos de un tutor.
     * Usado por un padre (USER) para ver sus hijos supervisados.
     *
     * @param tutorId ID del tutor (obtenido del JWT)
     * @return Lista de UsuarioResponseDTO con los hijos
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarHijosDeTutor(Long tutorId) {
        // Validar que el tutor exista
        usuarioRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tutor con ID " + tutorId + " no encontrado"));

        return usuarioRepository.findByTutorId(tutorId).stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ============ Caso de uso 8: Actualizar usuario ============
    /**
     * Actualiza los datos de un usuario existente.
     * NO permite cambiar: id, publicId, rol, fechaCreacion.
     *
     * @param id       ID interno del usuario
     * @param nombre   Nuevo nombre (opcional)
     * @param apellido Nuevo apellido (opcional)
     * @param telefono Nuevo teléfono (opcional)
     * @return UsuarioResponseDTO actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public UsuarioResponseDTO actualizarUsuario(
            Long id,
            String nombre,
            String apellido,
            String telefono) {

        // 1. Buscar usuario existente
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con ID " + id + " no encontrado"));

        // 2. Actualizar campos (solo si no son null)
        if (nombre != null && !nombre.trim().isEmpty()) {
            usuario.setNombre(nombre);
        }

        if (apellido != null && !apellido.trim().isEmpty()) {
            usuario.setApellido(apellido);
        }

        if (telefono != null && !telefono.trim().isEmpty()) {
            // Validar que el teléfono no esté en uso por otro usuario
            Optional<Usuario> usuarioConTelefono = usuarioRepository.findByTelefono(telefono);
            if (usuarioConTelefono.isPresent() && !usuarioConTelefono.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                        "El teléfono " + telefono + " ya está registrado por otro usuario");
            }
            usuario.setTelefono(telefono);
        }

        // 3. Validar reglas de negocio
        usuario.validar();

        // 4. Guardar cambios
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        // 5. Convertir a DTO
        return usuarioMapper.toResponseDTO(usuarioActualizado);
    }

    // ============ Caso de uso 9: Eliminar Usuario (Soft Delete) ============
    /**
     * Elimina un usuario (soft delete: marca como inactivo).
     * No borra físicamente de la BD.
     *
     * @param id ID interno del usuario
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void eliminarUsuario(Long id) {
        // Verificar que el usuario exista
        usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con ID " + id + " no encontrado"));

        // Soft delete (marca activo = false)
        usuarioRepository.deleteById(id);
    }

    // ============ Métodos Auxiliares ============
    /**
     * Verifica si un email ya existe en el sistema.
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Verifica si un teléfono ya existe en el sistema.
     *
     * @param telefono Teléfono a verificar
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existeTelefono(String telefono) {
        return usuarioRepository.existsByTelefono(telefono);
    }
}
