package com.gastuapp.application.dto.request.ahorro;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la creación/actualización de una Meta de Ahorro.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
public class MetaAhorroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "El monto objetivo es obligatorio")
    @Positive(message = "El monto objetivo debe ser positivo")
    private BigDecimal montoObjetivo;

    @Future(message = "La fecha límite debe ser futura")
    private LocalDateTime fechaLimite;

    private LocalDateTime fechaInicio;
    private String frecuencia; // Valorado como Enum FrecuenciaAhorro

    @NotBlank(message = "El color es obligatorio")
    private String color;

    @NotBlank(message = "El icono es obligatorio")
    private String icono;
}
