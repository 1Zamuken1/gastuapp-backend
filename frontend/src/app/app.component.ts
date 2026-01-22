import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// Importaciones de PrimeNG
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ButtonModule, CardModule],
  templateUrl: './app.component.html', // <-- Asegúrate de que apunte al archivo
  styleUrl: './app.component.scss',
})
export class AppComponent {
  title = 'GastuApp';
}
