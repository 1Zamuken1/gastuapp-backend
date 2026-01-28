package com.gastuapp.infrastructure.adapter.persistence;

import com.gastuapp.domain.model.planificacion.EstadoPresupuesto;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.domain.port.planificacion.PresupuestoRepositoryPort;
import com.gastuapp.infrastructure.adapter.persistence.entity.PresupuestoEntity;
import com.gastuapp.infrastructure.adapter.persistence.mapper.PresupuestoEntityMapper;
import com.gastuapp.infrastructure.adapter.persistence.repository.PresupuestoJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Adapter: PresupuestoRepositoryAdapter
 *
 * FLUJO DE DATOS:
 * - USADO POR: PresupuestoService (Application Layer)
 * - IMPLEMENTA: PresupuestoRepositoryPort (Domain Port)
 * - USA: PresupuestoJpaRepository (Infrastructure)
 * - USA: PresupuestoEntityMapper (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Implementa el contrato definido en el Domain Port.
 * Convierte entre Domain y Entity usando el mapper.
 * Orquesta las operaciones de persistencia con Spring Data JPA.
 *
 * PATRÓN ADAPTER:
 * - ADAPTA: JpaRepository (Spring) → PresupuestoRepositoryPort (Domain)
 * - PERMITE: Domain layer trabajar sin conocer Spring Data JPA
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Repository
public class PresupuestoRepositoryAdapter implements PresupuestoRepositoryPort {

    private final PresupuestoJpaRepository jpaRepository;
    private final PresupuestoEntityMapper entityMapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente el repository y el mapper.
     */
    public PresupuestoRepositoryAdapter(
            PresupuestoJpaRepository jpaRepository,
            PresupuestoEntityMapper entityMapper) {
        this.jpaRepository = jpaRepository;
        this.entityMapper = entityMapper;
    }

    // ==================== MÉTODOS CRUD BÁSICOS ====================

    @Override
    public Presupuesto save(Presupuesto presupuesto) {
        if (presupuesto == null) {
            throw new IllegalArgumentException("El presupuesto no puede ser nulo");
        }

        // Generar publicId si no existe
        if (presupuesto.getPublicId() == null || presupuesto.getPublicId().trim().isEmpty()) {
            presupuesto.setPublicId(UUID.randomUUID().toString());
        }

        PresupuestoEntity entity = entityMapper.toEntity(presupuesto);
        PresupuestoEntity savedEntity = jpaRepository.save(entity);
        return entityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Presupuesto> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    public Optional<Presupuesto> findByPublicId(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return Optional.empty();
        }

        PresupuestoEntity entity = jpaRepository.findByPublicId(publicId);
        return entity != null ? Optional.of(entityMapper.toDomain(entity)) : Optional.empty();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        jpaRepository.deleteById(id);
    }

    // ==================== MÉTODOS DE BÚSQUEDA POR USUARIO ====================

    @Override
    public List<Presupuesto> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findByUsuarioId(usuarioId);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findByUsuarioIdAndEstado(Long usuarioId, EstadoPresupuesto estado) {
        if (usuarioId == null || estado == null) {
            return List.of();
        }

        PresupuestoEntity.EstadoPresupuestoEnum estadoEntity = mapEstadoToEntity(estado);
        List<PresupuestoEntity> entities = jpaRepository.findByUsuarioIdAndEstado(usuarioId, estadoEntity);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndEstado(
            Long usuarioId, Long categoriaId, EstadoPresupuesto estado) {

        if (usuarioId == null || categoriaId == null || estado == null) {
            return Optional.empty();
        }

        PresupuestoEntity.EstadoPresupuestoEnum estadoEntity = mapEstadoToEntity(estado);
        PresupuestoEntity entity = jpaRepository.findByUsuarioIdAndCategoriaIdAndEstado(
                usuarioId, categoriaId, estadoEntity);

        return entity != null ? Optional.of(entityMapper.toDomain(entity)) : Optional.empty();
    }

    @Override
    public List<Presupuesto> findVigentesByUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findVigentesByUsuarioId(usuarioId);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findCercanosAVencer(Long usuarioId, LocalDate fecha) {
        if (usuarioId == null || fecha == null) {
            return List.of();
        }

        LocalDate fechaPlus7Days = fecha.plusDays(7);
        List<PresupuestoEntity> entities = jpaRepository.findCercanosAVencer(usuarioId, fecha, fechaPlus7Days);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findExcedidosByUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findExcedidosByUsuarioId(usuarioId);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findPendientesDeRenovacion(LocalDate fecha) {
        if (fecha == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findPendientesDeRenovacion(fecha);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findByUsuarioIdAndCategoriaIdAndFechas(
            Long usuarioId, Long categoriaId, LocalDate fechaInicio, LocalDate fechaFin) {

        if (usuarioId == null || categoriaId == null || fechaInicio == null || fechaFin == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findByUsuarioIdAndCategoriaIdAndFechas(
                usuarioId, categoriaId, fechaInicio, fechaFin);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findPorExceder(Long usuarioId, double porcentajeUmbral) {
        if (usuarioId == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findPorExceder(usuarioId, porcentajeUmbral);
        return entityMapper.toDomainList(entities);
    }

    @Override
    public List<Presupuesto> findByUsuarioIdAndFrecuencia(Long usuarioId, String frecuencia) {
        if (usuarioId == null || frecuencia == null) {
            return List.of();
        }

        try {
            PresupuestoEntity.FrecuenciaPresupuestoEnum frecuenciaEnum = PresupuestoEntity.FrecuenciaPresupuestoEnum
                    .valueOf(frecuencia.toUpperCase());
            List<PresupuestoEntity> entities = jpaRepository.findByUsuarioIdAndFrecuencia(usuarioId, frecuenciaEnum);
            return entityMapper.toDomainList(entities);
        } catch (IllegalArgumentException e) {
            // Frecuencia no válida
            return List.of();
        }
    }

    // ==================== MÉTODOS DE CONTEO ====================

    @Override
    public long countByUsuarioIdAndEstado(Long usuarioId, EstadoPresupuesto estado) {
        if (usuarioId == null || estado == null) {
            return 0;
        }

        PresupuestoEntity.EstadoPresupuestoEnum estadoEntity = mapEstadoToEntity(estado);
        return jpaRepository.countByUsuarioIdAndEstado(usuarioId, estadoEntity);
    }

    @Override
    public boolean existsByUsuarioIdAndCategoriaIdAndEstado(
            Long usuarioId, Long categoriaId, EstadoPresupuesto estado) {

        if (usuarioId == null || categoriaId == null || estado == null) {
            return false;
        }

        PresupuestoEntity.EstadoPresupuestoEnum estadoEntity = mapEstadoToEntity(estado);
        return jpaRepository.existsByUsuarioIdAndCategoriaIdAndEstado(usuarioId, categoriaId, estadoEntity);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte EstadoPresupuesto (Domain) a EstadoPresupuestoEnum (Entity).
     */
    private PresupuestoEntity.EstadoPresupuestoEnum mapEstadoToEntity(EstadoPresupuesto estado) {
        if (estado == null) {
            return null;
        }

        return switch (estado) {
            case ACTIVA -> PresupuestoEntity.EstadoPresupuestoEnum.ACTIVA;
            case INACTIVA -> PresupuestoEntity.EstadoPresupuestoEnum.INACTIVA;
            case EXCEDIDA -> PresupuestoEntity.EstadoPresupuestoEnum.EXCEDIDA;
        };
    }

    // ==================== MÉTODOS DE BATCH (FUTURO) ====================

    /**
     * Guarda múltiples presupuestos en batch.
     * Útil para operaciones de sincronización o renovación automática.
     *
     * @param presupuestos Lista de presupuestos a guardar
     * @return Lista de presupuestos guardados
     */
    public List<Presupuesto> saveAll(List<Presupuesto> presupuestos) {
        if (presupuestos == null || presupuestos.isEmpty()) {
            return List.of();
        }

        // Generar publicIds si no existen
        presupuestos.forEach(p -> {
            if (p.getPublicId() == null || p.getPublicId().trim().isEmpty()) {
                p.setPublicId(UUID.randomUUID().toString());
            }
        });

        List<PresupuestoEntity> entities = entityMapper.toEntityList(presupuestos);
        List<PresupuestoEntity> savedEntities = jpaRepository.saveAll(entities);
        return entityMapper.toDomainList(savedEntities);
    }

    /**
     * Elimina presupuestos por usuario y estado.
     * Útil para limpieza de datos o desactivación masiva.
     *
     * @param usuarioId ID del usuario
     * @param estado    Estado a eliminar
     * @return Cantidad de registros eliminados
     */
    public long deleteByUsuarioIdAndEstado(Long usuarioId, EstadoPresupuesto estado) {
        if (usuarioId == null || estado == null) {
            return 0;
        }

        // Primero buscar los registros a eliminar
        List<PresupuestoEntity> entitiesToDelete = jpaRepository.findByUsuarioIdAndEstado(
                usuarioId, mapEstadoToEntity(estado));

        // Eliminar
        jpaRepository.deleteAll(entitiesToDelete);

        return entitiesToDelete.size();
    }

    // ==================== MÉTODOS ADICIONALES PARA SCHEDULER ====================

    @Override
    public long countByEstado(EstadoPresupuesto estado) {
        if (estado == null) {
            return 0;
        }

        PresupuestoEntity.EstadoPresupuestoEnum estadoEntity = mapEstadoToEntity(estado);
        return jpaRepository.countByEstado(estadoEntity);
    }

    @Override
    public List<Presupuesto> findByFechaFinBefore(LocalDate fecha) {
        if (fecha == null) {
            return List.of();
        }

        List<PresupuestoEntity> entities = jpaRepository.findByFechaFinBefore(fecha);
        return entityMapper.toDomainList(entities);
    }
}