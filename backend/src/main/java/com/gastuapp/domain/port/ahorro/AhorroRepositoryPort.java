package com.gastuapp.domain.port.ahorro;

import com.gastuapp.domain.model.ahorro.Ahorro;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Movimientos de Ahorro (Abonos).
 * 
 * <p>
 * Gestiona la persistencia de los abonos realizados a las metas.
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
public interface AhorroRepositoryPort {

    Ahorro save(Ahorro ahorro);

    Optional<Ahorro> findById(Long id);

    List<Ahorro> findAllByMetaAhorroId(Long metaAhorroId);

    void deleteById(Long id);

    /**
     * Elimina todos los abonos asociados a una meta.
     * Útil cuando se elimina una meta completa.
     */
    void deleteAllByMetaAhorroId(Long metaAhorroId);
}
