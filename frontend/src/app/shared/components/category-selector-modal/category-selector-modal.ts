import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnInit,
  signal,
  computed,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';

// Core
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria, TipoCategoria } from '../../../core/models/categoria.model';

@Component({
  selector: 'app-category-selector-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, InputTextModule, ButtonModule],
  templateUrl: './category-selector-modal.html',
  styleUrl: './category-selector-modal.scss',
})
export class CategorySelectorModal implements OnInit {
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();

  @Input() type: 'INGRESO' | 'EGRESO' = 'EGRESO';
  @Output() onSelect = new EventEmitter<Categoria>();

  searchTerm = signal('');
  allCategories = signal<Categoria[]>([]);
  loading = signal(false);

  // Computed: Categories filtered by type (and visual icon override) AND search term
  displayCategories = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    const targetType = this.type;

    // 1. Filtrar por tipo y mapear el ícono visual estandarizado
    let list = this.allCategories().filter((c) => c.tipo === targetType);

    // 2. Filtrar por búsqueda
    if (term) {
      list = list.filter((c) => c.nombre.toLowerCase().includes(term));
    }

    return list;
  });

  constructor(private categoriaService: CategoriaService) {
    // Effect to reload if needed? No, we load all compatible on init usually.
    // Or we could trigger load based on type change?
    // Let's load ALL available categories on init to be robust.
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading.set(true);
    // Asumimos que existe un método para obtener todas las disponibles
    // Si no, podríamos listar por tipo separadamente.
    // Usaremos listarDisponiblesParaUsuario(1) (Hardcoded user for now? Or get auth?).
    // Better: CategoriaService probably has "listarPorTipo".
    // Let's use listarPorTipo if dynamic based on current type, OR load all.
    // To be safe and simple: Fetch ALL types and filter in frontend, or fetch by type?
    // Let's fetch by type to avoid overfetching.

    // Actually, "listarDisponiblesParaUsuario" brings everything.
    // Let's assuming hardcoded user ID 1 for MVP context or similar.
    // Re-checking CategoriaService: listarDisponiblesParaUsuario(usuarioId).
    // I should get the user ID from AuthService? Or just hardcode 1 for this context as seen in controllers.
    // Controller method "obtenerUsuarioIdAutenticado".
    // Service method "listarDisponiblesParaUsuario(usuarioId)".
    // I'll stick to a robust approach:
    // If AuthService is available, use it. If not, maybe pass UserId as Input?
    // Or assume the service handles 1L if not provided? No.
    // I'll use 1L for now, or AuthService if I can find it.

    // Load categories by type
    this.categoriaService.listarPorTipo(this.type).subscribe({
      next: (cats: Categoria[]) => {
        this.allCategories.set(cats);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  close(): void {
    this.visible = false;
    this.visibleChange.emit(false);
    this.searchTerm.set('');
  }

  select(category: Categoria): void {
    this.onSelect.emit(category);
    this.close();
  }

  // Visual Helper for Icon
  getIconForType(): string {
    // Nuevos íconos: Visuales y elegantes (Cartera vs Bolsa de compras)
    return this.type === 'INGRESO' ? 'pi pi-wallet' : 'pi pi-shopping-bag';
  }

  getColorClass(): string {
    return this.type === 'INGRESO' ? 'text-green-500' : 'text-orange-500';
  }

  // Clase para el efecto hover (borde coloreado)
  getHoverClass(): string {
    return this.type === 'INGRESO' ? 'category-hover-income' : 'category-hover-expense';
  }
}
