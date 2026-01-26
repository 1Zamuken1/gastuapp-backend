package com.gastuapp.domain.model.ahorro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de Dominio que representa un movimiento de ahorro (Abono o Retiro).
 * 
 * <p>
 * Cada registro en esta entidad afecta directamente el <code>montoActual</code>
 * de una {@link MetaAhorro}.
 * Funciona de manera similar a una transacción, pero vinculada específicamente
 * a una meta.
 * </p>
 * 
 * <p>
 * Reglas de negocio:
 * </p>
 * <ul>
 * <li>El monto debe ser positivo.</li>
 * <li>No se pueden hacer abonos a metas CANCELADAS o COMPLETADAS (salvo
 * excepciones de negocio).</li>
 * </ul>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ahorro {
    private Long id;
    private Long metaAhorroId; // Referencia a la meta (Dominio no usa @ManyToOne)
    private Long usuarioId;
    private BigDecimal monto;
    private String descripcion; // Ej: "Abono prima junio"
    private LocalDateTime fecha;
}
