package com.gastuapp.infrastructure.config;

import com.gastuapp.domain.model.categoria.Categoria;
import com.gastuapp.domain.model.categoria.TipoCategoria;
import com.gastuapp.domain.port.categoria.CategoriaRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Seeder: DataSeeder
 *
 * RESPONSABILIDAD:
 * Ejecuta datos iniciales al arrancar la aplicación.
 * Crea las 15 categorías predefinidas si no existen.
 *
 * EJECUCIÓN:
 * Se ejecuta automáticamente al iniciar Spring Boot (CommandLineRunner).
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoriaRepositoryPort categoriaRepository;

    public DataSeeder(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("🌱 Iniciando seed de datos...");
        seedCategorias();
        logger.info("✅ Seed completado");
    }

    /**
     * Crea las 15 categorías predefinidas si no existen.
     */
    private void seedCategorias() {
        List<Categoria> categorias = crearCategoriasPredefinidas();

        for (Categoria categoria : categorias) {
            if (!categoriaRepository.existsByNombreAndPredefinidaTrue(categoria.getNombre())) {
                categoriaRepository.save(categoria);
                logger.info("✅ Categoría creada: {}", categoria.getNombre());
            }
        }
    }

    /**
     * Define las 15 categorías predefinidas del sistema.
     *
     * CATEGORÍAS EGRESOS (9):
     * - Comida y bebidas
     * - Transporte
     * - Salud
     * - Entretenimiento
     * - Educación
     * - Hogar
     * - Ropa y accesorios
     * - Servicios
     * - Otros gastos
     *
     * CATEGORÍAS INGRESOS (6):
     * - Salario
     * - Freelance
     * - Inversiones
     * - Regalos
     * - Mesada
     * - Otros ingresos
     */
    private List<Categoria> crearCategoriasPredefinidas() {
        List<Categoria> categorias = new ArrayList<>();

        // ==================== EGRESOS ====================
        categorias.add(crearCategoria("Comida y bebidas", "🍔", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Transporte", "🚗", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Salud", "💊", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Entretenimiento", "🎮", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Educación", "📚", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Hogar", "🏠", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Ropa y accesorios", "👕", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Servicios", "💡", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Otros gastos", "📦", TipoCategoria.EGRESO));

        // ==================== INGRESOS ====================
        categorias.add(crearCategoria("Salario", "💰", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Freelance", "💼", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Inversiones", "📈", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Regalos", "🎁", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Mesada", "🪙", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Otros ingresos", "💸", TipoCategoria.INGRESO));

        return categorias;
    }

    /**
     * Helper para crear una categoría predefinida.
     */
    private Categoria crearCategoria(String nombre, String icono, TipoCategoria tipo) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setIcono(icono);
        categoria.setTipo(tipo);
        categoria.setPredefinida(true);
        categoria.setUsuarioId(null);
        return categoria;
    }
}