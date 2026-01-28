# AGENTS.md - Guía para Agentes de Código

## 📋 Descripción del Proyecto

**GastuApp** - Sistema de gestión financiera personal con enfoque en conciencia financiera activa basado en Calm Design Framework.

### Stack Tecnológico
- **Frontend**: Angular 21.1.1 con standalone components, PrimeNG 21.0.4, SCSS
- **Backend**: Spring Boot 4.0.1, Java 21, PostgreSQL, JWT
- **Arquitectura**: Hexagonal (Backend), Component-Based (Frontend)

### 📄 Contexto del Proyecto
**IMPORTANTE:** Este proyecto sigue una ruta de ejecución por niveles documentada en `@docs/CONTEXTO_GASTUAPP.md`. Los agentes deben consultar este archivo para entender:

- **Estado actual del desarrollo** (commits completados y pendientes)
- **Próximos pasos planificados** por fases
- **Módulos implementados** vs módulos pendientes
- **Arquitectura detallada** y convenciones específicas

**Estado Actual (Enero 2026):**
- ✅ Backend: Módulos Usuario, Autenticación JWT, Categorías, Transacciones, Ahorros (COMPLETO)
- ✅ Frontend: Angular 21 + PrimeNG 21 + Autenticación + Dashboard + Transacciones + Ahorros (COMPLETO)
- ⏸️ Próximos módulos: ConfiguraciónUsuario, Cuenta Bancaria, Presupuesto

---

## 🚀 Comandos de Build/Lint/Format

### Frontend (Angular)
```bash
# Desarrollo
cd frontend
npm start                    # Inicia servidor de desarrollo (ng serve)
ng serve                     # Inicia en http://localhost:4200

# Build
npm run build               # Build de producción
ng build                    # Build optimizado con budgets
ng build --configuration development  # Build desarrollo con source maps

# Formateo
npx prettier --write "src/**/*.{ts,html,scss}"  # Formatear todo
npx prettier --check "src/**/*.{ts,html,scss}" # Verificar formato
```

### Backend (Spring Boot)
```bash
# Desarrollo
cd backend
./mvnw spring-boot:run      # Inicia servidor en http://localhost:8080
mvn spring-boot:run         # Alternativa con Maven instalado

# Build
./mvnw clean compile        # Compilar
./mvnw clean package        # Generar JAR ejecutable
./mvnw clean install        # Compilar + instalar en local

# Formateo (si se configura)
mvn spotless:apply          # Aplicar formato (requiere plugin spotless)
mvn spotless:check          # Verificar formato
```

---

## 🎨 Guías de Estilo y Convenciones

### Frontend (TypeScript/Angular)

#### 📁 Estructura de Carpetas
```
src/app/
├── core/                   # Singletons, servicios globales
│   ├── services/          # Servicios inyectados en 'root'
│   ├── models/            # Modelos de dominio
│   ├── guards/            # Route guards
│   └── interceptors/      # HTTP interceptors
├── features/              # Módulos de funcionalidad
│   ├── [feature]/
│   │   ├── [feature].component.ts
│   │   ├── [feature].component.html
│   │   └── [feature].component.scss
├── layout/                # Componentes de layout
└── shared/                # Componentes reutilizables
    └── components/
```

#### 🏗️ Componentes (Standalone)
```typescript
/**
 * Component: [NombreComponent]
 *
 * FLUJO DE DATOS:
 * - RECIBE: Datos de servicios
 * - RENDERIZA: UI elements  
 * - EMITE: Events (opcional)
 *
 * RESPONSABILIDAD:
 * [Descripción del propósito del componente]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-XX
 */
import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-[nombre]',
  standalone: true,
  imports: [CommonModule, /* otros módulos */],
  templateUrl: './[nombre].component.html',
  styleUrl: './[nombre].component.scss',
})
export class [Nombre]Component {
  // Signals para estado reactivo
  loading = signal(true);
  data = signal<DatoType[]>([]);
  
  // Computed para estado derivado
  isEmpty = computed(() => this.data().length === 0);
}
```

#### 🔄 Signals (Estado Reactivo)
```typescript
// Signals para estado primitivo
loading = signal<boolean>(true);
error = signal<string | null>(null);

// Signals para objetos/arrays
user = signal<User | null>(null);
items = signal<Item[]>([]);

// Computed para estado derivado
isAuthenticated = computed(() => !!this.user());
totalAmount = computed(() => 
  this.items().reduce((sum, item) => sum + item.amount, 0)
);
```

