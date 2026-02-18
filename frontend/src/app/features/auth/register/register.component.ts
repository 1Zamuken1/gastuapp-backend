/**
 * Component: RegisterComponent (Supabase Auth)
 *
 * RESPONSABILIDAD:
 * Permite registrar nuevos usuarios en el sistema via Supabase Auth.
 * Utiliza Reactive Forms para validaciones complejas.
 * Los datos adicionales (nombre, tipología, etc.) se envían como user_metadata
 * que el trigger de BD usa para crear el registro en public.usuarios.
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
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
import { AutoCompleteModule } from 'primeng/autocomplete';
import { StepsModule } from 'primeng/steps';
import { MenuItem, MessageService } from 'primeng/api';

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
    AutoCompleteModule,
    StepsModule,
  ],
  providers: [MessageService],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = signal(false);

  // Wizard State
  currentStep = signal(0);
  steps: MenuItem[] = [{ label: 'Datos Personales' }, { label: 'Perfil' }, { label: 'Detalles' }];

  // Tipologías con Iconos (PrimeIcons)
  tipologias = [
    {
      label: 'Estudiante',
      value: 'ESTUDIANTE',
      icon: 'pi pi-book',
      description: 'Dedico mi tiempo al estudio',
    },
    {
      label: 'Trabajador',
      value: 'TRABAJADOR',
      icon: 'pi pi-briefcase',
      description: 'Tengo un empleo formal',
    },
    {
      label: 'Independiente',
      value: 'INDEPENDIENTE',
      icon: 'pi pi-rocket',
      description: 'Trabajo por mi cuenta',
    },
    { label: 'Otro', value: 'OTRO', icon: 'pi pi-user', description: 'Otra situación' },
  ];

  // Profesiones para Autocomplete
  profesionesSugeridas: string[] = [
    'Ingeniero de Sistemas',
    'Ingeniero Civil',
    'Ingeniero Industrial',
    'Médico',
    'Enfermero',
    'Odontólogo',
    'Psicólogo',
    'Abogado',
    'Contador',
    'Administrador',
    'Diseñador Gráfico',
    'Arquitecto',
    'Docente',
    'Profesor',
    'Estudiante',
    'Desarrollador de Software',
    'Analista de Datos',
    'Comerciante',
    'Vendedor',
    'Asesor',
    'Freelancer',
    'Consultor',
  ];
  profesionesFiltradas = signal<string[]>([]);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
  ) {
    this.registerForm = this.fb.group({
      // Step 1
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      telefono: ['', [Validators.required]],

      // Step 2
      tipologia: ['', [Validators.required]],

      // Step 3
      profesion: ['', [Validators.required]],
      institucion: ['', [Validators.required]],
    });
  }

  // ================= NAVIGATION =================

  nextStep(): void {
    if (this.currentStep() === 0) {
      const fields = ['nombre', 'apellido', 'email', 'password', 'telefono'];
      if (!this.validateFields(fields)) return;
    } else if (this.currentStep() === 1) {
      if (!this.validateFields(['tipologia'])) {
        this.messageService.add({
          severity: 'warn',
          summary: 'Selección requerida',
          detail: 'Por favor selecciona una ocupación',
        });
        return;
      }
    }

    if (this.currentStep() < this.steps.length - 1) {
      this.currentStep.update((v) => v + 1);
    }
  }

  prevStep(): void {
    if (this.currentStep() > 0) {
      this.currentStep.update((v) => v - 1);
    }
  }

  selectTipologia(valor: string): void {
    this.registerForm.patchValue({ tipologia: valor });
  }

  // ================= AUTOCOMPLETE =================

  filterProfesion(event: any) {
    const query = event.query.toLowerCase();
    const filtered = this.profesionesSugeridas.filter((p) => p.toLowerCase().includes(query));
    this.profesionesFiltradas.set(filtered);
  }

  // ================= SUBMIT =================

  /**
   * Registra al usuario via Supabase Auth.
   * Los datos adicionales se envían como user_metadata.
   */
  async onSubmit(): Promise<void> {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario incompleto',
        detail: 'Por favor revisa todos los pasos.',
      });
      return;
    }

    this.loading.set(true);
    const request: RegistroRequest = this.registerForm.value;

    const result = await this.authService.register(request);

    if (result.success && !result.error) {
      // Registro exitoso con auto-login
      this.messageService.add({
        severity: 'success',
        summary: '¡Bienvenido a GastuApp!',
        detail: 'Cuenta creada exitosamente.',
      });
      setTimeout(() => this.router.navigate(['/dashboard']), 1500);
    } else if (result.success && result.error) {
      // Registro exitoso pero requiere confirmación de email
      this.loading.set(false);
      this.messageService.add({
        severity: 'info',
        summary: 'Revisa tu email',
        detail: result.error,
        life: 8000,
      });
    } else {
      // Error en registro
      this.loading.set(false);
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: result.error || 'No se pudo crear la cuenta.',
      });
    }
  }

  // Helper validation
  private validateFields(fields: string[]): boolean {
    let isValid = true;
    fields.forEach((field) => {
      const control = this.registerForm.get(field);
      if (control?.invalid) {
        control.markAsTouched();
        control.markAsDirty();
        isValid = false;
      }
    });

    if (!isValid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Datos incompletos',
        detail: 'Por favor completa todos los campos requeridos.',
      });
    }
    return isValid;
  }

  isInvalid(field: string): boolean {
    const control = this.registerForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
