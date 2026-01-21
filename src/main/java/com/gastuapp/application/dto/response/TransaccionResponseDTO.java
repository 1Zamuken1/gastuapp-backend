package com.gastuapp.application.dto.response;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO Response: TransaccionResponseDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: TransaccionService (via TransaccionMapper)
 * - ENVÍA DATOS A: Controller (Java → JSON)
 * - CONVERTIDO DESDE: Transaccion (Domain) via TransaccionMapper
 *
 * RESPONSABILIDAD:
 * Representa los datos que se envían al cliente (Angular/Postman).
 * Incluye información de la categoría (nombre e ícono) para evitar múltiples requests.
 *
 * EJEMPLO JSON DE RESPUESTA:
 * {
 *   "id": 123,
 *   "monto": 45000.50,
 *   "tipo": "EGRESO",
 *   "descripcion": "Compra de mercado en Éxito",
 *   "fecha": "2025-01-21",
 *   "fechaCreacion": "2025-01-21T14:30:00",
 *   "categoriaId": 1,
 *   "categoriaNombre": "Comida y bebidas",
 *   "categoriaIcono": "🍔",
 *   "usuarioId": 5,
 *   "esAutomatica": false
 * }
 *
 * NOTAS:
 * - categoriaNombre y categoriaIcono se incluyen para mejorar UX
 * - usuarioId se incluye (útil para verificaciones en frontend)
 * - proyeccionId solo se incluye si existe (transacciones automáticas)
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionResponseDTO {

    // ==================== IDENTIFICACIÓN ====================

    private Long id;
    private Long usuarioId;

    // ==================== DATOS DE LA TRANSACCIÓN ====================

    private BigDecimal monto;
    private TipoTransaccion tipo;
    private String descripcion;
    private LocalDate fecha;
    private LocalDateTime fechaCreacion;

    // ==================== CATEGORÍA (DENORMALIZADA PARA UX) ====================

    /**
     * ID de la categoría.
     */
    private Long categoriaId;

    /**
     * Nombre de la categoría (denormalizado).
     * Ejemplo: "Comida y bebidas"
     * 
     * VENTAJA: Evita request adicional al backend para obtener el nombre.
     */
    private String categoriaNombre;

    /**
     * Ícono de la categoría (denormalizado).
     * Ejemplo: "🍔"
     * 
     * VENTAJA: Permite mostrar el ícono directamente en la lista.
     */
    private String categoriaIcono;

    // ==================== PROYECCIONES (FUTURO - FASE 3) ====================

    /**
     * ID de la proyección (solo si es transacción automática).
     * null para transacciones manuales.
     */
    private Long proyeccionId;

    /**
     * Indica si la transacción fue creada automáticamente.
     * true: Creada por proyección automática
     * false: Creada manualmente por el usuario
     */
    private Boolean esAutomatica;

    // ==================== CAMPO CALCULADO ====================

    /**
     * Monto con signo según el tipo.
     * INGRESO: +monto
     * EGRESO: -monto
     * 
     * ÚTIL PARA: Cálculos en frontend sin lógica adicional.
     */
    public BigDecimal getMontoConSigno() {
        if (tipo == TipoTransaccion.INGRESO) {
            return monto;
        } else {
            return monto.negate();
        }
    }
}