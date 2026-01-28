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
        // Alimentación
        categorias.add(crearCategoria("Alimentación", "pi pi-shopping-cart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Restaurantes", "pi pi-shopping-cart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Mercado", "pi pi-shopping-cart", TipoCategoria.EGRESO));

        // Transporte
        categorias.add(crearCategoria("Transporte Público", "pi pi-car", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Taxi/Uber", "pi pi-car", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Gasolina", "pi pi-car", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Mantenimiento Vehículo", "pi pi-car", TipoCategoria.EGRESO));

        // Vivienda y Servicios
        categorias.add(crearCategoria("Arriendo/Hipoteca", "pi pi-home", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Servicios Públicos", "pi pi-bolt", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Internet/TV", "pi pi-wifi", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Mantenimiento Hogar", "pi pi-home", TipoCategoria.EGRESO));

        // Salud y Bienestar
        categorias.add(crearCategoria("Salud", "pi pi-heart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Medicamentos", "pi pi-heart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Deportes", "pi pi-heart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Cuidado Personal", "pi pi-heart", TipoCategoria.EGRESO));

        // Entretenimiento y Ocio
        categorias.add(crearCategoria("Entretenimiento", "pi pi-ticket", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Cine", "pi pi-ticket", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Viajes", "pi pi-globe", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Suscripciones", "pi pi-play", TipoCategoria.EGRESO));

        // Educación
        categorias.add(crearCategoria("Educación", "pi pi-book", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Cursos", "pi pi-book", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Material Escolar", "pi pi-book", TipoCategoria.EGRESO));

        // Otros
        categorias.add(crearCategoria("Ropa", "pi pi-tag", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Mascotas", "pi pi-heart", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Regalos", "pi pi-gift", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Tecnología", "pi pi-desktop", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Impuestos", "pi pi-percentage", TipoCategoria.EGRESO));
        categorias.add(crearCategoria("Otros Gastos", "pi pi-box", TipoCategoria.EGRESO));

        // ==================== INGRESOS ====================
        categorias.add(crearCategoria("Salario", "pi pi-money-bill", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Freelance", "pi pi-briefcase", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Inversiones", "pi pi-chart-line", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Arriendos", "pi pi-home", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Regalos", "pi pi-gift", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Mesada", "pi pi-wallet", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Devoluciones", "pi pi-replay", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Bonus", "pi pi-star", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Ventas", "pi pi-tag", TipoCategoria.INGRESO));
        categorias.add(crearCategoria("Otros Ingresos", "pi pi-plus", TipoCategoria.INGRESO));

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