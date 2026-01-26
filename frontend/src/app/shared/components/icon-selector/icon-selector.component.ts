import { Component, EventEmitter, Input, Output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-icon-selector',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, InputTextModule, ButtonModule],
  templateUrl: './icon-selector.component.html',
  styleUrl: './icon-selector.component.scss',
})
export class IconSelectorComponent {
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() iconSelected = new EventEmitter<string>();

  // Search
  searchTerm = signal('');

  // Icon List (Con etiquetas en ESPAÑOL)
  allIcons: { icon: string; tags: string[] }[] = [
    // Finanzas
    { icon: 'pi pi-wallet', tags: ['billetera', 'cartera', 'dinero', 'finanzas', 'pago'] },
    { icon: 'pi pi-money-bill', tags: ['billete', 'efectivo', 'plata', 'dinero'] },
    { icon: 'pi pi-credit-card', tags: ['tarjeta', 'credito', 'debito', 'banco'] },
    { icon: 'pi pi-dollar', tags: ['dolar', 'dinero', 'moneda', 'precio'] },
    { icon: 'pi pi-euro', tags: ['euro', 'moneda', 'europa'] },
    { icon: 'pi pi-bitcoin', tags: ['bitcoin', 'cripto', 'moneda'] },
    { icon: 'pi pi-calculator', tags: ['calculadora', 'cuentas', 'contabilidad'] },
    { icon: 'pi pi-chart-bar', tags: ['grafico', 'estadisticas', 'inversion', 'crecimiento'] },
    { icon: 'pi pi-chart-line', tags: ['linea', 'tendencia', 'progreso'] },
    { icon: 'pi pi-percentage', tags: ['porcentaje', 'interes', 'descuento'] },
    // Hogar y Vida
    { icon: 'pi pi-home', tags: ['casa', 'hogar', 'vivienda', 'alquiler', 'familia'] },
    { icon: 'pi pi-building', tags: ['edificio', 'apartamento', 'oficina'] },
    { icon: 'pi pi-key', tags: ['llave', 'seguridad', 'acceso'] },
    { icon: 'pi pi-shopping-cart', tags: ['carrito', 'compras', 'supermercado', 'tienda'] },
    { icon: 'pi pi-shopping-bag', tags: ['bolsa', 'compras', 'regalo'] },
    { icon: 'pi pi-box', tags: ['caja', 'paquete', 'envio'] },
    // Transporte y Viajes
    { icon: 'pi pi-car', tags: ['carro', 'auto', 'coche', 'vehiculo', 'transporte'] },
    { icon: 'pi pi-truck', tags: ['camion', 'mudanza', 'transporte'] },
    { icon: 'pi pi-compass', tags: ['brujula', 'viaje', 'direccion', 'explorar'] },
    { icon: 'pi pi-map', tags: ['mapa', 'plano', 'ubicacion'] },
    { icon: 'pi pi-map-marker', tags: ['marcador', 'ubicacion', 'punto', 'destino'] },
    { icon: 'pi pi-ticket', tags: ['ticket', 'boleto', 'entrada', 'cine', 'vuelo'] },
    { icon: 'pi pi-globe', tags: ['mundo', 'tierra', 'viaje', 'internacional'] },
    { icon: 'pi pi-send', tags: ['avion', 'enviar', 'viaje', 'papel'] },
    // Tecnología
    { icon: 'pi pi-mobile', tags: ['celular', 'movil', 'telefono', 'smartphone'] },
    { icon: 'pi pi-desktop', tags: ['computador', 'pc', 'ordenador', 'monitor'] },
    { icon: 'pi pi-tablet', tags: ['tablet', 'ipad', 'dispositivo'] },
    { icon: 'pi pi-wifi', tags: ['wifi', 'internet', 'red', 'conexion'] },
    { icon: 'pi pi-camera', tags: ['camara', 'foto', 'video'] },
    { icon: 'pi pi-video', tags: ['video', 'pelicula', 'cine', 'grabacion'] },
    // Trabajo y Estudio
    { icon: 'pi pi-briefcase', tags: ['maletin', 'trabajo', 'negocios', 'oficina'] },
    { icon: 'pi pi-envelope', tags: ['sobre', 'carta', 'correo', 'email'] },
    { icon: 'pi pi-book', tags: ['libro', 'lectura', 'estudio', 'educacion'] },
    { icon: 'pi pi-pencil', tags: ['lapiz', 'escribir', 'editar', 'arte'] },
    // Salud y Bienestar
    { icon: 'pi pi-heart', tags: ['corazon', 'salud', 'vida', 'amor'] },
    { icon: 'pi pi-heart-fill', tags: ['corazon', 'salud', 'vida', 'amor'] },
    { icon: 'pi pi-apple', tags: ['manzana', 'fruta', 'comida', 'salud'] },
    { icon: 'pi pi-bolt', tags: ['rayo', 'energia', 'electricidad', 'fuerza'] },
    // Entretenimiento y Otros
    { icon: 'pi pi-gift', tags: ['regalo', 'premio', 'sorpresa'] },
    { icon: 'pi pi-star', tags: ['estrella', 'favorito', 'destacado'] },
    { icon: 'pi pi-palette', tags: ['paleta', 'arte', 'pintura', 'color'] },
    { icon: 'pi pi-wrench', tags: ['herramienta', 'reparacion', 'ajustes'] },
    { icon: 'pi pi-tag', tags: ['etiqueta', 'precio', 'categoria'] },
    { icon: 'pi pi-calendar', tags: ['calendario', 'fecha', 'agenda', 'tiempo'] },
    { icon: 'pi pi-clock', tags: ['reloj', 'tiempo', 'hora'] },
    { icon: 'pi pi-bell', tags: ['campana', 'notificacion', 'alerta'] },
    { icon: 'pi pi-trash', tags: ['basura', 'eliminar', 'papelera'] },
    { icon: 'pi pi-lock', tags: ['candado', 'seguridad', 'bloqueado'] },
  ];

  // Filtered Icons
  filteredIcons = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return this.allIcons.map((i) => i.icon);

    return this.allIcons
      .filter(
        (item) =>
          // Busco en tags O en el nombre de la clase
          item.tags.some((tag) => tag.includes(term)) || item.icon.includes(term),
      )
      .map((i) => i.icon);
  });

  close(): void {
    this.visible = false;
    this.visibleChange.emit(false);
    this.searchTerm.set('');
  }

  selectIcon(icon: string): void {
    this.iconSelected.emit(icon);
    this.close();
  }
}
