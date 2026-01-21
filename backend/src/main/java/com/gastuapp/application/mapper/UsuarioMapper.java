package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.AdminCrearUsuarioRequestDTO;
import com.gastuapp.application.dto.request.CrearHijoRequestDTO;
import com.gastuapp.application.dto.request.RegistroRequestDTO;
import com.gastuapp.application.dto.response.UsuarioResponseDTO;
import com.gastuapp.domain.model.usuario.RolUsuario;
import com.gastuapp.domain.model.usuario.TipologiaUsuario;
import com.gastuapp.domain.model.usuario.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Application Mapper: UsuarioMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: UsuarioService, AuthService
 * - CONVIERTE: DTOs (Request/Response) ↔ Usuario (Domain)
 *
 * RESPONSABILIDAD:
 * Traduce entre objetos de transferencia de datos (DTOs) y modelos de dominio.
 * Maneja el hasheo de passwords con BCrypt.
 * Asigna roles automáticamente según el tipo de DTO.
 *
 * CONVERSIONES:
 * - RegistroRequestDTO → Usuario (rol = USER automático)
 * - CrearHijoRequestDTO → Usuario (rol = USER_HIJO automático)
 * - AdminCrearUsuarioRequestDTO → Usuario (rol seleccionable)
 * - Usuario → UsuarioResponseDTO (sin password, con publicId)
 *
 * SEGURIDAD:
 * - Hashea passwords con BCrypt antes de convertir a Domain
 * - ResponseDTO NO incluye password
 * - ResponseDTO usa publicId (UUID) en lugar de id interno
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Component
public class UsuarioMapper {
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor con inyección de dependencias.
     * BCryptPasswordEncoder debe estar configurado como Bean en SecurityConfig.
     */
    public UsuarioMapper() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    //  ============ Request DTO -> Domain ============
    /**
     * Convierte RegistroRequestDTO a Usuario (Domain).
     * Usado en registro público.
     *
     * FLUJO:
     * POST /api/auth/register → RegistroRequestDTO → [ESTE MÉTODO] → Usuario → Service
     *
     * REGLAS:
     * - Rol: Siempre USER (automático)
     * - Password: Hasheado con BCrypt
     * - TutorId: null (no aplica para registro público)
     * - Activo: true (inicializado por Usuario.inicializarValoresPorDefecto())
     *
     * @param dto RegistroRequestDTO del cliente
     * @return Usuario del Domain con rol USER
     */
    public Usuario registroDTOToDomain(RegistroRequestDTO dto){
        if (dto == null){
            return null;
        }

        Usuario usuario = new Usuario();

        // Datos básicos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());

        // Password hasheado
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Rol automático: USER
        usuario.setRol(RolUsuario.USER);

        // Tipología (opcional)
        usuario.setTipologia(dto.getTipologia());
        usuario.setProfesion(dto.getProfesion());
        usuario.setInstitucion(dto.getInstitucion());

        // No tiene tutor (registro público)
        usuario.setTutorId(null);

        // Inicializar valores por defecto (activo, fechaCreacion)
        usuario.inicializarValoresPorDefecto();

        return usuario;
    }

    /**
     * Convierte CrearHijoRequestDTO a Usuario (Domain).
     * Usado cuando un padre crea un hijo supervisado.
     *
     * FLUJO:
     * POST /api/usuarios/hijo → CrearHijoRequestDTO → [ESTE MÉTODO] → Usuario → Service
     *
     * REGLAS:
     * - Rol: Siempre USER_HIJO (automático)
     * - Tipología: Siempre ESTUDIANTE (automático)
     * - TutorId: Debe pasarse como parámetro (obtenido del JWT)
     * - Password: Hasheado con BCrypt
     *
     * @param dto CrearHijoRequestDTO del cliente
     * @param tutorId ID del padre (obtenido del usuario autenticado)
     * @return Usuario del Domain con rol USER_HIJO
     */
    public Usuario crearHijoDTOToDomain(CrearHijoRequestDTO dto, Long tutorId) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();

        // Datos básicos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());

        // Password hasheado
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Rol automático: USER_HIJO
        usuario.setRol(RolUsuario.USER_HIJO);

        // Tipología automática: ESTUDIANTE
        usuario.setTipologia(TipologiaUsuario.ESTUDIANTE);
        usuario.setProfesion(dto.getProfesion());
        usuario.setInstitucion(dto.getInstitucion());

        // Asignar tutor (padre)
        usuario.setTutorId(tutorId);

        // Inicializar valores por defecto
        usuario.inicializarValoresPorDefecto();

        return usuario;
    }

    /**
     * Convierte AdminCrearUsuarioRequestDTO a Usuario (Domain).
     * Usado cuando un ADMIN crea un usuario con rol específico.
     *
     * FLUJO:
     * POST /api/admin/usuarios → AdminCrearUsuarioRequestDTO → [ESTE MÉTODO] → Usuario → Service
     *
     * REGLAS:
     * - Rol: Seleccionado por el ADMIN (viene en el DTO)
     * - Password: Hasheado con BCrypt
     * - TutorId: Opcional (solo si rol = USER_HIJO)
     *
     * @param dto AdminCrearUsuarioRequestDTO del cliente
     * @return Usuario del Domain con rol especificado
     */
    public Usuario adminCrearDTOToDomain(AdminCrearUsuarioRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();

        // Datos básicos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());

        // Password hasheado
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Rol seleccionado por el ADMIN
        usuario.setRol(dto.getRol());

        // Tipología y datos demográficos
        usuario.setTipologia(dto.getTipologia());
        usuario.setProfesion(dto.getProfesion());
        usuario.setInstitucion(dto.getInstitucion());

        // TutorId (solo si rol = USER_HIJO)
        usuario.setTutorId(dto.getTutorId());

        // Inicializar valores por defecto
        usuario.inicializarValoresPorDefecto();

        return usuario;
    }

    //  ============ Domain -> Response DTO ============
    /**
     * Convierte Usuario (Domain) a UsuarioResponseDTO.
     * Usado para enviar datos al cliente.
     *
     * FLUJO:
     * Service → Usuario → [ESTE MÉTODO] → UsuarioResponseDTO → Controller → JSON
     *
     * SEGURIDAD:
     * - NO incluye password (nunca se envía al cliente)
     * - USA publicId (UUID) en lugar de id interno
     *
     * @param usuario Usuario del Domain
     * @return UsuarioResponseDTO para el cliente
     */
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        // ID público (UUID) - NO exponer id interno
        dto.setPublicId(usuario.getPublicId());

        // Datos básicos (SIN password)
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());

        // Roles y estado
        dto.setRol(usuario.getRol());
        dto.setTipologia(usuario.getTipologia());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());

        // Información demográfica
        dto.setProfesion(usuario.getProfesion());
        dto.setInstitucion(usuario.getInstitucion());

        // Relación padre-hijo
        dto.setTutorId(usuario.getTutorId());

        // OAuth
        dto.setGoogleId(usuario.getGoogleId());

        return dto;
    }

    //  ============ Utilidades ============
    /**
     * Hashea un password en texto plano.
     * Usado cuando se actualiza el password de un usuario.
     *
     * @param plainPassword Password en texto plano
     * @return Password hasheado con BCrypt
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verifica si un password en texto plano coincide con un hash BCrypt.
     * Usado en el proceso de login.
     *
     * @param plainPassword Password en texto plano
     * @param hashedPassword Password hasheado (de la BD)
     * @return true si coinciden, false si no
     */
    public boolean matchesPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
