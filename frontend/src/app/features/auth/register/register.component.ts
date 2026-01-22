/**
 * Component: RegisterComponent
 *
 * RESPONSABILIDAD:
 * Permite registrar nuevos usuarios en el sistema.
 * Utiliza Reactive Forms para validaciones complejas.
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-22
 */
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { ToastModule } from 'primeng/toast';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';

// Services & Models
import { AuthService } from '../../../core/services/auth.service';
import { RegistroRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    CardModule,
    InputTextModule,
    ButtonModule,
    PasswordModule,
    ToastModule,
    SelectModule,
  ],
  providers: [MessageService],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = signal(false);

  // Tipologías (podrían venir del backend o enums)
  tipologias = [
    { label: 'Trabajador', value: 'TRABAJADOR' },
    { label: 'Estudiante', value: 'ESTUDIANTE' },
    { label: 'Independiente', value: 'INDEPENDIENTE' },
    { label: 'Otro', value: 'OTRO' },
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
  ) {
    this.registerForm = this.fb.group({
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      telefono: ['', [Validators.required]],
      tipologia: ['TRABAJADOR', [Validators.required]],
      profesion: ['', [Validators.required]],
      institucion: ['', [Validators.required]],
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario inválido',
        detail: 'Por favor revisa los campos marcados en rojo.',
      });
      return;
    }

    this.loading.set(true);

    const request: RegistroRequest = this.registerForm.value;

    this.authService.register(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: '¡Registro exitoso!',
          detail: 'Tu cuenta ha sido creada. Iniciando sesión...',
        });
        // Pequeño delay para que el usuario lea el mensaje
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1500);
      },
      error: (err) => {
        console.error('Error registro:', err);
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error al registrar',
          detail: err.error?.message || 'No se pudo crear la cuenta. Intenta de nuevo.',
        });
      },
    });
  }

  // Helper para validaciones en template
  isInvalid(field: string): boolean {
    const control = this.registerForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
