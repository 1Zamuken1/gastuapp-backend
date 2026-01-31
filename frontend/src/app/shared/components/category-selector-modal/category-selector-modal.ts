import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnInit,
  signal,
  computed,
  effect,
  OnChanges,
  SimpleChanges,
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
export class CategorySelectorModal implements OnChanges {
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

    // El filtro por tipo ya se hace al cargar desde el backend,
    // pero filtramos de nuevo por seguridad
    let list = this.allCategories();

    // Filtro de búsqueda seguro
    if (term) {
      list = list.filter((c) => (c.nombre || '').toLowerCase().includes(term));
    }

    return list;
  });

  constructor(private categoriaService: CategoriaService) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Si cambia el tipo o se abre el modal, recargar categorías
    if (changes['visible']?.currentValue === true || changes['type']) {
      // Solo cargar si es visible para evitar cargas innecesarias
      if (this.visible) {
        this.loadCategories();
      }
    }
  }

  loadCategories(): void {
    this.loading.set(true);
    // Cargar categorías según el tipo actual
    this.categoriaService.listarPorTipo(this.type).subscribe({
      next: (cats: Categoria[]) => {
        this.allCategories.set(cats);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading categories', err);
        this.allCategories.set([]); // Evitar estado inconsistente
        this.loading.set(false);
      },
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