#### 🌐 Servicios (Inyectables)
```typescript
/**
 * Service: [NombreService]
 *
 * FLUJO DE DATOS:
 * - RECIBE: Datos desde componentes
 * - LLAMA A: Backend endpoints
 * - PROVEE: Estado reactivo a la aplicación
 *
 * RESPONSABILIDAD:
 * [Descripción del servicio]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-XX
 */
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class [Nombre]Service {
  private readonly apiUrl = `${environment.apiUrl}/[endpoint]`;
  
  // Signals para estado
  loading = signal(false);
  error = signal<string | null>(null);
  
  constructor(private http: HttpClient) {}
  
  getData(): Observable<ResponseType> {
    this.loading.set(true);
    return this.http.get<ResponseType>(this.apiUrl).pipe(
      tap(() => this.loading.set(false)),
      catchError((err) => {
        this.error.set('Error al cargar datos');
        return throwError(() => err);
      })
    );
  }
}
```

#### 📝 Imports y Organización
```typescript
// 1. Angular core
import { Component, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';

// 2. Third party (PrimeNG, etc.)
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

// 3. Application imports
import { [Nombre]Service } from '../../core/services/[nombre].service';
import { [Model]Type } from '../../core/models/[nombre].model';
```

#### 🎨 Estilos (SCSS)
```scss
// Usar variables de PrimeNG cuando sea posible
:host {
  display: block;
}

.nombre-component {
  padding: 1rem;
  
  &__header {
    margin-bottom: 1rem;
  }
  
  &__content {
    background: var(--surface-card);
    border-radius: var(--border-radius);
    padding: 1rem;
  }
}
```

### Backend (Java/Spring Boot)

#### 📁 Estructura de Paquetes (Hexagonal)
```
com.gastuapp/
├── domain/                 # Lógica de negocio pura
│   ├── model/             # Modelos de dominio (sin anotaciones)
│   ├── port/              # Interfaces (contratos)
│   └── service/           # Servicios de dominio
├── application/           # Casos de uso
│   ├── service/           # Servicios de aplicación
│   ├── mapper/            # DTO ↔ Domain mapping
│   └── dto/               # Request/Response DTOs
└── infrastructure/        # Preocupaciones externas
    ├── adapter/
    │   ├── persistence/   # Base de datos
    │   └── rest/          # REST controllers
    ├── config/            # Configuración
    └── security/          # Seguridad (JWT)
```

#### 📋 Documentación JavaDoc
```java
/**
 * [Tipo]: [Nombre]
 *
 * FLUJO DE DATOS:
 * - RECIBE DATOS DE: [Capa anterior]
 * - ENVÍA DATOS A: [Capa siguiente]
 * - USADO POR: [Clases que lo usan]
 *
 * RESPONSABILIDAD:
 * [Descripción detallada del propósito]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-XX
 */
```

#### 🏗️ Controllers (REST)
```java
@RestController
@RequestMapping("/api/[endpoint]")
@CrossOrigin(origins = "*")
public class [Nombre]Controller {
    
    private final [Nombre]Service service;
    
    public [Nombre]Controller([Nombre]Service service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<[Response]DTO> create(
            @Valid @RequestBody [Request]DTO dto) {
        [Response]DTO response = service.create(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<[Response]DTO> getById(@PathVariable UUID id) {
        [Response]DTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }
}
```

#### 🔄 Services (Application)
```java
@Service
@Transactional
public class [Nombre]Service {
    
    private final [Nombre]RepositoryPort repository;
    private final [Nombre]Mapper mapper;
    
    public [Nombre]Service(
            [Nombre]RepositoryPort repository,
            [Nombre]Mapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public [Response]DTO create([Request]DTO dto) {
        // 1. Convertir DTO a Domain
        [Domain] domain = mapper.toDomain(dto);
        
        // 2. Guardar en repositorio
        [Domain] saved = repository.save(domain);
        
        // 3. Convertir a DTO de respuesta
        return mapper.toResponseDTO(saved);
    }
}
```

