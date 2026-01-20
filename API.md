# 📚 GastuApp - Documentación de API REST

**Base URL**: `http://localhost:8080/api`

**Versión**: 1.0.0

**Última actualización**: 2025-01-20

---

## 🔐 Autenticación

### POST /auth/register
Registra un nuevo usuario en el sistema (público).

**Rol asignado**: `USER` (automático)

**Request Body**:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "telefono": "3001234567",
  "password": "password123",
  "tipologia": "TRABAJADOR",
  "profesion": "Ingeniero de Software",
  "institucion": "Tech Corp"
}
```

**Response** (201 Created):
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "telefono": "3001234567",
  "rol": "USER",
  "tipologia": "TRABAJADOR",
  "activo": true,
  "fechaCreacion": "2025-01-20T10:30:00",
  "profesion": "Ingeniero de Software",
  "institucion": "Tech Corp",
  "tutorId": null,
  "googleId": null
}
```

**Errores**:
- `400 Bad Request`: Validaciones fallidas (email inválido, campos vacíos)
- `400 Bad Request`: Email ya registrado
- `400 Bad Request`: Teléfono ya registrado

---

### POST /auth/login
Inicia sesión y retorna JWT token.

**Request Body**:
```json
{
  "email": "juan@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600,
  "usuario": {
    "publicId": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan@example.com",
    "rol": "USER"
  }
}
```

**Errores**:
- `400 Bad Request`: Email o password vacíos
- `401 Unauthorized`: Credenciales inválidas
- `404 Not Found`: Usuario no existe

---

## 👥 Usuarios

### GET /usuarios/me
Obtiene el perfil del usuario autenticado.

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "telefono": "3001234567",
  "rol": "USER",
  "tipologia": "TRABAJADOR",
  "activo": true,
  "fechaCreacion": "2025-01-20T10:30:00",
  "profesion": "Ingeniero de Software",
  "institucion": "Tech Corp",
  "tutorId": null
}
```

**Errores**:
- `401 Unauthorized`: Token inválido o expirado
- `404 Not Found`: Usuario no encontrado

---

### POST /usuarios/hijo
Crea un usuario hijo supervisado (requiere autenticación).

**Rol del usuario autenticado**: `USER`

**Rol asignado al hijo**: `USER_HIJO` (automático)

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "nombre": "María",
  "apellido": "Pérez",
  "email": "maria@example.com",
  "telefono": "3009876543",
  "password": "password123",
  "profesion": "Estudiante de secundaria",
  "institucion": "Colegio San José"
}
```

**Response** (201 Created):
```json
{
  "publicId": "660e8400-e29b-41d4-a716-446655440001",
  "nombre": "María",
  "apellido": "Pérez",
  "email": "maria@example.com",
  "telefono": "3009876543",
  "rol": "USER_HIJO",
  "tipologia": "ESTUDIANTE",
  "activo": true,
  "fechaCreacion": "2025-01-20T11:00:00",
  "profesion": "Estudiante de secundaria",
  "institucion": "Colegio San José",
  "tutorId": 1
}
```

**Errores**:
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: Usuario no tiene rol USER
- `400 Bad Request`: Email ya registrado
- `400 Bad Request`: Validaciones fallidas

---

### GET /usuarios/hijos
Lista todos los hijos del usuario autenticado.

**Rol requerido**: `USER`

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
[
  {
    "publicId": "660e8400-e29b-41d4-a716-446655440001",
    "nombre": "María",
    "apellido": "Pérez",
    "email": "maria@example.com",
    "rol": "USER_HIJO",
    "tipologia": "ESTUDIANTE",
    "activo": true,
    "tutorId": 1
  },
  {
    "publicId": "770e8400-e29b-41d4-a716-446655440002",
    "nombre": "Carlos",
    "apellido": "Pérez",
    "email": "carlos@example.com",
    "rol": "USER_HIJO",
    "tipologia": "ESTUDIANTE",
    "activo": true,
    "tutorId": 1
  }
]
```

**Errores**:
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Usuario no tiene rol USER

---

### PUT /usuarios/me
Actualiza los datos del usuario autenticado.

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body** (todos los campos son opcionales):
```json
{
  "nombre": "Juan Carlos",
  "apellido": "Pérez García",
  "telefono": "3001234568"
}
```

**Response** (200 OK):
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan Carlos",
  "apellido": "Pérez García",
  "email": "juan@example.com",
  "telefono": "3001234568",
  "rol": "USER",
  "activo": true
}
```

**Errores**:
- `401 Unauthorized`: No autenticado
- `400 Bad Request`: Teléfono ya en uso por otro usuario
- `400 Bad Request`: Validaciones fallidas

---

### DELETE /usuarios/me
Elimina la cuenta del usuario autenticado (soft delete).

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (204 No Content)

**Errores**:
- `401 Unauthorized`: No autenticado

---

## 🔒 Admin (Solo ADMIN)

### GET /admin/usuarios
Lista todos los usuarios activos del sistema.

**Rol requerido**: `ADMIN`

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
[
  {
    "publicId": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Juan",
    "email": "juan@example.com",
    "rol": "USER",
    "activo": true,
    "fechaCreacion": "2025-01-20T10:30:00"
  },
  {
    "publicId": "660e8400-e29b-41d4-a716-446655440001",
    "nombre": "María",
    "email": "maria@example.com",
    "rol": "USER_HIJO",
    "activo": true,
    "fechaCreacion": "2025-01-20T11:00:00"
  }
]
```

**Errores**:
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Usuario no tiene rol ADMIN

---

### POST /admin/usuarios
Crea un usuario con rol específico (solo ADMIN).

**Rol requerido**: `ADMIN`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "nombre": "Carlos",
  "apellido": "Admin",
  "email": "carlos@gastuapp.com",
  "telefono": "3001234567",
  "password": "admin123",
  "rol": "ADMIN",
  "tipologia": "TRABAJADOR",
  "profesion": "Administrador del Sistema",
  "institucion": "GastuApp",
  "tutorId": null
}
```

