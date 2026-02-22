# CONTEXTO ACTUALIZADO - GASTUAPP V2.0

**Última actualización:** 22 de febrero de 2026
**Estado:** Backend + Frontend funcionales con Supabase Auth integrado

---

## INFORMACIÓN GENERAL DEL PROYECTO

### ¿Qué es GastuApp?

**GastuApp** es un gestor de finanzas personales con enfoque en **conciencia financiera activa** basado en el **Calm Design Framework**.

**Filosofía del producto:**

- Tracking manual recomendado (conciencia activa) + automático opcional
- Gamificación positiva (badges, progreso, celebraciones)
- Sin ansiedad financiera (diseño calmado, sin alertas agresivas)
- Educación financiera para padres e hijos

---

## STACK TECNOLÓGICO

### Backend (Actual)

- **Framework:** Spring Boot 4.0.1
- **Java:** 21
- **Base de datos:** PostgreSQL (Supabase)
- **Arquitectura:** Hexagonal (Ports & Adapters)
- **Seguridad:** Supabase Auth (ES256/JWKS) + fallback JWT HS256 legacy
- **ORM:** JPA/Hibernate

### Frontend (Implementado)

- **Framework:** Angular 19
- **Librería UI:** PrimeNG
- **Estilo:** Calm Design Framework (tema oscuro, colores por módulo)
- **Auth:** Supabase JS (@supabase/supabase-js v2)
- **HTTP:** Angular HttpClient + interceptor JWT

### IA (Futuro - Fase 3)

- **Provider:** GROQ (Llama 3)
- **Tipo:** AI Agent con function calling

---

## AUTENTICACIÓN (ESTADO ACTUAL)

### Flujo Completo

```
Usuario → Supabase Auth (ES256) → Frontend (Angular)
                                       │
                              Authorization: Bearer <supabase_token>
                                       │
                              Spring Boot (JwtAuthenticationFilter)
                                       │
                         ┌─────────────┴──────────────┐
                         ▼                              ▼
               SupabaseJwtUtils               JwtUtils (legacy)
               (ES256 / JWKS)                 (HS256 / secret)
                         │
                  findBySupabaseUid()
                         │
               principal = usuario.getId() [Long]
                         │
               SecurityContextHolder
```

### Detalles Clave

- **Supabase firma tokens con ES256** (no HS256). Las claves públicas se obtienen desde:
  `https://<project>.supabase.co/auth/v1/.well-known/jwks.json`
- **`SupabaseJwtUtils`** valida tokens ES256 usando JWKS cacheado
- **`JwtAuthenticationFilter`** tiene estrategia dual:
  1. Intenta validar con SupabaseJwtUtils (Supabase tokens)
  2. Fallback a JwtUtils (tokens legacy HS256 propios)
- **El principal en SecurityContext es el ID interno (Long)** — no el supabase_uid (UUID). Los controllers hacen `Long.parseLong(authentication.getName())`
- La tabla `usuarios` tiene columna `supabase_uid UUID` que vincula con `auth.users.id` de Supabase

### Registro/Login

- El registro y login se hacen **directamente contra Supabase Auth** desde el frontend
- El backend solo valida el token, no genera tokens propios para usuarios Supabase
- `AuthController` aún tiene endpoints legacy pero no son usados por el flujo principal

---

## ARQUITECTURA BACKEND

### Estructura de Capas (Hexagonal)

```
src/main/java/com/gastuapp/
├── domain/
│   ├── model/
│   │   ├── usuario/         (Usuario, RolUsuario, TipologiaUsuario, ConfiguracionUsuario)
│   │   ├── categoria/       (Categoria, TipoCategoria)
│   │   ├── transaccion/     (Transaccion, TipoTransaccion)
│   │   ├── ahorro/          (MetaAhorro, Ahorro, CuotaAhorro)
│   │   ├── planificacion/   (Presupuesto/Planificacion)
│   │   ├── proyeccion/      (Proyeccion)
│   │   ├── badge/           (pendiente)
│   │   ├── notificacion/    (pendiente)
│   │   └── suscripcion/     (pendiente)
│   └── port/
│       └── [Entidad]RepositoryPort.java
│
├── application/
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UsuarioService.java
│   │   ├── CategoriaService.java
│   │   ├── TransaccionService.java
│   │   ├── AhorroService.java
│   │   ├── PresupuestoService.java
│   │   ├── PresupuestoScheduler.java
│   │   └── ProyeccionService.java
│   ├── mapper/
│   └── dto/ (request/ + response/)
│
└── infrastructure/
    ├── adapter/
    │   ├── persistence/entity/
    │   │   ├── UsuarioEntity.java          (con supabase_uid UUID)
    │   │   ├── ConfiguracionUsuarioEntity.java
    │   │   ├── CategoriaEntity.java
    │   │   ├── TransaccionEntity.java
    │   │   ├── MetaAhorroEntity.java
    │   │   ├── AhorroEntity.java
    │   │   ├── CuotaAhorroEntity.java
    │   │   ├── PresupuestoEntity.java
    │   │   └── ProyeccionEntity.java
    │   └── rest/controller/
    │       ├── AuthController.java
    │       ├── UsuarioController.java
    │       ├── CategoriaController.java
    │       ├── TransaccionController.java
    │       ├── AhorroController.java
    │       ├── PresupuestoController.java
    │       ├── ProyeccionController.java
    │       └── HealthController.java
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── JwtProperties.java
    │   └── DataSeeder.java
    └── security/jwt/
        ├── JwtUtils.java               (HS256 legacy)
        ├── SupabaseJwtUtils.java       (ES256 / JWKS)
        └── JwtAuthenticationFilter.java
```

