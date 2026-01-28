package com.gastuapp.application.mapper;

import com.gastuapp.application.dto.request.ActualizarPresupuestoRequestDTO;
import com.gastuapp.application.dto.request.CrearPresupuestoRequestDTO;
import com.gastuapp.application.dto.response.PresupuestoResponseDTO;
import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.planificacion.Presupuesto;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Application Mapper: PresupuestoMapper
 *
 * FLUJO DE DATOS:
 * - USADO POR: PresupuestoService
 * - CONVIERTE: DTOs (Request/Response) ↔ Presupuesto (Domain)
 *
 * RESPONSABILIDAD:
 * Traduce entre objetos de transferencia de datos (DTOs) y modelos de dominio.
 * Enriquece el ResponseDTO con información de la categoría (nombre e ícono).
 * Asigna el usuarioId desde el contexto de autenticación.
 *
 * CONVERSIONES:
 * - toPresupuesto(): CrearPresupuestoRequestDTO → Presupuesto (Domain)
 * - actualizarPresupuesto(): ActualizarPresupuestoRequestDTO → Presupuesto
 * existente
 * - toResponseDTO(): Presupuesto → PresupuestoResponseDTO (con datos de
 * categoría)
 *
 * DEPENDENCIAS:
 * - CategoriaRepositoryPort: Para obtener nombre e ícono de la categoría
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-27
 */
@Component
public class PresupuestoMapper {

    private final CategoriaRepositoryPort categoriaRepository;

