# API de Autenticación

Endpoints para el registro, autenticación y gestión de sesiones de usuarios.

## Registrar Usuario

Crea una nueva cuenta de usuario (Individual u Organización).

### Endpoint

```
POST /api/v1/auth/register
```

### Acceso

🔓 Público - No requiere autenticación

### Request Body - Usuario Individual

```json
{
  "email": "usuario@example.com",
  "password": "Password123!",
  "userType": "INDIVIDUAL",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phone": "+57 300 1234567",
  "address": "Calle 123 #45-67",
  "city": "Medellín",
  "country": "Colombia"
}
```

### Request Body - Organización

```json
{
  "email": "organizacion@example.com",
  "password": "Password123!",
  "userType": "ORGANIZATION",
  "organizationName": "Tech Solutions S.A.S",
  "taxId": "900123456-7",
  "phone": "+57 300 7654321",
  "address": "Carrera 45 #78-90",
  "city": "Bogotá",
  "country": "Colombia"
}
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| email | string | Sí | Email único del usuario (formato válido) |
| password | string | Sí | Contraseña (mínimo 8 caracteres) |
| userType | enum | Sí | Tipo de usuario: `INDIVIDUAL` o `ORGANIZATION` |
| firstName | string | Si INDIVIDUAL | Nombre del usuario |
| lastName | string | Si INDIVIDUAL | Apellido del usuario |
| organizationName | string | Si ORGANIZATION | Nombre de la organización |
| taxId | string | No | Número de identificación tributaria |
| phone | string | No | Número de teléfono |
| address | string | No | Dirección |
| city | string | No | Ciudad |
| country | string | No | País |

### Respuesta Exitosa (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "email": "usuario@example.com",
  "userType": "INDIVIDUAL"
}
```

### Cookies Establecidas

```
Set-Cookie: token=eyJhbGci...; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=86400
```

### Errores

#### 400 - Email ya registrado

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El email ya está registrado",
  "path": "/api/v1/auth/register"
}
```

#### 400 - Validación fallida

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La contraseña debe tener al menos 8 caracteres",
  "path": "/api/v1/auth/register"
}
```

---

## Iniciar Sesión

Autentica a un usuario existente y obtiene un token JWT.

### Endpoint

```
POST /api/v1/auth/authenticate
```

### Acceso

🔓 Público - No requiere autenticación

### Request Body

```json
{
  "email": "usuario@example.com",
  "password": "Password123!"
}
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| email | string | Sí | Email del usuario |
| password | string | Sí | Contraseña del usuario |

### Respuesta Exitosa (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "usuario@example.com",
  "userType": "INDIVIDUAL"
}
```

### Cookies Establecidas

```
Set-Cookie: token=eyJhbGci...; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=86400
```

### Errores

#### 401 - Credenciales inválidas

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Email o contraseña incorrectos",
  "path": "/api/v1/auth/authenticate"
}
```

---

## Cerrar Sesión

Invalida el token JWT actual y limpia la cookie de sesión.

### Endpoint

```
POST /api/v1/auth/logout
```

### Acceso

🔒 Requiere autenticación

### Headers

```
Authorization: Bearer {token}
```

### Respuesta Exitosa (200 OK)

```json
{
  "message": "Logout exitoso"
}
```

### Cookies Eliminadas

```
Set-Cookie: token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0
```

---

## Ejemplo con cURL

### Registro

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "password": "Password123!",
    "userType": "INDIVIDUAL",
    "firstName": "María",
    "lastName": "García"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "nuevo@example.com",
    "password": "Password123!"
  }'
```

### Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer TU_TOKEN" \
  -b cookies.txt
```

## Notas de Seguridad

- ✅ Las contraseñas se hashean con BCrypt antes de almacenarse
- ✅ Los tokens JWT tienen una expiración de 24 horas
- ✅ Las cookies son HttpOnly, Secure y SameSite=Strict
- ✅ El endpoint de logout invalida el token actual
- ⚠️ Se recomienda usar HTTPS en producción

## Ver También

- [Seguridad JWT](../security/jwt.md)
- [Gestión de Usuarios](./individuals.md)
- [Gestión de Organizaciones](./organizations.md)

