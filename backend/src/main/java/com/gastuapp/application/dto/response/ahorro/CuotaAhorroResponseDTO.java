package com.gastuapp.application.dto.response.ahorro;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CuotaAhorroResponseDTO {
    private Long id;
    private Integer numeroCuota;
    private LocalDate fechaProgramada;
    private BigDecimal montoEsperado;
    private String estado;
    private Long ahorroId;
}
