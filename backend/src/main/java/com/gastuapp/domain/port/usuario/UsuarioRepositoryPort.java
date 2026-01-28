package com.gastuapp.domain.port.usuario;

import com.gastuapp.domain.model.usuario.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Port: UsuarioRepositoryPort
 *
 * FLUJO DE DATOS:
 * - USADO POR: UsuarioService (Application Layer)
 * - IMPLEMENTADO POR: UsuarioRepositoryAdapter (Infrastructure Layer)
 *
 * RESPONSABILIDAD:
 * Define el contrato para operaciones de persistencia de Usuario.
 * Establece QUÉ operaciones están disponibles, pero NO define CÓMO se implementan.
 *
 * ARQUITECTURA HEXAGONAL:
 * Este es un "Puerto de salida" (Output Port) del Domain hacia Infrastructure.
 * Permite que el Domain solicite persistencia sin conocer detalles técnicos (JPA, SQL, etc.)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
public interface UsuarioRepositoryPort {

    /**
     * Guarda un usuario (create o update).
     *
     * FLUJO:
     * UsuarioService → UsuarioRepositoryPort → UsuarioRepositoryAdapter → JPA → PostgreSQL
     *
     * @param usuario Usuario a guardar (Domain model)
     * @return Usuario guardado con ID asignado (Domain model)
     */
    Usuario save(Usuario usuario);

    /**
     * Busca un usuario por su ID.
     *
     * FLUJO:
     * UsuarioService → UsuarioRepositoryPort → UsuarioRepositoryAdapter → JPA → PostgreSQL
     * PostgreSQL → JPA → UsuarioRepositoryAdapter → UsuarioRepositoryPort → UsuarioService
     *
     * @param id ID del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findById(Long id);

    /**
     * Busca un usuario por su publicId (UUID).
     * Este método debe usarse en APIs públicas en lugar de findById.
     *
     * @param publicId UUID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByPublicId(String publicId);

    /**
     * Busca un usuario por su email.
     * Usado principalmente para login y validación de duplicados.
     *
     * @param email Email del usuario (único en el sistema)
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su teléfono.
     * Usado para login alternativo (si se implementa).
     *
     * @param telefono Teléfono del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByTelefono(String telefono);

    /**
     * Busca todos los usuarios hijos de un tutor.
     * Usado para que un padre (USER) vea la lista de sus hijos supervisados.
     *
     * @param tutorId ID del tutor (padre con rol USER)
     * @return Lista de usuarios hijos (rol USER_HIJO)
     */
    List<Usuario> findByTutorId(Long tutorId);

    /**
     * Lista todos los usuarios activos.
     * Usado por ADMIN para gestión de usuarios.
     *
     * @return Lista de usuarios con activo = true
     */
    List<Usuario> findAllActivos();

    /**
     * Verifica si existe un usuario con ese email.
     * Usado para validar duplicados antes de registrar.
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con ese teléfono.
     * Usado para validar duplicados antes de registrar.
     *
     * @param telefono Teléfono a verificar
     * @return true si existe, false si no
     */
    boolean existsByTelefono(String telefono);

    /**
     * Elimina un usuario por su ID (soft delete).
     * No borra físicamente de la BD, solo marca activo = false.
     *
     * FLUJO:
     * UsuarioService → UsuarioRepositoryPort → UsuarioRepositoryAdapter → JPA → PostgreSQL
     * (PostgreSQL: UPDATE usuarios SET activo = false WHERE id = ?)
     *
     * @param id ID del usuario a eliminar
     */
    void deleteById(Long id);

    /**
     * Verifica si existe un usuario por su ID.
     *
     * @param id ID del usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsById(Long id);
}