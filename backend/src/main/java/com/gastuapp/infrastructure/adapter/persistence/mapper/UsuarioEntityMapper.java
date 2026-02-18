package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.usuario.RolUsuario;
import com.gastuapp.domain.model.usuario.TipologiaUsuario;
import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.infrastructure.adapter.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Entity Mapper: UsuarioEntityMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: UsuarioRepositoryAdapter
 * - CONVIERTE: Usuario (Domain) ↔ UsuarioEntity (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Traduce entre el modelo de dominio puro y la entidad JPA.
 * Mapea enums de Domain a enums de Entity y viceversa.
 * Mantiene la independencia de capas (Domain no conoce JPA).
 *
 * CONVERSIONES:
 * - toDomain(): UsuarioEntity → Usuario (para leer de BD)
 * - toEntity(): Usuario → UsuarioEntity (para guardar en BD)
 * - updateEntity(): Actualiza UsuarioEntity existente con datos de Usuario
 *
 * MAPEO DE ENUMS:
 * - RolUsuario (Domain) ↔ UsuarioEntity.RolUsuarioEnum (Infrastructure)
 * - TipologiaUsuario (Domain) ↔ UsuarioEntity.TipologiaUsuarioEnum
 * (Infrastructure)
 *
 * NOTAS:
 * - El 'id' y 'publicId' se mapean directamente
 * - El 'password' se mantiene hasheado (no se procesa aquí)
 * - Los campos null se mapean como null (el Domain los valida)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Component
public class UsuarioEntityMapper {
    // ============ Entity -> Domain ============
    /**
     * Convierte UsuarioEntity (JPA) a Usuario (Domain).
     * Usado cuando se lee de la base de datos.
     *
     * FLUJO:
     * PostgreSQL → JpaRepository → UsuarioEntity → [ESTE MÉTODO] → Usuario →
     * Service
     *
     * @param entity UsuarioEntity de la BD
     * @return Usuario del Domain (null si entity es null)
     */
    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }

        Usuario usuario = new Usuario();

        // IDs
        usuario.setId(entity.getId());
        usuario.setPublicId(entity.getPublicId());

        // Datos básicos
        usuario.setNombre(entity.getNombre());
        usuario.setApellido(entity.getApellido());
        usuario.setEmail(entity.getEmail());
        usuario.setTelefono(entity.getTelefono());
        usuario.setPassword(entity.getPassword());

        // Enums
        usuario.setRol(mapRolToDomain(entity.getRol()));
        usuario.setTipologia(mapTipologiaToDomain(entity.getTipologia()));

        // Estado
        usuario.setActivo(entity.getActivo());
        usuario.setFechaCreacion(entity.getFechaCreacion());

        // Información demgráfica
        usuario.setProfesion(entity.getProfesion());
        usuario.setInstitucion(entity.getInstitucion());

        // Relación padre-hijo
        usuario.setTutorId(entity.getTutorId());

        // Supabase Auth
        usuario.setSupabaseUid(entity.getSupabaseUid() != null ? entity.getSupabaseUid().toString() : null);

        return usuario;
    }

    // ============ Domain -> Entity ============
    /**
     * Convierte Usuario (Domain) a UsuarioEntity (JPA).
     * Usado cuando se va a guardar en la base de datos.
     *
     * FLUJO:
     * Service → Usuario → [ESTE MÉTODO] → UsuarioEntity → JpaRepository →
     * PostgreSQL
     *
     * @param usuario Usuario del Domain
     * @return UsuarioEntity para la BD (null si usuario es null)
     */
    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioEntity entity = new UsuarioEntity();

        // IDs
        entity.setId(usuario.getId());
        entity.setPublicId(usuario.getPublicId());

        // Datos básicos
        entity.setNombre(usuario.getNombre());
        entity.setApellido(usuario.getApellido());
        entity.setEmail(usuario.getEmail());
        entity.setTelefono(usuario.getTelefono());
        entity.setPassword(usuario.getPassword());

        // Enums (convertir de Domain a Entity)
        entity.setRol(mapRolToEntity(usuario.getRol()));
        entity.setTipologia(mapTipologiaToEntity(usuario.getTipologia()));

        // Estado
        entity.setActivo(usuario.getActivo());
        entity.setFechaCreacion(usuario.getFechaCreacion());

        // Información demográfica
        entity.setProfesion(usuario.getProfesion());
        entity.setInstitucion(usuario.getInstitucion());

        // Relación padre-hijo
        entity.setTutorId(usuario.getTutorId());

        // OAuth
        entity.setGoogleId(usuario.getGoogleId());

        // Supabase Auth
        entity.setSupabaseUid(usuario.getSupabaseUid() != null ? UUID.fromString(usuario.getSupabaseUid()) : null);

        return entity;
    }

    // ============ Update entity ============
    /**
     * Actualiza una UsuarioEntity existente con datos de Usuario.
     * Usado para operaciones de UPDATE (no se cambia el ID ni publicId).
     *
     * IMPORTANTE:
     * - NO actualiza: id, publicId, fechaCreacion (inmutables)
     * - SÍ actualiza: todos los demás campos
     *
     * FLUJO:
     * Service → Usuario actualizado → [ESTE MÉTODO] → UsuarioEntity actualizada →
     * JpaRepository → PostgreSQL
     *
     * @param entity  UsuarioEntity existente (con id de BD)
     * @param usuario Usuario con datos nuevos
     */
    public void updateEntity(UsuarioEntity entity, Usuario usuario) {
        if (entity == null || usuario == null) {
            return;
        }

        // Actualizar datos básicos
        entity.setNombre(usuario.getNombre());
        entity.setApellido(usuario.getApellido());
        entity.setEmail(usuario.getEmail());
        entity.setTelefono(usuario.getTelefono());

        // Solo actualiza Password si es diferente de null
        if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
            entity.setPassword(usuario.getPassword());
        }

        // Actualizar enums
        entity.setRol(mapRolToEntity(usuario.getRol()));
        entity.setTipologia(mapTipologiaToEntity(usuario.getTipologia()));

        // Actualizar estado
        entity.setActivo(usuario.getActivo());

        // Actualizar información demográfica
        entity.setProfesion(usuario.getProfesion());
        entity.setInstitucion(usuario.getInstitucion());

        // Actualizar relación padre-hijo
        entity.setTutorId(usuario.getTutorId());

        // Actualizar OAuth
        entity.setGoogleId(usuario.getGoogleId());
    }

    // ============ Mapeo de Enums ============
    /**
     * Convierte RolUsuarioEnum (Entity) a RolUsuario (Domain).
     */
    private RolUsuario mapRolToDomain(UsuarioEntity.RolUsuarioEnum rolEnum) {
        if (rolEnum == null) {
            return null;
        }

        return switch (rolEnum) {
            case ADMIN -> RolUsuario.ADMIN;
            case USER -> RolUsuario.USER;
            case USER_HIJO -> RolUsuario.USER_HIJO;
        };
    }

    /**
     * Convierte RolUsuario (Domain) a RolUsuarioEnum (Entity).
     */
    private UsuarioEntity.RolUsuarioEnum mapRolToEntity(RolUsuario rol) {
        if (rol == null) {
            return null;
        }

        return switch (rol) {
            case ADMIN -> UsuarioEntity.RolUsuarioEnum.ADMIN;
            case USER -> UsuarioEntity.RolUsuarioEnum.USER;
            case USER_HIJO -> UsuarioEntity.RolUsuarioEnum.USER_HIJO;
        };
    }

    /**
     * Convierte TipologiaUsuarioEnum (Entity) a TipologiaUsuario (Domain).
     */
    private TipologiaUsuario mapTipologiaToDomain(UsuarioEntity.TipologiaUsuarioEnum tipologiaEnum) {
        if (tipologiaEnum == null) {
            return null;
        }

        return switch (tipologiaEnum) {
            case ESTUDIANTE -> TipologiaUsuario.ESTUDIANTE;
            case TRABAJADOR -> TipologiaUsuario.TRABAJADOR;
            case INDEPENDIENTE -> TipologiaUsuario.INDEPENDIENTE;
            case OTRO -> TipologiaUsuario.OTRO;
        };
    }

    /**
     * Convierte TipologiaUsuario (Domain) a TipologiaUsuarioEnum (Entity).
     */
    private UsuarioEntity.TipologiaUsuarioEnum mapTipologiaToEntity(TipologiaUsuario tipologia) {
        if (tipologia == null) {
            return null;
        }

        return switch (tipologia) {
            case ESTUDIANTE -> UsuarioEntity.TipologiaUsuarioEnum.ESTUDIANTE;
            case TRABAJADOR -> UsuarioEntity.TipologiaUsuarioEnum.TRABAJADOR;
            case INDEPENDIENTE -> UsuarioEntity.TipologiaUsuarioEnum.INDEPENDIENTE;
            case OTRO -> UsuarioEntity.TipologiaUsuarioEnum.OTRO;
        };
    }
}