#### 📦 Models (Domain)
```java
// Modelos de dominio - SIN anotaciones JPA
public class [Nombre] {
    private UUID id;
    private String nombre;
    private boolean activo;
    
    // Constructor con parámetros
    public [Nombre](UUID id, String nombre, boolean activo) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.activo = activo;
    }
    
    // Getters only (inmutabilidad)
    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isActivo() { return activo; }
    
    // Métodos de negocio
    public [Nombre] desactivar() {
        return new [Nombre](this.id, this.nombre, false);
    }
}
```

#### 🗄️ Entities (JPA)
```java
@Entity
@Table(name = "[tabla]")
public class [Nombre]Entity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private boolean activo;
    
    // Getters/Setters para JPA
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
```

---

## 🔧 Configuración de Build

### Frontend (Angular)
- **Budgets**: Bundle inicial < 500kB, componentes < 4kB
- **Optimización**: Tree-shaking, AOT compilation, minificación
- **Output**: `dist/frontend/`
- **Source Maps**: Solo en desarrollo
- **Lazy Loading**: Módulos de features cargados bajo demanda

### Backend (Maven)
- **Java Version**: 21 (LTS)
- **Spring Boot**: 4.0.1 con parent POM
- **Lombok**: Annotation processing configurado
- **Packaging**: JAR ejecutable con embedded Tomcat
- **Dependencies**: Spring Starters (JPA, Security, Validation, Web)

---

## 📛 Convenciones de Nomenclatura

