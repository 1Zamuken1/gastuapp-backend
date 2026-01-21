# CONTEXTO ACTUALIZADO - GASTUAPP V2.0

**Última actualización:** 21 de enero de 2025  
**Estado:** Backend Fase 1 completado - Listo para iniciar Frontend

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
- **Seguridad:** JWT con HS256
- **ORM:** JPA/Hibernate

### Frontend (Por iniciar)
- **Framework:** Angular 20
- **Librería UI:** PrimeNG
- **Estilo:** Calm Design Framework

### IA (Futuro - Fase 3)
- **Provider:** GROQ (Llama 3)
- **Tipo:** AI Agent con function calling
- **Skills:** Implementados como contexto (ver carpeta `/mnt/skills/user/`)

---

## ARQUITECTURA BACKEND

### Estructura de Capas (Hexagonal)

```
src/main/java/com/gastuapp/
├── domain/                          # Capa de Dominio (Core)
│   ├── model/                       # Modelos puros (sin JPA)
│   │   ├── usuario/
│   │   │   ├── Usuario.java
│   │   │   ├── RolUsuario.java (ADMIN, USER, USER_HIJO)
│   │   │   ├── TipologiaUsuario.java
│   │   │   ├── TipoOnboarding.java
│   │   │   └── ConfiguracionUsuario.java
│   │   ├── categoria/
│   │   │   ├── Categoria.java
│   │   │   └── TipoCategoria.java
│   │   └── transaccion/
│   │       ├── Transaccion.java
│   │       └── TipoTransaccion.java (INGRESO, EGRESO)
│   └── port/                        # Interfaces (Ports)
│       ├── usuario/UsuarioRepositoryPort.java
│       ├── categoria/CategoriaRepositoryPort.java
│       └── transaccion/TransaccionRepositoryPort.java
│
├── application/                     # Capa de Aplicación
│   ├── service/                     # Lógica de negocio
│   │   ├── AuthService.java
│   │   ├── UsuarioService.java
│   │   ├── CategoriaService.java
│   │   └── TransaccionService.java
│   ├── mapper/                      # Mappers DTO ↔ Domain
│   │   ├── UsuarioMapper.java
│   │   ├── CategoriaMapper.java
│   │   └── TransaccionMapper.java
│   └── dto/
│       ├── request/                 # DTOs de entrada
│       │   ├── LoginRequestDTO.java
│       │   ├── RegistroRequestDTO.java
│       │   ├── CrearHijoRequestDTO.java
│       │   ├── AdminCrearUsuarioRequestDTO.java
│       │   └── TransaccionRequestDTO.java
│       └── response/                # DTOs de salida
│           ├── AuthResponseDTO.java
│           ├── UsuarioResponseDTO.java
│           ├── CategoriaResponseDTO.java
│           └── TransaccionResponseDTO.java
│
└── infrastructure/                  # Capa de Infraestructura
    ├── adapter/
    │   ├── persistence/             # Persistencia (PostgreSQL)
    │   │   ├── entity/              # Entidades JPA
    │   │   │   ├── UsuarioEntity.java
    │   │   │   ├── ConfiguracionUsuarioEntity.java
    │   │   │   ├── CategoriaEntity.java
    │   │   │   └── TransaccionEntity.java
    │   │   ├── repository/          # Spring Data JPA
    │   │   │   ├── UsuarioJpaRepository.java
    │   │   │   ├── ConfiguracionUsuarioJpaRepository.java
    │   │   │   ├── CategoriaJpaRepository.java
    │   │   │   └── TransaccionJpaRepository.java
    │   │   ├── mapper/              # Mappers Entity ↔ Domain
    │   │   │   ├── UsuarioEntityMapper.java
    │   │   │   ├── ConfiguracionUsuarioEntityMapper.java
    │   │   │   ├── CategoriaEntityMapper.java
    │   │   │   └── TransaccionEntityMapper.java
    │   │   └── UsuarioRepositoryAdapter.java
    │   │       CategoriaRepositoryAdapter.java
    │   │       TransaccionRepositoryAdapter.java
    │   └── rest/                    # REST API
    │       ├── controller/
    │       │   ├── AuthController.java
    │       │   ├── UsuarioController.java
    │       │   ├── CategoriaController.java
    │       │   ├── TransaccionController.java
    │       │   └── HealthController.java
    │       └── exception/           # Manejo de errores
    │           ├── GlobalExceptionHandler.java
    │           ├── ErrorResponse.java
    │           └── UsuarioNotFoundException.java
    ├── config/                      # Configuración
    │   ├── SecurityConfig.java      # Spring Security + JWT
    │   ├── JwtProperties.java       # Propiedades JWT
    │   └── DataSeeder.java          # Seed de categorías
    └── security/
        └── jwt/
            ├── JwtUtils.java        # Generación/validación JWT
            └── JwtAuthenticationFilter.java
```

