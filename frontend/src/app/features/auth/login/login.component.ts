/**
 * Component: LoginComponent (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - RECIBE: Credenciales del usuario desde formulario
 * - LLAMA A: AuthService.login() (Supabase Auth)
 * - REDIRIGE: A /dashboard si éxito
 *
 * RESPONSABILIDAD:
 * Página de inicio de sesión.
 * Valida credenciales via Supabase Auth.
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

// Services
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    CardModule,
    InputTextModule,
    ButtonModule,
    PasswordModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  // Datos del formulario
  email = '';
  password = '';

  // Estados
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
  ) {}

  /**
   * Envía las credenciales a Supabase Auth
   */
  async onSubmit(): Promise<void> {
    // Validación básica
    if (!this.email || !this.password) {
      this.errorMessage.set('Por favor completa todos los campos');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const credentials: LoginRequest = {
      email: this.email,
      password: this.password,
    };

    const result = await this.authService.login(credentials);

    if (result.success) {
      this.messageService.add({
        severity: 'success',
        summary: '¡Bienvenido!',
        detail: 'Inicio de sesión exitoso',
      });
      this.router.navigate(['/dashboard']);
    } else {
      this.loading.set(false);
      this.errorMessage.set(result.error || 'Error al iniciar sesión. Intenta nuevamente.');
    }
  }
}
