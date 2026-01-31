package com.gastuapp.domain.port.proyeccion;

import com.gastuapp.domain.model.proyeccion.Proyeccion;
import java.util.List;
import java.util.Optional;

/**
 * Domain Port: ProyeccionRepositoryPort
 *
 * RESPONSABILIDAD:
 * Define el contrato para la persistencia de proyecciones.
 * Implementado por ProyeccionRepositoryAdapter (Infrastructure).
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-30
 */
public interface ProyeccionRepositoryPort {

    Proyeccion save(Proyeccion proyeccion);

    Optional<Proyeccion> findById(Long id);

    List<Proyeccion> findAllByUsuarioIdAndActivoTrue(Long usuarioId);

    void deleteById(Long id); // Físico o lógico, depende de implementacion
}
