package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.ahorro.MetaAhorroRequestDTO;
import com.gastuapp.application.dto.response.ahorro.MetaAhorroResponseDTO;
import com.gastuapp.domain.model.ahorro.FrecuenciaAhorro;
import com.gastuapp.domain.model.ahorro.MetaAhorro;
import org.springframework.stereotype.Component;

/**
 * Application Mapper: MetaAhorroMapper
 * 
 * <p>
 * Convierte DTOs (Request/Response) ↔ Domain (MetaAhorro).
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class MetaAhorroMapper {

    // ==================== REQUEST DTO → DOMAIN ====================

    public MetaAhorro toDomain(MetaAhorroRequestDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        MetaAhorro meta = new MetaAhorro();
        meta.setUsuarioId(usuarioId);
        meta.setNombre(dto.getNombre());
        meta.setMontoObjetivo(dto.getMontoObjetivo());
        meta.setFechaLimite(dto.getFechaLimite());
        meta.setFechaInicio(dto.getFechaInicio());
        if (dto.getFrecuencia() != null) {
            try {
                meta.setFrecuencia(FrecuenciaAhorro.valueOf(dto.getFrecuencia()));
            } catch (IllegalArgumentException e) {
                // Ignore or handle
            }
        }
        meta.setColor(dto.getColor());
        meta.setIcono(dto.getIcono());

        // Valores por defecto
        meta.setEstado(MetaAhorro.EstadoMeta.ACTIVA);

        return meta;
    }

    // ==================== DOMAIN → RESPONSE DTO ====================

    public MetaAhorroResponseDTO toResponseDTO(MetaAhorro meta) {
        if (meta == null) {
            return null;
        }

        MetaAhorroResponseDTO dto = new MetaAhorroResponseDTO();
        dto.setId(meta.getId());
        dto.setUsuarioId(meta.getUsuarioId());
        dto.setNombre(meta.getNombre());
        dto.setMontoObjetivo(meta.getMontoObjetivo());
        dto.setMontoActual(meta.getMontoActual() != null ? meta.getMontoActual() : java.math.BigDecimal.ZERO);
        dto.setFechaLimite(meta.getFechaLimite());
        dto.setFechaInicio(meta.getFechaInicio());
        dto.setFrecuencia(meta.getFrecuencia() != null ? meta.getFrecuencia().name() : null);
        dto.setColor(meta.getColor());
        dto.setIcono(meta.getIcono());
        dto.setEstado(meta.getEstado() != null ? meta.getEstado().name() : null);
        dto.setFechaCreacion(meta.getFechaCreacion());

        // Campos calculados
        dto.setPorcentajeProgreso(meta.calcularProgreso());

        return dto;
    }
}
