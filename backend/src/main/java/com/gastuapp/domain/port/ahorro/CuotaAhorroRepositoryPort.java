package com.gastuapp.domain.port.ahorro;

import com.gastuapp.domain.model.ahorro.CuotaAhorro;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Cuotas de Ahorro.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
public interface CuotaAhorroRepositoryPort {

    CuotaAhorro save(CuotaAhorro cuota);

    List<CuotaAhorro> saveAll(List<CuotaAhorro> cuotas);

    Optional<CuotaAhorro> findById(Long id);

    List<CuotaAhorro> findAllByMetaAhorroId(Long metaAhorroId);

    void deleteById(Long id);

    void deleteAllByMetaAhorroId(Long metaAhorroId);
}
