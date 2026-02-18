# GastuApp - Seguridad Supabase

## Problemas de Seguridad Detectados

Supabase ha detectado los siguientes problemas de seguridad en la base de datos:

### 1. Row Level Security (RLS) Deshabilitado

**Tablas afectadas:**
- `public.usuarios`
- `public.configuracion_usuario`
- `public.categorias`
- `public.transacciones`
- `public.metas_ahorro`
- `public.ahorros`
- `public.cuotas_ahorro`
- `public.proyecciones`
- `public.presupuestos_planificaciones`

**Descripción:** Las tablas están marcadas como públicas pero RLS no está habilitado, lo que significa que cualquier persona con acceso a la base de datos puede leer/escribir sin restricciones.

### 2. Columna Sensible Expuesta

**Tabla:** `public.usuarios`  
**Columna:** `password`  
**Descripción:** La columna de password está expuesta en una tabla accesible vía API pública.

---

## Arquitectura Actual

```
┌─────────────┐      JWT (propio)      ┌──────────────────┐
│   Frontend  │ ─────────────────────► │   Spring Boot   │
│  (Angular)  │                        │   (Auth + API)  │
└─────────────┘                        └────────┬─────────┘
                                               │
                                    Connection Pooler
                                               │
                                               ▼
                                    ┌──────────────────┐
                                    │    Supabase      │
                                    │  (PostgreSQL)    │
                                    │  Solo Storage    │
                                    └──────────────────┘
```

**Problema:** El connection pooler de Supabase crea conexiones "trusted", por lo que RLS no funciona correctamente y todas las operaciones parecen autorizadas.

---

## Solución Recomendada: Migrar a Supabase Auth

Esta solución implementa **defense in depth** con dos capas de seguridad:

```
┌─────────────┐      Supabase JWT      ┌──────────────────┐
│   Frontend  │ ─────────────────────► │   Spring Boot    │
│  (Angular)  │                        │  (Validación JWT) │
└─────────────┘                        └────────┬─────────┘
                                               │
                                    JWKS (Supabase)
                                               │
                                    ┌──────────▼──────────┐
                                    │     Supabase       │
                                    │  (Auth + Storage)  │
                                    │  RLS con auth.uid()│
                                    └────────────────────┘
```

### Flujo de Autenticación

1. **Login:** Frontend → Supabase Auth API → Obtiene JWT
2. **Request:** Frontend → Spring Boot (JWT en header)
3. **Validación App:** Spring Boot → Valida JWT contra JWKS de Supabase
4. **Validación DB:** Supabase → RLS verifica `auth.uid() = user_id`

---

## Pasos de Implementación

### Paso 1: Configurar Supabase

#### 1.1 Obtener credenciales

Desde el dashboard de Supabase:
- **Project Settings → API**
- Copiar: `Project URL`, `anon key`, `JWT secret`

#### 1.2 Habilitar Row Level Security

Ejecutar en SQL Editor:

```sql
-- Habilitar RLS en todas las tablas
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE configuracion_usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE transacciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE metas_ahorro ENABLE ROW LEVEL SECURITY;
ALTER TABLE ahorros ENABLE ROW LEVEL SECURITY;
ALTER TABLE cuotas_ahorro ENABLE ROW LEVEL SECURITY;
ALTER TABLE proyecciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE presupuestos_planificaciones ENABLE ROW LEVEL SECURITY;
```

#### 1.3 Crear políticas RLS

```sql
-- Política para usuarios: solo el propio usuario puede ver sus datos
CREATE POLICY "usuarios_own_data" ON usuarios
FOR ALL
USING (auth.uid()::text = id::text)
WITH CHECK (auth.uid()::text = id::text);

-- Política para transacciones
CREATE POLICY "transacciones_own_data" ON transacciones
FOR ALL
USING (auth.uid()::text = usuario_id::text)
WITH CHECK (auth.uid()::text = usuario_id::text);

-- Repite para otras tablas...
```

> **Nota:** La columna `usuario_id` debe existir en cada tabla. Si no existe, crearla o ajustar la política.

---

### Paso 2: Backend - Spring Boot

#### 2.1 Agregar configuración de Supabase

En `application.properties`:

```properties
# Supabase Configuration
supabase.url=https://<PROJECT_ID>.supabase.co
supabase.anon-key=<ANON_KEY>
supabase.jwt-secret=<JWT_SECRET>
```

#### 2.2 Crear JwtUtils para Supabase

Reemplazar `JwtUtils.java` actual para validar contra JWKS de Supabase:

```java
package com.gastuapp.infrastructure.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Component
public class SupabaseJwtUtils {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    private static final String JWKS_URL = "/auth/v1/.well-known/jwks.json";

    public boolean validateToken(String token) {
        try {
            // Método 1: Validar con JWT Secret (más rápido para legacy)
            SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            
            return true;
        } catch (Exception e) {
            // Método 2: Validar contra JWKS (para nuevos tokens asymmetric)
            return validateWithJwks(token);
        }
    }

    private boolean validateWithJwks(String token) {
        // Implementar validación con JWKS si es necesario
        // Para la mayoría de casos, el JWT secret basta
        return false;
    }

    public String getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.getSubject(); // user_id en Supabase
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
```