**Response** (201 Created):
```json
{
  "publicId": "880e8400-e29b-41d4-a716-446655440003",
  "nombre": "Carlos",
  "apellido": "Admin",
  "email": "carlos@gastuapp.com",
  "rol": "ADMIN",
  "activo": true,
  "fechaCreacion": "2025-01-20T12:00:00"
}
```

**Errores**:
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Usuario no tiene rol ADMIN
- `400 Bad Request`: Email ya registrado
- `400 Bad Request`: Validaciones fallidas

---

### DELETE /admin/usuarios/{publicId}
Elimina un usuario del sistema (soft delete).

**Rol requerido**: `ADMIN`

**Headers**:
```
Authorization: Bearer {token}
```

**URL Parameters**:
- `publicId`: UUID del usuario a eliminar

**Response** (204 No Content)

**Errores**:
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Usuario no tiene rol ADMIN
- `404 Not Found`: Usuario no existe

---

## 📊 Códigos de Estado HTTP

| Código | Significado | Cuándo se usa |
|--------|-------------|---------------|
| 200 | OK | Petición exitosa (GET, PUT) |
| 201 | Created | Recurso creado exitosamente (POST) |
| 204 | No Content | Operación exitosa sin contenido (DELETE) |
| 400 | Bad Request | Validaciones fallidas, datos inválidos |
| 401 | Unauthorized | Token inválido, expirado o no enviado |
| 403 | Forbidden | Usuario no tiene permisos para esta operación |
| 404 | Not Found | Recurso no encontrado |
| 500 | Internal Server Error | Error inesperado del servidor |
| 501 | Not Implemented | Endpoint no implementado aún |

---

## 🔑 Autenticación con JWT

### Formato del Token
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Expiración
- **Desarrollo**: 24 horas
- **Producción**: 1 hora (recomendado)

### Claims del Token
```json
{
  "sub": "juan@example.com",
  "userId": 1,
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "rol": "USER",
  "iat": 1705750000,
  "exp": 1705836400
}
```

---

## 🧪 Credenciales de Demo (Seeder)
```
Admin:
  Email: admin@gastuapp.com
  Password: admin123

Usuario Normal (Padre):
  Email: juan@demo.com
  Password: demo123

Usuario Hijo:
  Email: maria@demo.com
  Password: demo123
```

---

## 📝 Notas Importantes

1. **IDs Públicos**: Todos los endpoints usan `publicId` (UUID) en lugar de IDs internos por seguridad.

2. **Passwords**: Nunca se retornan en las respuestas. Siempre hasheados con BCrypt en BD.

3. **Soft Delete**: Los usuarios eliminados se marcan como `activo: false`, no se borran físicamente.

4. **Roles Automáticos**:
   - Registro público → `USER`
   - Crear hijo → `USER_HIJO`
   - Solo ADMIN puede asignar roles manualmente

5. **Validaciones**: Todos los endpoints validan datos con Jakarta Validation antes de procesar.

---

## 🚀 Ejemplos de Uso (cURL)

### Registrar usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan@example.com",
    "password": "password123",
    "telefono": "3001234567",
    "tipologia": "TRABAJADOR"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "password123"
  }'
```

### Obtener perfil (con token)
```bash
curl -X GET http://localhost:8080/api/usuarios/me \
  -H "Authorization: Bearer {tu_token_aqui}"
```

---

## 📞 Contacto y Soporte

**Desarrollador**: Juan Esteban Barrios Portela  
**Proyecto**: GastuApp v2.0  
**GitHub**: [tu-usuario/gastuapp-backend]  
**Fecha**: Enero 2025

---

_Última actualización: 2025-01-20_
```

---

## 📝 También crear un `ENDPOINTS.txt` (versión rápida)

Para referencia ultra-rápida:

**Ubicación**: `ENDPOINTS.txt` (raíz del proyecto)
```
===========================================
  GASTUAPP - ENDPOINTS RÁPIDOS
===========================================

BASE URL: http://localhost:8080/api

===========================================
AUTH (Público)
===========================================
POST   /auth/register     → Registro público (rol USER)
POST   /auth/login        → Login (retorna JWT)

===========================================
USUARIOS (Autenticado)
===========================================
GET    /usuarios/me       → Perfil del usuario
PUT    /usuarios/me       → Actualizar perfil
DELETE /usuarios/me       → Eliminar cuenta
POST   /usuarios/hijo     → Crear hijo (requiere rol USER)
GET    /usuarios/hijos    → Listar hijos del usuario

===========================================
ADMIN (Solo ADMIN)
===========================================
GET    /admin/usuarios           → Listar todos los usuarios
POST   /admin/usuarios           → Crear usuario con rol específico
DELETE /admin/usuarios/{publicId} → Eliminar usuario

===========================================
DEMO CREDENTIALS
===========================================
Admin:    admin@gastuapp.com / admin123
Usuario:  juan@demo.com / demo123
Hijo:     maria@demo.com / demo123

===========================================
HEADERS
===========================================
Content-Type: application/json
Authorization: Bearer {token}