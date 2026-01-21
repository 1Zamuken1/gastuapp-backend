package com.gastuapp.infrastructure.adapter.persistence.mapper;

import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.categoria.TipoCategoria;
import com.gastuapp.infrastructure.adapter.persistence.entity.CategoriaEntity;
import org.springframework.stereotype.Component;

/**
 * Entity Mapper: CategoriaEntityMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: CategoriaRepositoryAdapter
 * - CONVIERTE: Categoria (Domain) ↔ CategoriaEntity (Infrastructure)
 *
 * RESPONSABILIDAD:
 * Traduce entre el modelo de dominio y la entidad JPA.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class CategoriaEntityMapper {

    // ============ Entity -> Domain ============
    /**
     * Convierte CategoriaEntity (JPA) a Categoria (Domain).
     */
    public Categoria toDomain(CategoriaEntity entity) {
        if (entity == null) {
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setId(entity.getId());
        categoria.setNombre(entity.getNombre());
        categoria.setIcono(entity.getIcono());
        categoria.setTipo(mapTipoToDomain(entity.getTipo()));
        categoria.setPredefinida(entity.getPredefinida());
        categoria.setUsuarioId(entity.getUsuarioId());

        return categoria;
    }

    // ============ Domain -> Entity ============
    /**
     * Convierte Categoria (Domain) a CategoriaEntity (JPA).
     */
    public CategoriaEntity toEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        CategoriaEntity entity = new CategoriaEntity();
        entity.setId(categoria.getId());
        entity.setNombre(categoria.getNombre());
        entity.setIcono(categoria.getIcono());
        entity.setTipo(mapTipoToEntity(categoria.getTipo()));
        entity.setPredefinida(categoria.getPredefinida());
        entity.setUsuarioId(categoria.getUsuarioId());

        return entity;
    }

    // ============ Mapeo de Enums ============
    private TipoCategoria mapTipoToDomain(CategoriaEntity.TipoCategoriaEnum tipoEnum) {
        if (tipoEnum == null) {
            return null;
        }

        return switch (tipoEnum) {
            case INGRESO -> TipoCategoria.INGRESO;
            case EGRESO -> TipoCategoria.EGRESO;
            case AMBOS -> TipoCategoria.AMBOS;
        };
    }

    private CategoriaEntity.TipoCategoriaEnum mapTipoToEntity(TipoCategoria tipo) {
        if (tipo == null) {
            return null;
        }

        return switch (tipo) {
            case INGRESO -> CategoriaEntity.TipoCategoriaEnum.INGRESO;
            case EGRESO -> CategoriaEntity.TipoCategoriaEnum.EGRESO;
            case AMBOS -> CategoriaEntity.TipoCategoriaEnum.AMBOS;
        };
    }
}
