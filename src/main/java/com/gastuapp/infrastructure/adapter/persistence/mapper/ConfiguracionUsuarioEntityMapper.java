package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.ConfiguracionUsuario;
import com.gastuapp.domain.model.TipoOnboarding;
import org.springframework.stereotype.Component;
import com.gastuapp.infrastructure.adapter.persistence.entity.ConfiguracionUsuarioEntity;

/**
 * Entity Mapper: ConfiguracionUsuarioEntityMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: ConfiguracionRepositoryAdapter (cuando se implemente)
 * - CONVIERTE: ConfiguracionUsuario (Domain) ↔ ConfiguracionUsuarioEntity (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Traduce entre el modelo de configuración de dominio y la entidad JPA.
 * Mapea enum TipoOnboarding entre capas.
 *
 * CONVERSIONES:
 * - toDomain(): ConfiguracionUsuarioEntity → ConfiguracionUsuario
 * - toEntity(): ConfiguracionUsuario → ConfiguracionUsuarioEntity
 * - updateEntity(): Actualiza entity existente con datos de domain
 *
 * MAPEO DE ENUMS:
 * - TipoOnboarding (Domain) ↔ ConfiguracionUsuarioEntity.TipoOnboardingEnum (Infrastructure)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-18
 */
@Component
public class ConfiguracionUsuarioEntityMapper {
    //  ============ Entity -> Domain   ============
    /**
     * Convierte ConfiguracionUsuarioEntity (JPA) a ConfiguracionUsuario (Domain).
     * Usado cuando se lee de la base de datos.
     *
     * @param entity ConfiguracionUsuarioEntity de la BD
     * @return ConfiguracionUsuario del Domain (null si entity es null)
     */
    public ConfiguracionUsuario toDomain(com.gastuapp.infrastructure.adapter.persistence.entity.ConfiguracionUsuarioEntity entity){
        if (entity == null){
            return null;
        }

        ConfiguracionUsuario config = new ConfiguracionUsuario();

        config.setId(entity.getId());
        config.setUsuarioId(entity.getUsuarioId());
        config.setNotificacionesActivas(entity.getNotificacionesActivas());
        config.setCelebracionesActivas(entity.getCelebracionesActivas());
        config.setOnboardingCompletado(mapTipoOnboardingToDomain(entity.getOnboardingCompletado()));
        config.setIdiomaPreferido(entity.getIdiomaPreferido());
        config.setModoOscuro(entity.getModoOscuro());

        return config;
    }

    //   ============ Domain -> Entity  ============
    /**
     * Convierte ConfiguracionUsuario (Domain) a ConfiguracionUsuarioEntity (JPA).
     * Usado cuando se va a guardar en la base de datos.
     *
     * @param config ConfiguracionUsuario del Domain
     * @return ConfiguracionUsuarioEntity para la BD (null si config es null)
     */
    public ConfiguracionUsuarioEntity toEntity(ConfiguracionUsuario config){
        if (config == null){
            return null;
        }

        ConfiguracionUsuarioEntity entity = new ConfiguracionUsuarioEntity();

        entity.setId(config.getId());
        entity.setUsuarioId(config.getUsuarioId());
        entity.setNotificacionesActivas(config.getNotificacionesActivas());
        entity.setCelebracionesActivas(config.getCelebracionesActivas());
        entity.setOnboardingCompletado(mapTipoOnboardingToEntity(config.getOnboardingCompletado()));
        entity.setIdiomaPreferido(config.getIdiomaPreferido());
        entity.setModoOscuro(config.getModoOscuro());

        return entity;
    }

    //   ============ Update Entity  ============
    /**
     * Actualiza una ConfiguracionUsuarioEntity existente con datos de ConfiguracionUsuario.
     * Usado para operaciones de UPDATE.
     *
     * IMPORTANTE:
     * - NO actualiza: id, usuarioId (inmutables)
     * - SÍ actualiza: todas las preferencias
     *
     * @param entity ConfiguracionUsuarioEntity existente (con id de BD)
     * @param config ConfiguracionUsuario con datos nuevos
     */
    public void updateEntity(ConfiguracionUsuarioEntity entity, ConfiguracionUsuario config){
        if (entity == null || config == null){
            return;
        }

        // Actualizar preferencias
        entity.setNotificacionesActivas(config.getNotificacionesActivas());
        entity.setCelebracionesActivas(config.getCelebracionesActivas());
        entity.setOnboardingCompletado(mapTipoOnboardingToEntity(config.getOnboardingCompletado()));
        entity.setIdiomaPreferido(config.getIdiomaPreferido());
        entity.setModoOscuro(config.getModoOscuro());
    }

    //   ============ Mapeo de Enums  ============
    /**
     * Convierte TipoOnboardingEnum (Entity) a TipoOnboarding (Domain).
     */
    private TipoOnboarding mapTipoOnboardingToDomain(
            ConfiguracionUsuarioEntity.TipoOnboardingEnum onboardingEnum){
        if (onboardingEnum == null){
            return null;
        }

        return switch (onboardingEnum){
            case NO_COMPLETADO -> TipoOnboarding.NO_COMPLETADO;
            case BASICO -> TipoOnboarding.BASICO;
            case COMPLETO -> TipoOnboarding.COMPLETO;
        };
    }

    /**
     * Convierte TipoOnboarding (Domain) a TipoOnboardingEnum (Entity).
     */
    private ConfiguracionUsuarioEntity.TipoOnboardingEnum mapTipoOnboardingToEntity(
            TipoOnboarding onboarding) {
        if (onboarding == null) {
            return null;
        }

        return switch (onboarding) {
            case NO_COMPLETADO -> ConfiguracionUsuarioEntity.TipoOnboardingEnum.NO_COMPLETADO;
            case BASICO -> ConfiguracionUsuarioEntity.TipoOnboardingEnum.BASICO;
            case COMPLETO -> ConfiguracionUsuarioEntity.TipoOnboardingEnum.COMPLETO;
        };
    }
}
