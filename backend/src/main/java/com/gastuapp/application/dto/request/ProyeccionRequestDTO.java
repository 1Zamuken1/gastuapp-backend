package com.gastuapp.application.dto.request;

import com.gastuapp.domain.model.proyeccion.Frecuencia;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProyeccionRequestDTO {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El tipo es obligatorio")
    private TipoTransaccion tipo;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotNull(message = "La frecuencia es obligatoria")
    private Frecuencia frecuencia;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;
}