### Frontend
- **Componentes**: PascalCase (`DashboardComponent.ts`)
- **Archivos**: kebab-case (`dashboard.component.ts`)
- **Carpetas**: kebab-case (`dashboard/`)
- **Servicios**: camelCase con sufijo (`authService.ts`)
- **Models**: camelCase (`userModel.ts`)
- **Variables**: camelCase (`userName`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`)

### Backend
- **Clases**: PascalCase (`UsuarioService.java`)
- **Métodos**: camelCase (`findById()`)
- **Variables**: camelCase (`usuarioId`)
- **Constantes**: UPPER_SNAKE_CASE (`DEFAULT_PAGE_SIZE`)
- **Paquetes**: lowercase (`com.gastuapp.domain.model`)
- **Enums**: PascalCase (`RolUsuario.java`)

---

## 🛡️ Patrones de Seguridad

### JWT Authentication
- **Frontend**: Token en localStorage, signal reactivo, interceptor automático
- **Backend**: Validación en cada request, roles en token, secret key 512 bits
- **Públicos**: `/auth/login`, `/auth/register`, `/health`

### Validaciones
- **Frontend**: Reactive forms con validaciones Angular
- **Backend**: Jakarta Validation (`@Valid`, `@NotNull`, `@Email`)
- **DTOs**: Validaciones de negocio en capa de aplicación

---

## 🔄 Patrones de Integración

### API Integration
- **Base URL**: Configuración por ambiente (`environment.ts`)
- **Error Handling**: Centralizado con RxJS operators
- **HTTP Methods**: Convenciones RESTful
- **Response Handling**: Operadores RxJS (tap, catchError, map)

### Estado Global
- **Frontend**: Signals con computed para estado derivado
- **Backend**: Stateless con JWT, estado en base de datos

---

## 📊 Monitoreo y Logging

### Frontend
- **Console Logs**: Para desarrollo, remover en producción
- **Error Handling**: Catch blocks con logging contextual
- **Performance**: Angular budgets y bundle analysis

### Backend
- **Logging**: SLF4J con Logback (configuración Spring Boot)
- **Error Handling**: GlobalExceptionHandler con respuestas estandarizadas
- **Health Checks**: Endpoints `/health` para monitoreo

---

## 🎯 Mejores Prácticas

### Frontend
1. **Standalone Components**: Siempre usar `standalone: true`
2. **Signals**: Preferir signals sobre BehaviorSubject para estado
3. **Lazy Loading**: Cargar features bajo demanda
4. **Type Safety**: Interfaces fuertes, sin `any`
5. **Error Boundaries**: Manejo de errores en servicios y componentes

### Backend
1. **Hexagonal Architecture**: Respetar separación de capas
2. **Inmutabilidad**: Domain models inmutables
3. **Validaciones**: DTOs con validaciones Jakarta
4. **Transacciones**: `@Transactional` en servicios de aplicación
5. **Excepciones**: Lanzar excepciones específicas del dominio

---

## 🚨 Consideraciones Especiales

### Desarrollo Local
- **Frontend**: `ng serve` en puerto 4200, proxy a backend 8080
- **Backend**: `./mvnw spring-boot:run` en puerto 8080
- **Base de Datos**: PostgreSQL local o Supabase

### Ruta de Ejecución por Fases
**CRÍTICO:** Antes de comenzar cualquier desarrollo, revisar `@docs/CONTEXTO_GASTUAPP.md` para:

1. **Verificar estado actual** de cada módulo (commits completados)
2. **Identificar próximos pasos** según la planificación
3. **Entender arquitectura específica** de cada módulo
4. **Seguir secuencia de implementación** definida en el contexto
5. **Actualizar el contexto** después de completar cada fase

**Ejemplo de Flujo:**
- Módulo Usuario → Módulo Autenticación → Módulo Categorías → Módulo Transacciones → Módulo Ahorros
- Cada módulo requiere: Domain → Application → Infrastructure → Tests (opcional) → Frontend

### Environment Variables
```typescript
// frontend/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  tokenKey: 'gastuapp_token',
  defaultPageSize: 10,
};
```

### Patrones de Documentación
- **Cada clase**: JavaDoc/TS Doc completo
- **Métodos públicos**: Siempre documentados
- **FLUJO DE DATOS**: Sección obligatoria en clases principales
- **RESPONSABILIDAD**: Descripción clara del propósito

---

## 📝 Notas para Agentes

1. **Mantener arquitectura**: No mezclar capas (domain con infrastructure)
2. **Seguir convenciones**: Usar patrones establecidos de nomenclatura
3. **Documentar siempre**: Cada nueva clase/método debe tener documentación
4. **Tests**: Frameworks configurados pero tests mínimos actualmente
5. **Seguridad**: Nunca exponer passwords ni datos sensibles en logs
6. **Performance**: Respetar budgets de Angular y optimizar queries JPA

Este proyecto sigue estándares enterprise-level con arquitecturas modernas y separación clara de responsabilidades.

---

## 🔄 Secuencia de Desarrollo por Módulos

### Módulos Completados (Enero 2026)

#### ✅ Backend Completado
- **Módulo Usuario**: Domain → Application → Infrastructure → REST (Commits 11-15)
- **Autenticación JWT**: JwtUtils + Filter + SecurityConfig (Commit 16)
- **Módulo Categorías**: 15 categorías predefinidas + DataSeeder (Commits 17-18)
- **Módulo Transacciones**: CRUD completo + filtros + balance (Commit 19)
- **Módulo Ahorros**: Metas de ahorro + cuotas + progreso (Commits 20-22)

#### ✅ Frontend Completado
- **Angular 21 + PrimeNG 21**: Setup + configuración (Commit 20)
- **Autenticación**: Login + Register + JWT interceptor (Commit 21)
- **Dashboard**: Cards balance + navegación (Commit 22)
- **Transacciones**: Lista + CRUD + filtros (Commits 23-25)
- **Ahorros**: Metas + progreso + gráficos (Commits 26-28)

### 📋 Próximos Módulos (Plan 2026)

#### ⏸️ ConfiguraciónUsuario (PENDIENTE)
- **Backend**: Service + Controller + endpoints
- **Frontend**: Componentes de configuración de perfil

#### ⏸️ Cuenta Bancaria (PENDIENTE)
- **Backend**: Domain → Application → Infrastructure → REST
- **Frontend**: Listado + CRUD + vinculación con transacciones

#### ⏸️ Presupuesto (PENDIENTE)
- **Backend**: Límites mensuales + alertas
- **Frontend**: Configuración + seguimiento + visualización

### 🎯 Flujo de Trabajo Estándar

**Para cada nuevo módulo:**
1. **Domain**: Modelos puos + puertos (interfaces)
2. **Application**: DTOs + Mappers + Services
3. **Infrastructure**: Entities + Repositories + Adapters
4. **REST**: Controllers con validaciones
5. **Frontend**: Components + Services + UI
6. **Testing**: Unit tests (opcional actualmente)
7. **Documentation**: Actualizar CONTEXTO_GASTUAPP.md

**Commit Pattern:**
```
feat: [módulo] - [funcionalidad principal]

Arquitectura:
- Domain: [detalles de modelos]
- Application: [casos de uso implementados]
- Infrastructure: [entidades y repositorios]
- REST: [endpoints creados]
- Frontend: [componentes y servicios]

Funcionalidades:
- [lista de funcionalidades implementadas]

Validaciones:
- [validaciones y reglas de negocio]
```