---

## ENDPOINTS IMPLEMENTADOS

### Base URL

```
http://localhost:8080/api
```

### Auth (Mixto - principalmente legacy)

```
POST   /auth/register           # Registro legacy (sin Supabase)
POST   /auth/login              # Login legacy (sin Supabase)
GET    /health                  # Estado del servidor
```

### Usuarios (Autenticado)

```
GET    /usuarios/me             # Perfil del usuario autenticado
PUT    /usuarios/me             # Actualizar perfil propio
DELETE /usuarios/me             # Eliminar cuenta propia
GET    /usuarios/{publicId}     # Ver perfil (con validación de permisos)
POST   /usuarios/hijo           # Crear hijo (solo USER)
GET    /usuarios/hijos          # Listar hijos del usuario (solo USER)
GET    /admin/usuarios          # Listar todos (solo ADMIN)
```

### Categorías (Autenticado)

```
GET    /categorias                      # Listar todas las categorías
GET    /categorias/{id}                 # Por ID
GET    /categorias/tipo/{tipo}          # Por tipo (INGRESO/EGRESO)
```

### Transacciones (Autenticado)

```
POST   /transacciones
GET    /transacciones
GET    /transacciones/{id}
PUT    /transacciones/{id}
DELETE /transacciones/{id}
GET    /transacciones/tipo/{tipo}
GET    /transacciones/categoria/{id}
GET    /transacciones/rango
GET    /transacciones/balance
GET    /transacciones/resumen
```

### Ahorros (Autenticado)

```
POST   /ahorros/metas                   # Crear meta de ahorro
GET    /ahorros/metas                   # Listar metas
GET    /ahorros/metas/{id}
PUT    /ahorros/metas/{id}
DELETE /ahorros/metas/{id}
POST   /ahorros/metas/{id}/cuotas       # Registrar cuota
GET    /ahorros/metas/{id}/cuotas
GET    /ahorros/metas/{id}/progreso     # Progreso calculado
GET    /ahorros/resumen                 # Resumen global de ahorros
```

### Presupuestos/Planificaciones (Autenticado)

```
POST   /presupuestos-planificaciones
GET    /presupuestos-planificaciones
GET    /presupuestos-planificaciones/{id}
PUT    /presupuestos-planificaciones/{id}
DELETE /presupuestos-planificaciones/{id}
GET    /presupuestos-planificaciones/activas    # ⚠️ Retorna 409 (bug a resolver)
```

### Proyecciones (Autenticado)

```
GET    /proyecciones
GET    /proyecciones/{id}
POST   /proyecciones
```

---

## ESTADO DEL FRONTEND

### Módulos Implementados

| Módulo                       | Ruta                  | Estado                      |
| ---------------------------- | --------------------- | --------------------------- |
| **Auth**                     | `/login`, `/register` | ✅ Completo (Supabase Auth) |
| **Dashboard**                | `/dashboard`          | ✅ Completo                 |
| **Transacciones** (Ingresos) | `/ingresos`           | ✅ Completo                 |
| **Transacciones** (Egresos)  | `/egresos`            | ✅ Completo                 |
| **Ahorros**                  | `/ahorros`            | ✅ Completo                 |
| **Planificaciones**          | `/planificaciones`    | ✅ Completo                 |
| **Proyecciones**             | `/proyecciones`       | ✅ Completo                 |

### Servicios Core

- **`AuthService`** — gestión Supabase Auth, señales de sesión, token
- **`auth.interceptor.ts`** — añade `Authorization: Bearer <token>` a todas las requests al backend
- **`supabase.client.ts`** — instancia singleton con lock personalizado (evita NavigatorLockAcquireTimeoutError)