    /**
     * Constructor con inyección de dependencias.
     * CategoriaRepositoryPort se usa para enriquecer el ResponseDTO.
     */
    public PresupuestoMapper(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // ==================== REQUEST DTO → DOMAIN ====================

    /**
     * Convierte CrearPresupuestoRequestDTO a Presupuesto (Domain).
     * Usado cuando el usuario crea una nueva planificación de presupuesto.
     *
     * FLUJO:
     * POST /api/presupuestos-planificaciones → CrearPresupuestoRequestDTO → [ESTE
     * MÉTODO] →
     * Presupuesto → Service
     *
     * NOTA IMPORTANTE:
     * El usuarioId NO viene en el DTO, debe ser asignado por el Service
     * desde el usuario autenticado (JWT).
     *
     * @param dto       CrearPresupuestoRequestDTO del cliente
     * @param usuarioId ID del usuario autenticado (obtenido del JWT)
     * @return Presupuesto del Domain
     */
    public Presupuesto toPresupuesto(CrearPresupuestoRequestDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        Presupuesto presupuesto = new Presupuesto();

        // Datos del DTO
        presupuesto.setMontoTope(dto.getMontoTope() != null ? BigDecimal.valueOf(dto.getMontoTope()) : null);
        presupuesto.setFechaInicio(dto.getFechaInicio());
        presupuesto.setFechaFin(dto.getFechaFin());
        presupuesto.setFrecuencia(dto.getFrecuencia());
        presupuesto.setAutoRenovar(dto.getAutoRenovar());
        presupuesto.setCategoriaId(dto.getCategoriaId());

        // Usuario autenticado (NO viene en el DTO)
        presupuesto.setUsuarioId(usuarioId);

        // Inicializar valores por defecto
        presupuesto.inicializarValoresPorDefecto();

        return presupuesto;
    }

    /**
     * Actualiza un Presupuesto existente con los datos del
     * ActualizarPresupuestoRequestDTO.
     * Usado para actualizaciones parciales de una planificación.
     *
     * @param presupuestoExistente Presupuesto existente a actualizar
     * @param dto                  DTO con los datos a actualizar
     */
    public void actualizarPresupuesto(Presupuesto presupuestoExistente, ActualizarPresupuestoRequestDTO dto) {
        if (presupuestoExistente == null || dto == null) {
            return;
        }

        // Solo actualizar los campos proporcionados
        if (dto.getMontoTope() != null) {
            presupuestoExistente.setMontoTope(BigDecimal.valueOf(dto.getMontoTope()));
        }
        if (dto.getFechaInicio() != null) {
            presupuestoExistente.setFechaInicio(dto.getFechaInicio());
        }
        if (dto.getFechaFin() != null) {
            presupuestoExistente.setFechaFin(dto.getFechaFin());
        }
        if (dto.getFrecuencia() != null) {
            presupuestoExistente.setFrecuencia(dto.getFrecuencia());
        }
        if (dto.getAutoRenovar() != null) {
            presupuestoExistente.setAutoRenovar(dto.getAutoRenovar());
        }
    }

    // ==================== DOMAIN → RESPONSE DTO ====================

    /**
     * Convierte Presupuesto (Domain) a PresupuestoResponseDTO.
     * Usado para enviar datos al cliente.
     * 
     * FLUJO:
     * Service → Presupuesto → [ESTE MÉTODO] → PresupuestoResponseDTO → Controller →
     * JSON
     *
     * ENRIQUECIMIENTO:
     * Busca la categoría en BD para incluir nombre e ícono en la respuesta.
     * Calcula campos adicionales para mejorar la UX.
     *
     * @param presupuesto Presupuesto del Domain
     * @return PresupuestoResponseDTO para el cliente
     */
    public PresupuestoResponseDTO toResponseDTO(Presupuesto presupuesto) {
        if (presupuesto == null) {
            return null;
        }

        PresupuestoResponseDTO dto = new PresupuestoResponseDTO();

        // IDs
        dto.setId(presupuesto.getId());
        dto.setPublicId(presupuesto.getPublicId());
        dto.setUsuarioId(presupuesto.getUsuarioId());

        // Datos del presupuesto
        dto.setMontoTope(presupuesto.getMontoTope());
        dto.setMontoGastado(presupuesto.getMontoGastado());
        dto.setFechaInicio(presupuesto.getFechaInicio());
        dto.setFechaFin(presupuesto.getFechaFin());
        dto.setFrecuencia(presupuesto.getFrecuencia());
        dto.setEstado(presupuesto.getEstado());
        dto.setAutoRenovar(presupuesto.getAutoRenovar());
        dto.setFechaCreacion(presupuesto.getFechaCreacion());
        dto.setCategoriaId(presupuesto.getCategoriaId());

        // Enriquecer con datos de la categoría
        enriquecerConCategoria(dto, presupuesto.getCategoriaId());

        // Calcular campos adicionales
        dto.setMontoRestante(dto.calcularMontoRestante());
        dto.setPorcentajeUtilizacion(dto.calcularPorcentajeUtilizacion());
        dto.setEstaVigente(dto.calcularEstaVigente());
        dto.setEstaExcedido(dto.calcularEstaExcedido());
        dto.setDiasRestantes(dto.calcularDiasRestantes());

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
    private void enriquecerConCategoria(PresupuestoResponseDTO dto, Long categoriaId) {
        if (categoriaId == null) {
            return;
        }

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
        categoriaOpt.ifPresent(categoria -> {
            dto.setCategoriaNombre(categoria.getNombre());
            dto.setCategoriaIcono(categoria.getIcono());
        });

        // Si no se encuentra la categoría, los campos quedan null
        // El frontend debe manejar este caso (categoría eliminada)
    }

    // ==================== VALIDACIONES AUXILIARES ====================

    /**
     * Valida que la categoría sea de tipo EGRESO.
     * Los presupuestos solo aplican a categorías de egresos.
     *
     * @param categoriaId ID de la categoría a validar
     * @throws IllegalArgumentException si la categoría no es de tipo EGRESO
     */
    public void validarCategoriaEgreso(Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
        if (categoriaOpt.isEmpty()) {
            throw new IllegalArgumentException("La categoría especificada no existe");
        }

        Categoria categoria = categoriaOpt.get();
        if (!"EGRESO".equals(categoria.getTipo().name())) {
            throw new IllegalArgumentException("Los presupuestos solo pueden crearse para categorías de tipo EGRESO");
        }
    }
}