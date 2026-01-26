package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.ahorro.AhorroRequestDTO;
import com.gastuapp.application.dto.response.ahorro.AhorroResponseDTO;
import com.gastuapp.application.dto.response.ahorro.CuotaAhorroResponseDTO;
import com.gastuapp.domain.model.ahorro.Ahorro;
import com.gastuapp.domain.model.ahorro.CuotaAhorro;
import org.springframework.stereotype.Component;

/**
 * Application Mapper: AhorroMapper
 * 
 * <p>
 * Convierte DTOs (Request/Response) ↔ Domain (Ahorro).
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Component
public class AhorroMapper {

    // ==================== REQUEST DTO → DOMAIN ====================

    public Ahorro toDomain(AhorroRequestDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        Ahorro ahorro = new Ahorro();
        ahorro.setMetaAhorroId(dto.getMetaAhorroId());
        ahorro.setUsuarioId(usuarioId);
        ahorro.setMonto(dto.getMonto());
        ahorro.setDescripcion(dto.getDescripcion());
        ahorro.setFecha(dto.getFecha());
        // Si fecha es null, el dominio o entidad lo manejarán (PrePersist)

        return ahorro;
    }

    // ==================== DOMAIN → RESPONSE DTO ====================

    public AhorroResponseDTO toResponseDTO(Ahorro ahorro) {
        if (ahorro == null) {
            return null;
        }

        AhorroResponseDTO dto = new AhorroResponseDTO();
        dto.setId(ahorro.getId());
        dto.setMetaAhorroId(ahorro.getMetaAhorroId());
        dto.setUsuarioId(ahorro.getUsuarioId());
        dto.setMonto(ahorro.getMonto());
        dto.setDescripcion(ahorro.getDescripcion());
        dto.setFecha(ahorro.getFecha());

        return dto;
    }

    public CuotaAhorroResponseDTO toCuotaResponseDTO(CuotaAhorro cuota) {
        if (cuota == null)
            return null;

        return CuotaAhorroResponseDTO.builder()
                .id(cuota.getId())
                .numeroCuota(cuota.getNumeroCuota())
                .fechaProgramada(cuota.getFechaProgramada())
                .montoEsperado(cuota.getMontoEsperado())
                .estado(cuota.getEstado() != null ? cuota.getEstado().name() : null)
                .ahorroId(cuota.getAhorroId())
                .build();
    }
}
