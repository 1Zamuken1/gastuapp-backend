package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.response.CategoriaResponseDTO;
import com.gastuapp.domain.model.categoria.Categoria;
import org.springframework.stereotype.Component;

/**
 * Application Mapper: CategoriaMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: CategoriaService
 * - CONVIERTE: Categoria (Domain) → CategoriaResponseDTO
 *
 * RESPONSABILIDAD:
 * Traduce entre modelo de dominio y DTOs de respuesta.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class CategoriaMapper {
    /**
     * Convierte Categoria (Domain) a CategoriaResponseDTO.
     */
    public CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setIcono(categoria.getIcono());
        dto.setTipo(categoria.getTipo().name());
        dto.setPredefinida(categoria.getPredefinida());

        return dto;
    }
}
