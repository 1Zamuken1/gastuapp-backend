package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.Usuario;
import com.gastuapp.domain.port.UsuarioRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.mapper.UsuarioEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.UsuarioJpaRepository;
import com.gastuapp.infrastructure.adapter.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter: UsuarioRepositoryAdapter
 *
 * FLUJO DE DATOS:
 * - IMPLEMENTA: UsuarioRepositoryPort (Domain Layer)
 * - RECIBE DATOS DE: UsuarioService (Application Layer) a través del Port
 * - USA: UsuarioJpaRepository (Spring Data JPA)
 * - USA: UsuarioEntityMapper (para convertir Domain ↔ Entity)
 * - ENVÍA DATOS A: PostgreSQL (a través de JpaRepository)
 *
 * RESPONSABILIDAD:
 * Implementación concreta del UsuarioRepositoryPort.
 * Conecta el Domain con la infraestructura de persistencia (JPA/PostgreSQL).
 * Convierte entre modelos de Domain y entidades de BD.
 *
 * ARQUITECTURA HEXAGONAL:
 * Este es un "Adapter de salida" (Output Adapter) que implementa un Port del Domain.
 * Permite que el Domain persista datos SIN conocer detalles de JPA o PostgreSQL.
 *
 * CONVERSIONES:
 * - Domain (Usuario) → Entity (UsuarioEntity) → PostgreSQL
 * - PostgreSQL → Entity (UsuarioEntity) → Domain (Usuario)
 *
 * MANEJO DE IDs:
 * - El Domain usa 'id' y 'publicId'
 * - Internamente usa JpaRepository con 'id' (BIGINT)
 * - Para búsquedas públicas usa 'publicId' (UUID)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {
    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioEntityMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente el repository y el mapper.
     */
    public UsuarioRepositoryAdapter(
            UsuarioJpaRepository jpaRepository,
            UsuarioEntityMapper mapper){
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    //   ============ Operaciones CRUD ============
    /**
     * Guarda un usuario (create o update).
     *
     * FLUJO:
     * Usuario (Domain) → UsuarioEntity → JpaRepository.save() → PostgreSQL
     * PostgreSQL → UsuarioEntity (con id generado) → Usuario (Domain)
     *
     * Si el usuario tiene id null, es un INSERT (JPA genera el id).
     * Si el usuario tiene id, es un UPDATE.
     *
     * @param usuario Usuario del Domain
     * @return Usuario guardado con id asignado
     */
    @Override
    public Usuario save(Usuario usuario){
        // 1. Convertir Domain -> Entity
        UsuarioEntity entity = mapper.toEntity(usuario);

        // 2. Guardar en BD (JPA genera un id si es nuevo)
        UsuarioEntity savedEntity = jpaRepository.save(entity);

        // 3. Convertir Entity -> Domain y retornar
        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca un usuario por su ID interno.
     *
     * NOTA: Este método usa el id interno (BIGINT).
     * Para búsquedas desde APIs, mejor usar findByPublicId() si está disponible.
     *
     * @param id ID interno del usuario
     * @return Optional con el usuario si existe
     */
    @Override
    public Optional<Usuario> findById(Long id){
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Busca un usuario por su email.
     * Usado para login y validación de duplicados.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    @Override
    public Optional<Usuario> findByEmail(String email){
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    /**
     * Busca un usuario por su teléfono.
     *
     * @param telefono Teléfono del usuario
     * @return Optional con el usuario si existe
     */
    @Override
    public Optional<Usuario> findByTelefono(String telefono){
        return jpaRepository.findByTelefono(telefono)
                .map(mapper::toDomain);
    }

    // ============ BÚSQUEDAS POR RELACIONES ============

    /**
     * Busca todos los usuarios hijos de un tutor.
     *
     * IMPORTANTE: Este método usa el id interno (tutorId es BIGINT).
     * El Service debe pasar el id interno, no el publicId.
     *
     * @param tutorId ID interno del tutor
     * @return Lista de usuarios hijos
     */
    @Override
    public List<Usuario> findByTutorId(Long tutorId) {
        return jpaRepository.findByTutorId(tutorId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos los usuarios activos.
     *
     * @return Lista de usuarios con activo = true
     */
    @Override
    public List<Usuario> findAllActivos() {
        return jpaRepository.findByActivoTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    //  ============ VALIDACIONES DE EXISTENCIA ============

    /**
     * Verifica si existe un usuario con ese email.
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    /**
     * Verifica si existe un usuario con ese teléfono.
     *
     * @param telefono Teléfono a verificar
     * @return true si existe, false si no
     */
    @Override
    public boolean existsByTelefono(String telefono) {
        return jpaRepository.existsByTelefono(telefono);
    }

    //  ============ ELIMINACIÓN (SOFT DELETE) ============

    /**
     * Elimina un usuario por su ID (soft delete).
     * No borra físicamente de la BD, solo marca activo = false.
     *
     * FLUJO:
     * 1. Buscar usuario por id
     * 2. Marcar activo = false
     * 3. Guardar cambios
     *
     * Si el usuario no existe, no hace nada (operación idempotente).
     *
     * @param id ID interno del usuario a eliminar
     */
    @Override
    public void deleteById(Long id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setActivo(false);
            jpaRepository.save(entity);
        });
    }

    //  ============ MÉTODOS ADICIONALES (NO EN PORT) ============

    /**
     * Busca un usuario por su publicId (UUID).
     * Este método NO está en el Port, pero es útil para APIs públicas.
     *
     * USO RECOMENDADO:
     * En lugar de exponer findById(Long id) en APIs públicas,
     * los Controllers deberían buscar por publicId.
     *
     * @param publicId UUID del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> findByPublicId(String publicId) {
        return jpaRepository.findByPublicId(publicId)
                .map(mapper::toDomain);
    }

    /**
     * Verifica si existe un usuario con ese publicId.
     *
     * @param publicId UUID a verificar
     * @return true si existe, false si no
     */
    public boolean existsByPublicId(String publicId) {
        return jpaRepository.existsByPublicId(publicId);
    }
}
