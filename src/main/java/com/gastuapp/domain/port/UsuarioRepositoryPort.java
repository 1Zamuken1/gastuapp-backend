package com.gastuapp.domain.port;

import com.gastuapp.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

// Este port será implementado por UsuarioRepositoryAdapter en Infrastructure.
public interface UsuarioRepositoryPort {
    /**
     * Guarda un usuario (create o update)
     * @param usuario Usuario a guardar
     * @return Usuario guardado con ID asignado
     */
    Usuario save(Usuario usuario);

    /**
     * Busca un usuario por su ID.
     * @param id ID del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findById(Long id);

    /**
     * Busca un usuario por su email.
     * @param email Email del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su teléfono.
     * @param telefono Teléfono del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByTelefono(String telefono);

    /**
     * Busca todos los usuarios hijos de un tutor.
     * @param tutorId ID del tutor (padre)
     * @return Lista de usuarios hijos
     */
    List<Usuario> findByTutorId(Long tutorId);

    /**
     * Lista todos los usuarios activos.
     * @return Lista de usuarios activos
     */
    List<Usuario> findAllActivos();

    /**
     * Verifica si existe un usuario con ese email.
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existByEmail(String email);

    /**
     * Verifica si existe un usuario con ese teléfono.
     * @param telefono Teléfono a verificar
     * @return true si existe, false si no
     */
    boolean existsByTelefono(String telefono);

    /**
     * Elimina un usuario por su ID (soft delete - marca como inactivo).
     * @param id ID del usuario a eliminar
     */
    void deleteById(Long id);
}
