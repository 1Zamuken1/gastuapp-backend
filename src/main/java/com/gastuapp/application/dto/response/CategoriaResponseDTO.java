package com.gastuapp.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response: CategoriaResponseDTO
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: CategoriaService (Application Layer)
 * - ENVÍA DATOS A: Controller → Cliente (JSON)
 *
 * RESPONSABILIDAD:
 * Representa una categoría en las respuestas HTTP.
 *
 * EJEMPLO JSON:
 * {
 * "id": 1,
 * "nombre": "Comida",
 * "icono": "🍔",
 * "tipo": "EGRESO",
 * "predefinida": true
 * }
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String icono;
    private String tipo;
    private Boolean predefinida;
}
