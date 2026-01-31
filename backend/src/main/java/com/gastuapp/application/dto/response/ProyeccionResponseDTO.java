package com.gastuapp.application.dto.response;

import com.gastuapp.domain.model.proyeccion.Frecuencia;
import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyeccionResponseDTO {
    private Long id;
    private BigDecimal monto;
    private TipoTransaccion tipo;
    private Long categoriaId;
    private Long usuarioId;
    private Frecuencia frecuencia;
    private LocalDate fechaInicio;
    private LocalDate ultimaEjecucion;
    private LocalDate proximoCobro; // Calculado
    private Boolean activo;

    // Enriquecimiento (opcional)
    private String nombreCategoria;
    private String iconoCategoria;
}
