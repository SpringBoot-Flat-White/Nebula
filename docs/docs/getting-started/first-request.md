# Tu Primera Petición API

Esta guía te mostrará cómo realizar tus primeras peticiones a la API de Nebula Cloud.

## Herramientas Recomendadas

- **Postman** - Cliente API con interfaz gráfica
- **cURL** - Cliente de línea de comandos
- **HTTPie** - Cliente de línea de comandos amigable
- **Insomnia** - Alternativa a Postman

## Endpoints Públicos

Los siguientes endpoints no requieren autenticación:

### 1. Registrar un Usuario Individual

**Endpoint:** `POST /api/v1/auth/register`

**Body:**
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

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "Password123!",
    "userType": "INDIVIDUAL",
    "firstName": "Juan",
    "lastName": "Pérez",
    "phone": "+57 300 1234567",
    "address": "Calle 123 #45-67",
    "city": "Medellín",
    "country": "Colombia"
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "usuario@example.com",
  "userType": "INDIVIDUAL"
}
```

### 2. Iniciar Sesión

**Endpoint:** `POST /api/v1/auth/authenticate`

**Body:**
```json
{
  "email": "usuario@example.com",
  "password": "Password123!"
}
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "Password123!"
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "usuario@example.com",
  "userType": "INDIVIDUAL"
}
```

## Endpoints Protegidos

Los siguientes endpoints requieren autenticación mediante un token JWT.

### 3. Obtener Perfil de Usuario Individual

**Endpoint:** `GET /api/v1/individuals/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/individuals/1 \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**Respuesta Exitosa (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "email": "usuario@example.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phone": "+57 300 1234567",
  "address": "Calle 123 #45-67",
  "city": "Medellín",
  "country": "Colombia",
  "createdAt": "2025-11-10T10:30:00"
}
```

### 4. Registrar una Organización

**Endpoint:** `POST /api/v1/auth/register`

**Body:**
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

## Manejo de Errores

### Error 400 - Bad Request

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El email ya está registrado",
  "path": "/api/v1/auth/register"
}
```

### Error 401 - Unauthorized

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "path": "/api/v1/individuals/1"
}
```

### Error 404 - Not Found

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Recurso no encontrado",
  "path": "/api/v1/individuals/999"
}
```

## Cookies JWT

La aplicación también soporta autenticación mediante cookies HttpOnly. El token JWT se enviará automáticamente en las cookies después del registro o inicio de sesión.

## Próximos Pasos

- [Explorar todos los endpoints de la API](../api/overview.md)
- [Entender la seguridad JWT](../security/jwt.md)
- [Gestión de organizaciones](../api/organizations.md)