#### 2.3 Modificar JwtAuthenticationFilter

El filtro actual ya extrae el token. Solo necesita apuntar al nuevo `SupabaseJwtUtils`:

```java
// Cambiar de:
// private final JwtUtils jwtUtils;
// A:
// private final SupabaseJwtUtils jwtUtils;
```

**Importante:** El filtro ya no necesita consultar la base de datos para obtener el usuario. El token de Supabase ya contiene el `sub` (user_id).

#### 2.4 Eliminar AuthService.register() y AuthService.login()

Estos endpoints ya no son necesarios porque la autenticación ocurre en Supabase:

- ~~`POST /api/auth/register`~~ → Usar Supabase Auth API
- ~~`POST /api/auth/login`~~ → Usar Supabase Auth API

**Mantener:**
- `GET /api/auth/validate` → Para verificar token (opcional)

---

### Paso 3: Frontend - Angular

#### 3.1 Instalar cliente de Supabase

```bash
npm install @supabase/supabase-js
```

#### 3.2 Configurar cliente

```typescript
// src/app/core/config/supabase.ts
import { createClient } from '@supabase/supabase-js';

export const supabase = createClient(
  environment.supabaseUrl,
  environment.supabaseAnonKey
);
```

#### 3.3 Modificar AuthService

Cambiar los métodos de login/register:

```typescript
// Antes: Llamaba a Spring Boot
login(email: string, password: string): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, {
    email,
    password
  });
}

// Después: Llama a Supabase Auth
async login(email: string, password: string): Promise<{ data, error }> {
  return supabase.auth.signInWithPassword({ email, password });
}

async register(email: string, password: string): Promise<{ data, error }> {
  return supabase.auth.signUp({ email, password });
}
```

#### 3.4 Manejar respuesta

```typescript
// Extraer token y enviar a Spring Boot
const { data, error } = await supabase.auth.signInWithPassword({
  email, password
});

if (data.session) {
  const token = data.session.access_token;
  // Guardar token y usar en interceptor HTTP
}
```

#### 3.5 Actualizar interceptor JWT

El interceptor ya funciona igual, solo cambia de dónde viene el token:

```typescript
// interceptor ya usa: request.clone({ setHeaders: ... })
// Solo necesita obtener el token de Supabase en lugar de Spring Boot
```

---

### Paso 4: Sincronizar Usuarios

Si ya tienes usuarios registrados, necesitas sincronizarlos:

```sql
-- Supabase Auth usa auth.users
-- Nuestra tabla usuarios tiene su propio ID

-- Opción 1: Mantener sincronización manual
-- Crear trigger en Supabase para crear registro en tabla usuarios

-- Opción 2: Migrar usuarios existentes
-- Exportar usuarios → Importar a Supabase Auth
-- Mapear auth.users.id con nuestra tabla usuarios
```

**Recomendación:** Para simplificar, se puede mantener la tabla `usuarios` local pero usar `auth.uid()` de Supabase para RLS. Agregar columna `supabase_uid` a la tabla.

---

## Comparación de Soluciones

| Aspecto | Opción A (Solo Spring) | Opción B (Supabase Auth) |
|---------|------------------------|--------------------------|
| **Complejidad** | Baja | Media |
| **Tiempo** | 5 minutos | 2-4 horas |
| **Seguridad** | Una capa | Dos capas (App + DB) |
| **RLS** | No funciona | Funciona correctamente |
| **Password** | Expuesta via API | Protegida por Supabase |

---

## Riesgos de Mantener Solo Spring Boot JWT

1. **Acceso directo a DB:** Cualquiera con credentials de conexión puede ver todos los datos
2. **Sin RLS:** No hay protección a nivel de base de datos
3. **Password expuesta:** Columna visible en tabla pública
4. **Single point of failure:** Si JWT es comprometido, todo está expuesto

---

## Rollback (si algo sale mal)

Si la migración a Supabase Auth falla:

1. **Deshabilitar RLS temporalmente:**
   ```sql
   ALTER TABLE usuarios DISABLE ROW LEVEL SECURITY;
   -- Repetir para todas las tablas
   ```

2. **Revertir cambios en código:**
   - Restaurar `JwtUtils` original
   - Restaurar `AuthService.login/register`
   - Restaurar Frontend AuthService

3. **Probar nuevamente en staging antes de producción**

---

## Recursos

- [Supabase Auth Documentation](https://supabase.com/docs/guides/auth)
- [Supabase JWT Verification](https://supabase.com/docs/guides/auth/jwts)
- [Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)

---

*Documento creado para planificar la migración de seguridad.*
*Fecha: Febrero 2026*
