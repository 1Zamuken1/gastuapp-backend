package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.TransaccionRequestDTO;
import com.gastuapp.application.dto.response.TransaccionResponseDTO;
import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.transaccion.Transaccion;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Application Mapper: TransaccionMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: TransaccionService
 * - CONVIERTE: DTOs (Request/Response) ↔ Transaccion (Domain)
 *
 * RESPONSABILIDAD:
 * Traduce entre objetos de transferencia de datos (DTOs) y modelos de dominio.
 * Enriquece el ResponseDTO con información de la categoría (nombre e ícono).
 * Asigna el usuarioId desde el contexto de autenticación.
 *
 * CONVERSIONES:
 * - toTransaccion(): TransaccionRequestDTO → Transaccion (Domain)
 * - toResponseDTO(): Transaccion → TransaccionResponseDTO (con datos de
 * categoría)
 *
 * DEPENDENCIAS:
 * - CategoriaRepositoryPort: Para obtener nombre e ícono de la categoría
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class TransaccionMapper {

    private final CategoriaRepositoryPort categoriaRepository;

    /**
     * Constructor con inyección de dependencias.
     * CategoriaRepositoryPort se usa para enriquecer el ResponseDTO.
     */
    public TransaccionMapper(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // ==================== REQUEST DTO → DOMAIN ====================

    /**
     * Convierte TransaccionRequestDTO a Transaccion (Domain).
     * Usado cuando el usuario crea o actualiza una transacción.
     *
     * FLUJO:
     * POST/PUT /api/transacciones → TransaccionRequestDTO → [ESTE MÉTODO] →
     * Transaccion → Service
     *
     * NOTA IMPORTANTE:
     * El usuarioId NO viene en el DTO, debe ser asignado por el Service
     * desde el usuario autenticado (JWT).
     *
     * @param dto       TransaccionRequestDTO del cliente
     * @param usuarioId ID del usuario autenticado (obtenido del JWT)
     * @return Transaccion del Domain
     */
    public Transaccion toTransaccion(TransaccionRequestDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        Transaccion transaccion = new Transaccion();

        // Datos del DTO
        transaccion.setMonto(dto.getMonto());
        transaccion.setTipo(dto.getTipo());
        transaccion.setDescripcion(dto.getDescripcion());
        transaccion.setFecha(dto.getFecha());
        transaccion.setCategoriaId(dto.getCategoriaId());
        transaccion.setProyeccionId(dto.getProyeccionId());

        // Usuario autenticado (NO viene en el DTO)
        transaccion.setUsuarioId(usuarioId);

        // Determinar si es automática
        transaccion.setEsAutomatica(dto.getProyeccionId() != null);

        // Inicializar valores por defecto
        transaccion.inicializarValoresPorDefecto();

        return transaccion;
    }

    // ==================== DOMAIN → RESPONSE DTO ====================

    /**
     * Convierte Transaccion (Domain) a TransaccionResponseDTO.
     * Usado para enviar datos al cliente.
     * 
     * FLUJO:
     * Service → Transaccion → [ESTE MÉTODO] → TransaccionResponseDTO → Controller →
     * JSON
     *
     * ENRIQUECIMIENTO:
     * Busca la categoría en BD para incluir nombre e ícono en la respuesta.
     * Esto evita que el frontend tenga que hacer un request adicional.
     *
     * @param transaccion Transaccion del Domain
     * @return TransaccionResponseDTO para el cliente
     */
    public TransaccionResponseDTO toResponseDTO(Transaccion transaccion) {
        if (transaccion == null) {
            return null;
        }

        TransaccionResponseDTO dto = new TransaccionResponseDTO();

        // IDs
        dto.setId(transaccion.getId());
        dto.setUsuarioId(transaccion.getUsuarioId());
        dto.setCategoriaId(transaccion.getCategoriaId());
        dto.setProyeccionId(transaccion.getProyeccionId());

        // Datos de la transacción
        dto.setMonto(transaccion.getMonto());
        dto.setTipo(transaccion.getTipo());
        dto.setDescripcion(transaccion.getDescripcion());
        dto.setFecha(transaccion.getFecha());
        dto.setFechaCreacion(transaccion.getFechaCreacion());
        dto.setEsAutomatica(transaccion.getEsAutomatica());

        // Enriquecer con datos de la categoría
        enriquecerConCategoria(dto, transaccion.getCategoriaId());

        return dto;
    }

    // ==================== ENRIQUECIMIENTO ====================

    /**
     * Enriquece el DTO con información de la categoría.
     * Busca la categoría en BD y agrega nombre e ícono al DTO.
     *
     * VENTAJA:
     * El frontend recibe toda la información en un solo response,
     * sin necesidad de hacer requests adicionales.
     *
     * @param dto         DTO a enriquecer
     * @param categoriaId ID de la categoría
     */
    private void enriquecerConCategoria(TransaccionResponseDTO dto, Long categoriaId) {
        if (categoriaId == null) {
            return;
        }

        categoriaRepository.findById(categoriaId).ifPresent(categoria -> {
            dto.setCategoriaNombre(categoria.getNombre());
            dto.setCategoriaIcono(categoria.getIcono());
        });

        // Si no se encuentra la categoría, los campos quedan null
        // El frontend debe manejar este caso (categoría eliminada)
    }
}