package com.gastuapp.application.dto.response.ahorro;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una Meta de Ahorro.
 * Incluye campos calculados como el porcentaje de progreso.
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-25
 */
@Data
public class MetaAhorroResponseDTO {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private BigDecimal montoObjetivo;
    private BigDecimal montoActual;
    private Double porcentajeProgreso; // Calculado
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaInicio;
    private String frecuencia;
    private String color;
    private String icono;
    private String estado;
    private LocalDateTime fechaCreacion;
}
