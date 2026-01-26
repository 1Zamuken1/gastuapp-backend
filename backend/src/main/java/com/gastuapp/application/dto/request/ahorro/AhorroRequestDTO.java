package com.gastuapp.application.dto.request.ahorro;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para registrar un abono a una meta.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
public class AhorroRequestDTO {

    @NotNull(message = "El ID de la meta es obligatorio")
    private Long metaAhorroId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto del abono debe ser positivo")
    private BigDecimal monto;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    // Fecha es opcional (si no viene, es now())
    // Fecha es opcional (si no viene, es now())
    private LocalDateTime fecha;

    private Long cuotaId; // Opcional, para enlazar con una cuota específica
}
