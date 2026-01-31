package com.gastuapp.application.service;

import com.gastuapp.application.dto.request.ProyeccionRequestDTO;
import com.gastuapp.application.dto.request.TransaccionRequestDTO;
import com.gastuapp.application.dto.response.ProyeccionResponseDTO;
import com.gastuapp.application.dto.response.TransaccionResponseDTO;
import com.gastuapp.application.mapper.ProyeccionMapper;
import com.gastuapp.domain.model.proyeccion.Proyeccion;
import com.gastuapp.domain.port.proyeccion.ProyeccionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service: ProyeccionService
 *
 * FLUJO DE DATOS:
 * - RECIBE: DTOs desde Controller
 * - USA: ProyeccionRepositoryPort, TransaccionService
 *
 * RESPONSABILIDAD:
 * Gestionar ciclo de vida de proyecciones y su ejecución manual.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
@Service
@Transactional
public class ProyeccionService {

    private final ProyeccionRepositoryPort proyeccionRepository;
    private final ProyeccionMapper proyeccionMapper;

    // Inyección circular potencial evitada si TransaccionService no depende de
    // ProyeccionService.
    // Verificamos que no sea así. Si fuera necesario, usar @Lazy.
    private final TransaccionService transaccionService;

    public ProyeccionService(
            ProyeccionRepositoryPort proyeccionRepository,
            ProyeccionMapper proyeccionMapper,
            TransaccionService transaccionService) {
        this.proyeccionRepository = proyeccionRepository;
        this.proyeccionMapper = proyeccionMapper;
        this.transaccionService = transaccionService;
    }

    public ProyeccionResponseDTO crearProyeccion(ProyeccionRequestDTO dto, Long usuarioId) {
        Proyeccion proyeccion = proyeccionMapper.toDomain(dto, usuarioId);
        proyeccion.validar();
        Proyeccion saved = proyeccionRepository.save(proyeccion);
        return proyeccionMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProyeccionResponseDTO> listarProyeccionesActivas(Long usuarioId) {
        return proyeccionRepository.findAllByUsuarioIdAndActivoTrue(usuarioId).stream()
                .map(proyeccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void eliminarProyeccion(Long id, Long usuarioId) {
        Proyeccion proyeccion = proyeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyección no encontrada"));

        if (!proyeccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permisos para eliminar esta proyección");
        }

        proyeccion.desactivar(); // Soft Delete
        proyeccionRepository.save(proyeccion);
    }

    public ProyeccionResponseDTO actualizarProyeccion(Long id, ProyeccionRequestDTO dto, Long usuarioId) {
        Proyeccion proyeccion = proyeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyección no encontrada"));

        if (!proyeccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permisos para actualizar esta proyección");
        }

        // Actualizar campos
        proyeccion.setNombre(dto.getNombre());
        proyeccion.setMonto(dto.getMonto());
        proyeccion.setTipo(dto.getTipo());
        proyeccion.setCategoriaId(dto.getCategoriaId());
        proyeccion.setFrecuencia(dto.getFrecuencia());
        proyeccion.setFechaInicio(dto.getFechaInicio());

        proyeccion.validar();
        Proyeccion saved = proyeccionRepository.save(proyeccion);
        return proyeccionMapper.toResponseDTO(saved);
    }

    /**
     * Ejecuta una proyección manualmente, creando una transacción real.
     * Actualiza la fecha de última ejecución.
     */
    public TransaccionResponseDTO ejecutarProyeccion(Long id, Long usuarioId) {
        // 1. Obtener Proyeccion
        Proyeccion proyeccion = proyeccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyección no encontrada"));

        if (!proyeccion.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permisos para ejecutar esta proyección");
        }

        if (!proyeccion.getActivo()) {
            throw new IllegalArgumentException("No se puede ejecutar una proyección inactiva");
        }

        // 2. Crear DTO para la Transacción
        TransaccionRequestDTO transaccionDTO = new TransaccionRequestDTO();
        transaccionDTO.setMonto(proyeccion.getMonto());
        transaccionDTO.setTipo(proyeccion.getTipo());
        transaccionDTO.setDescripcion("Ejecución de proyección: " + proyeccion.getNombre());
        transaccionDTO.setFecha(LocalDate.now());
        transaccionDTO.setCategoriaId(proyeccion.getCategoriaId());

        // 3. Crear Transacción usando el servicio existente (garantiza validaciones e
        // integridad)
        TransaccionResponseDTO nuevaTransaccion = transaccionService.crearTransaccion(transaccionDTO, usuarioId);

        // 4. Actualizar última ejecución
        proyeccion.setUltimaEjecucion(LocalDate.now());
        proyeccionRepository.save(proyeccion);

        return nuevaTransaccion;
    }
}
