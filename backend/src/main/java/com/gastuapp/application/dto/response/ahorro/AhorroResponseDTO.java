package com.gastuapp.application.dto.response.ahorro;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un abono.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
public class AhorroResponseDTO {
    private Long id;
    private Long metaAhorroId;
    private Long usuarioId;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fecha;
}