---

## ROLES Y PERMISOS

### Tipos de Usuario

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **ADMIN** | Administrador del sistema | Gestión de usuarios, sin acceso a datos financieros |
| **USER** | Usuario normal (padre) | Acceso completo + puede crear/supervisar hijos |
| **USER_HIJO** | Usuario hijo (menor) | Acceso restringido, supervisado por padre |

### Reglas de Supervisión Padre-Hijo

**Padre (USER):**
- ✅ Puede ver TODOS los datos de sus hijos
- ✅ Puede crear cuentas de hijos (USER_HIJO)
- ✅ Puede gestionar permisos de sus hijos (Futuro)

**Hijo (USER_HIJO):**
- ❌ NO puede ver datos del padre
- ✅ Solo ve sus propios datos
- ✅ Tiene funciones limitadas según configuración del padre

---

## ESTADO ACTUAL DEL BACKEND

### ✅ COMPLETADO (Commits 1-19)

#### **Commit 1-10: Infraestructura Base**
- ✅ Setup inicial Spring Boot 4.0.1 + PostgreSQL (Supabase)
- ✅ Configuración JPA/Hibernate
- ✅ Arquitectura hexagonal base
- ✅ Health endpoint

#### **Commit 11-15: Módulo Usuario**
- ✅ Domain: Usuario, RolUsuario, TipologiaUsuario
- ✅ Infrastructure: UsuarioEntity + JpaRepository + Adapter
- ✅ Application: DTOs + Mapper + UsuarioService
- ✅ REST: UsuarioController
- ✅ Funcionalidades:
  - Registro público (rol USER automático)
  - Login con email/password
  - CRUD de perfil propio
  - Crear/listar hijos (USER_HIJO)
  - Admin: gestión de usuarios

#### **Commit 16: Autenticación JWT**
- ✅ JwtUtils (generación/validación)
- ✅ JwtAuthenticationFilter
- ✅ SecurityConfig
- ✅ AuthService (login/register)
- ✅ Tokens incluyen: email, publicId, rol, userId
- ✅ Optimización: userId en JWT para evitar consultas a BD

#### **Commit 17-18: Módulo Categorías**
- ✅ Domain: Categoria, TipoCategoria
- ✅ Infrastructure: CategoriaEntity + Repository + Adapter
- ✅ Application: DTOs + Mapper + CategoriaService
- ✅ REST: CategoriaController
- ✅ DataSeeder: 15 categorías predefinidas
  - **Egresos (9):** Comida, Transporte, Salud, Entretenimiento, Educación, Hogar, Ropa, Servicios, Otros gastos
  - **Ingresos (6):** Salario, Freelance, Inversiones, Regalos, Mesada, Otros ingresos

#### **Commit 19: Módulo Transacciones (COMPLETO)**
- ✅ Domain: Transaccion, TipoTransaccion
- ✅ Infrastructure: TransaccionEntity + Repository + Adapter
- ✅ Application: DTOs + Mapper + TransaccionService (10 casos de uso)
- ✅ REST: TransaccionController (10 endpoints)
- ✅ Funcionalidades:
  - CRUD completo de transacciones
  - Filtros: tipo, categoría, rango de fechas
  - Cálculo de balance (ingresos - egresos)
  - Resumen financiero
  - Validación: categoría compatible con tipo
  - Enriquecimiento: ResponseDTO incluye nombre e ícono de categoría

### ⏸️ PENDIENTE EN BACKEND

#### **ConfiguracionUsuario**
- Estructura Domain/Entity/Repository completa
- Falta: Service + Controller + Endpoints
- Endpoints pendientes:
  - GET /api/usuarios/me/configuracion
  - PUT /api/usuarios/me/configuracion

#### **Cuenta Bancaria**
- Todo por implementar
- Relaciones: Usuario (1:N), Transaccion (1:N)

#### **Presupuesto**
- Todo por implementar
- Relaciones: Usuario, Categoria, periodo (mensual/anual)

#### **Meta de Ahorro**
- Todo por implementar
- Progreso calculado automáticamente

#### **Suscripción**
- Todo por implementar
- Detección automática (Fase 2)

#### **Proyección Financiera**
- Todo por implementar (Fase 3)
- Transacciones automáticas basadas en patrones

---

## ENDPOINTS IMPLEMENTADOS

### Base URL
```
http://localhost:8080/api
```

### Autenticación (Público)
```
POST   /auth/register           # Registro público (rol USER)
POST   /auth/login              # Login con JWT
```