### PWA

- `public/manifest.webmanifest` — configurado correctamente

---

## ROLES Y PERMISOS

| Rol           | Descripción                               |
| ------------- | ----------------------------------------- |
| **ADMIN**     | Administrador del sistema                 |
| **USER**      | Usuario normal (adulto/padre)             |
| **USER_HIJO** | Usuario hijo (menor de edad, supervisado) |

---

## BASE DE DATOS (PostgreSQL - Supabase)

### Tablas Implementadas

- `usuarios` — con columna `supabase_uid UUID UNIQUE` y RLS habilitado
- `configuracion_usuario`
- `categorias` — 15 categorías predefinidas (DataSeeder)
- `transacciones`
- `metas_ahorro`
- `ahorros`
- `cuotas_ahorro`
- `presupuestos_planificaciones`
- `proyecciones`

### Seguridad BD

- RLS habilitado en todas las tablas (ver `docs/sql/001_enable_rls.sql`)
- Trigger `handle_new_user()` sincroniza `auth.users` → `public.usuarios`
- Plan de rollback disponible en `docs/sql/002_rollback_rls.sql`

---

## CONVENCIONES DEL PROYECTO

### Nomenclatura Backend

- Modelos domain: `Usuario.java`, `Transaccion.java`
- Ports: `[Entidad]RepositoryPort.java`
- Entities: `[Entidad]Entity.java`
- Repositories: `[Entidad]JpaRepository.java`
- Adapters: `[Entidad]RepositoryAdapter.java`
- Mappers: `[Entidad]EntityMapper.java` / `[Entidad]Mapper.java`
- Services: `[Entidad]Service.java`
- DTOs: `[Operacion]RequestDTO.java` / `[Entidad]ResponseDTO.java`
- Controllers: `[Entidad]Controller.java`

### Documentación de Clases

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
 * [Descripción]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since [Fecha]
 */
```

### Commits

```
feat/fix/refactor: [título conciso]

[Descripción estructurada en español]

Módulo/Área:
- ítem
- ítem
```

---

## PRÓXIMOS PASOS

### Inmediato (este sprint)

1. **Fix 409 en `/presupuestos-planificaciones/activas`**
   - Investigar lógica en `PresupuestoController` y `PresupuestoService`
   - El 409 Conflict sugiere un problema de estado/lógica, no de auth

2. **ConfiguracionUsuario** (pendiente de Service + Controller)
   - La Entity y Repository ya existen
   - Endpoints por crear:
     - `GET  /api/usuarios/me/configuracion`
     - `PUT  /api/usuarios/me/configuracion`

### Descartado / Pospuesto indefinidamente

- **Cuentas Bancarias con API externa** — APIs bancarias en Colombia (Belvo, Open Banking) requieren certificación SFC, contratos costosos y verificaciones legales. No viable para un proyecto personal.
- **Suscripciones con detección automática** — requiere datos bancarios en tiempo real. Mismo problema que cuentas bancarias.
- **IA / Proyecciones automáticas** — Fase 3, futuro lejano

### Posible en próximos sprints

- **Cuentas manuales** — el usuario registra sus cuentas y saldos manualmente (sin API bancaria)
- **Suscripciones manuales** — tracking manual de pagos recurrentes (Netflix, gym, etc.)
- **ConfiguracionUsuario UI** — pantalla de perfil/configuración en el frontend
- **Notificaciones** — sistema de alertas dentro de la app

---

## CONFIGURACIÓN LOCAL

### Backend (Spring Boot)

```properties
# Base de datos (Supabase)
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres
spring.datasource.username=postgres.<project_id>
spring.datasource.password=[PASSWORD]

# Supabase Auth
supabase.url=https://<project_id>.supabase.co
supabase.jwt-secret=[JWT_SECRET_DEL_DASHBOARD]

# JWT legacy (mantener durante migración)
jwt.secret=[SECRET_KEY_256_BITS]
jwt.expiration=86400000
jwt.issuer=GastuApp

# JPA
spring.jpa.hibernate.ddl-auto=update
server.port=8080
server.servlet.context-path=/api
```

### Frontend (Angular)

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
  supabaseUrl: "https://<project_id>.supabase.co",
  supabaseAnonKey: "<anon_key>",
};
```

---

## METADATA

**Desarrollador:** Juan Esteban Barrios Portela
**Proyecto:** GastuApp v2.0
**Inicio:** Enero 2025
**Última actualización:** 22 de febrero de 2026

> **NOTA:** Actualizar este documento después de cada fase importante del proyecto.
