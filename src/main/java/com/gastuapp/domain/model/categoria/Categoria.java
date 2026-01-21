package com.gastuapp.domain.model.categoria;

import com.gastuapp.domain.model.transaccion.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Model: Categoria
 * <p>
 * FLUJO DE DATOS:
 * - CREADO POR: CategoriaService (Application Layer)
 * - USADO POR: Transaccion (relación FK)
 * - CONVERTIDO A: CategoriaEntity (Infrastructure Layer)
 * <p>
 * RESPONSABILIDAD:
 * Modelo de dominio puro que representa una categoría de transacciones.
 * Categorías predefinidas vienen con el sistema.
 * Usuarios pueden crear categorías personalizadas (FUTURO - PREMIUM).
 * <p>
 * TIPOS:
 * - INGRESO: Solo para ingresos (Salario, Freelance)
 * - EGRESO: Solo para egresos (Comida, Transporte)
 * - AMBOS: Para ingresos y egresos
 * <p>
 * CATEGORÍAS PREDEFINIDAS (15):
 * Egresos: Comida, Transporte, Salud, Entretenimiento, Educación,
 * Hogar, Ropa, Servicios, Otros gastos
 * Ingresos: Salario, Freelance, Inversiones, Regalos, Mesada, Otros ingresos
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    // ============ Atributos ============
    private Long id;
    private String nombre;
    private String icono;
    private TipoCategoria tipo;
    private Boolean predefinida;
    private Long usuarioId;

    // ============ Lógica de negocio ============

    /**
     * Valida que la categoría cumpla con las reglas de negocio.
     * <p>
     * VALIDACIONES:
     * - Nombre obligatorio (2-50 caracteres)
     * - Tipo obligatorio
     * - Predefinida no puede tener usuarioId
     * - Custom debe tener usuarioId
     *
     * @throws IllegalArgumentException si validación falla
     */
    public void validar() {
        validarNombre();
        validarTipo();
        validarPredefinida();
    }

    private void validarNombre() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombre.length() < 2 || nombre.length() > 50) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 50 caracteres");
        }
    }

    private void validarTipo() {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
    }

    private void validarPredefinida() {
        if (predefinida && usuarioId != null) {
            throw new IllegalArgumentException("La categoría predefinida no puede tener usuarioId");
        }

        if (!predefinida && usuarioId == null) {
            throw new IllegalArgumentException("La categoría custom debe tener usuarioId");
        }
    }

    /**
     * Verifica si la categoría es predefinida del sistema.
     */
    public boolean esPredefinida() {
        return predefinida != null && predefinida;
    }

    /**
     * Verifica si la categoría puede usarse para un tipo de transacción.
     */
    public boolean permiteTransaccion(TipoTransaccion tipoTransaccion) {
        if (tipo == TipoCategoria.AMBOS) {
            return true;
        }

        return (tipo == TipoCategoria.INGRESO && tipoTransaccion == TipoTransaccion.INGRESO) || (tipo == TipoCategoria.EGRESO && tipoTransaccion == TipoTransaccion.EGRESO);
    }

}
