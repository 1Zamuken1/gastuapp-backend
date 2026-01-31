package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.ProyeccionRequestDTO;
import com.gastuapp.application.dto.response.ProyeccionResponseDTO;
import com.gastuapp.domain.model.proyeccion.Proyeccion;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class ProyeccionMapper {

    private final CategoriaRepositoryPort categoriaRepository;

    public ProyeccionMapper(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Proyeccion toDomain(ProyeccionRequestDTO dto, Long usuarioId) {
        if (dto == null)
            return null;

        Proyeccion domain = new Proyeccion();
        domain.setMonto(dto.getMonto());
        domain.setTipo(dto.getTipo());
        domain.setCategoriaId(dto.getCategoriaId());
        domain.setUsuarioId(usuarioId);
        domain.setFrecuencia(dto.getFrecuencia());
        domain.setFechaInicio(dto.getFechaInicio());
        domain.setActivo(true); // Default active

        return domain;
    }

    public ProyeccionResponseDTO toResponseDTO(Proyeccion domain) {
        if (domain == null)
            return null;

        ProyeccionResponseDTO dto = new ProyeccionResponseDTO();
        dto.setId(domain.getId());
        dto.setMonto(domain.getMonto());
        dto.setTipo(domain.getTipo());
        dto.setCategoriaId(domain.getCategoriaId());
        dto.setUsuarioId(domain.getUsuarioId());
        dto.setFrecuencia(domain.getFrecuencia());
        dto.setFechaInicio(domain.getFechaInicio());
        dto.setUltimaEjecucion(domain.getUltimaEjecucion());
        dto.setActivo(domain.getActivo());

        // Calculated field
        dto.setProximoCobro(domain.calcularProximoCobro());

        // Enriquecimiento con datos de categoría (para mostrar iconito bonito)
        categoriaRepository.findById(domain.getCategoriaId()).ifPresent(cat -> {
            dto.setNombreCategoria(cat.getNombre());
            dto.setIconoCategoria(cat.getIcono());
        });

        return dto;
    }
}