### Usuarios (Autenticado)
```
GET    /usuarios/me             # Perfil del usuario autenticado
PUT    /usuarios/me             # Actualizar perfil propio
DELETE /usuarios/me             # Eliminar cuenta propia
GET    /usuarios/{publicId}     # Ver perfil (con validación de permisos)
POST   /usuarios/hijo           # Crear hijo (solo USER)
GET    /usuarios/hijos          # Listar hijos del usuario (solo USER)
```

### Admin (Solo ADMIN)
```
GET    /admin/usuarios                  # Listar todos los usuarios
POST   /admin/usuarios                  # Crear usuario con rol específico
DELETE /admin/usuarios/{publicId}       # Eliminar usuario
```

### Categorías (Autenticado)
```
GET    /categorias                      # Listar categorías predefinidas
GET    /categorias/{id}                 # Obtener categoría por ID
GET    /categorias/tipo/{tipo}          # Listar por tipo (INGRESO/EGRESO)
```

### Transacciones (Autenticado)
```
POST   /transacciones                   # Crear transacción
GET    /transacciones                   # Listar transacciones del usuario
GET    /transacciones/{id}              # Obtener transacción por ID
PUT    /transacciones/{id}              # Actualizar transacción
DELETE /transacciones/{id}              # Eliminar transacción
GET    /transacciones/tipo/{tipo}       # Filtrar por tipo
GET    /transacciones/categoria/{id}    # Filtrar por categoría
GET    /transacciones/rango             # Filtrar por rango de fechas
GET    /transacciones/balance           # Calcular balance del usuario
GET    /transacciones/resumen           # Resumen financiero completo
```

### Health Check
```
GET    /health                          # Estado del servidor
```

---

## SEGURIDAD JWT

### Estructura del Token

**Claims incluidos:**
```json
{
  "sub": "user@example.com",          // Email del usuario
  "publicId": "550e8400-e29b-41d4...", // UUID público
  "rol": "USER",                       // Rol del usuario
  "userId": 123,                       // ID interno (NUEVO - optimización)
  "iat": 1705750000,                   // Issued at
  "exp": 1705836400,                   // Expiration
  "iss": "GastuApp"                    // Issuer
}
```

### Configuración Actual
- **Algoritmo:** HS256
- **Secret:** Configurado en `application.properties`
- **Expiración:** 24 horas (desarrollo)
- **Optimización:** userId en token para evitar consultas a BD

### Flujo de Autenticación
1. Usuario hace login → Backend genera JWT con userId
2. Frontend guarda token en localStorage
3. Cada request incluye header: `Authorization: Bearer <token>`
4. JwtAuthenticationFilter valida token y extrae userId
5. userId se guarda en SecurityContext
6. Controllers obtienen userId directamente (sin consulta a BD)

---

## BASE DE DATOS (PostgreSQL - Supabase)

### Tablas Implementadas

#### **usuarios**
```sql
CREATE TABLE usuarios (
  id BIGSERIAL PRIMARY KEY,
  public_id VARCHAR(36) UNIQUE NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  telefono VARCHAR(20),
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(20) NOT NULL,
  activo BOOLEAN DEFAULT true,
  fecha_creacion TIMESTAMP NOT NULL,
  tipologia VARCHAR(20),
  profesion VARCHAR(255),
  institucion VARCHAR(255),
  tutor_id BIGINT REFERENCES usuarios(id),
  google_id VARCHAR(255) UNIQUE
);
```

#### **configuracion_usuario**
```sql
CREATE TABLE configuracion_usuario (
  id BIGSERIAL PRIMARY KEY,
  usuario_id BIGINT UNIQUE NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  notificaciones_activas BOOLEAN DEFAULT true,
  celebraciones_activas BOOLEAN DEFAULT true,
  onboarding_completado VARCHAR(20) DEFAULT 'NO_COMPLETADO',
  idioma_preferido VARCHAR(5) DEFAULT 'es',
  modo_oscuro BOOLEAN DEFAULT false
);
```

#### **categorias**
```sql
CREATE TABLE categorias (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  icono VARCHAR(50),
  tipo VARCHAR(20) NOT NULL,
  predefinida BOOLEAN DEFAULT false,
  usuario_id BIGINT REFERENCES usuarios(id)
);
```

#### **transacciones**
```sql
CREATE TABLE transacciones (
  id BIGSERIAL PRIMARY KEY,
  monto DECIMAL(15,2) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  descripcion VARCHAR(500) NOT NULL,
  fecha DATE NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL,
  categoria_id BIGINT NOT NULL REFERENCES categorias(id),
  usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
  proyeccion_id BIGINT REFERENCES proyecciones(id),
  es_automatica BOOLEAN DEFAULT false
);
```

### Tablas Pendientes
- `cuentas_bancarias`
- `presupuestos`
- `metas_ahorro`
- `suscripciones`
- `proyecciones` (Fase 3)

---

## CONVENCIONES DEL PROYECTO

### Nomenclatura

**Clases Domain:**
- Modelos: `Usuario.java`, `Transaccion.java`
- Enums: `RolUsuario.java`, `TipoTransaccion.java`
- Ports: `[Entidad]RepositoryPort.java`

**Clases Infrastructure:**
- Entities: `[Entidad]Entity.java`
- Repositories: `[Entidad]JpaRepository.java`
- Adapters: `[Entidad]RepositoryAdapter.java`
- Mappers: `[Entidad]EntityMapper.java`

**Clases Application:**
- Services: `[Entidad]Service.java`
- DTOs Request: `[Operacion]RequestDTO.java`
- DTOs Response: `[Entidad]ResponseDTO.java`
- Mappers: `[Entidad]Mapper.java`

**Clases REST:**
- Controllers: `[Entidad]Controller.java`

### Documentación

**Cada clase debe tener:**
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
 * [Descripción de la responsabilidad]
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since [Fecha]
 */
```

### Commits

**Formato:**
```
feat: [título conciso]

[Descripción estructurada sin emojis]

Funcionalidades:
- Lista de funcionalidades

Arquitectura:
- Detalles de arquitectura

[Otras secciones relevantes]

Validaciones/Optimizaciones/etc.
```

---

## PRÓXIMOS PASOS

### PLAN INMEDIATO: FRONTEND (Commits 20-25)

**Commit 20: Setup Angular 20 + PrimeNG**
- Crear proyecto Angular 20
- Instalar PrimeNG + dependencias
- Configurar estilos globales (Calm Design Framework)
- Configurar routing
- Configurar HttpClient + interceptors JWT

**Commit 21: Módulo de Autenticación**
- Componente Login
- Componente Register
- AuthService (llamadas al backend)
- Guard de autenticación
- Interceptor JWT
- Manejo de tokens en localStorage

**Commit 22: Dashboard Principal**
- Componente Dashboard
- Card de balance actual
- Card de resumen (ingresos/egresos)
- Navegación básica (sidebar/navbar)
- Layout responsive

**Commit 23: Transacciones - Listar**
- Componente lista de transacciones
- PrimeNG DataTable
- Filtros (tipo, categoría, fecha)
- Paginación

**Commit 24: Transacciones - Crear/Editar**
- Dialog crear transacción
- Dialog editar transacción
- Formularios reactivos
- Selector de categorías con íconos

**Commit 25: Transacciones - Eliminar + Balance**
- Confirmación de eliminación
- Visualización de balance
- Gráfico ingresos vs egresos

### DESPUÉS DEL FRONTEND BÁSICO

**Opción A: Volver al Backend**
- ConfiguracionUsuario (endpoints)
- Cuenta Bancaria (completo)
- Presupuesto (completo)

**Opción B: Continuar Frontend**
- Módulo de perfil
- Módulo de categorías personalizadas
- Módulo de cuentas bancarias

---

## CONFIGURACIÓN LOCAL

### Backend (Spring Boot)

**application.properties:**
```properties
# Base de datos (Supabase)
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres
spring.datasource.username=postgres.wqwtgmxvynuruwbusxjh
spring.datasource.password=[TU_PASSWORD]

# JWT
jwt.secret=[TU_SECRET_KEY_256_BITS]
jwt.expiration=86400000
jwt.issuer=GastuApp

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8080
server.servlet.context-path=/api
```

### Frontend (Angular)

**Configuración de entorno:**
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## SKILLS DE IA DISPONIBLES

Tienes 3 skills personalizados implementados en `/mnt/skills/user/`:

1. **ai-agent-function-calling:** Implementación de AI Agent con function calling
2. **postgresql-spring-boot:** Guía de PostgreSQL + JPA
3. **hexagonal-spring-boot:** Arquitectura hexagonal en Spring Boot

Estos skills están disponibles como contexto para Claude y deben ser consultados cuando sean relevantes.

---

## CONTACTO Y METADATA

**Desarrollador:** Juan Esteban Barrios Portela  
**Proyecto:** GastuApp v2.0  
**Inicio:** Enero 2025  
**Estado:** Backend Fase 1 completado - Frontend en inicio  
**Repositorio:** [Pendiente de definir]

---

**NOTA IMPORTANTE:** Este documento debe actualizarse después de cada fase importante del proyecto para mantener el contexto claro en futuros chats.

---

_Última actualización: 21 de enero de 2025 - Post Commit 19_
