# API de Usuarios Individuales

Endpoints para gestionar usuarios individuales en el sistema.

## Listar Usuarios Individuales

Obtiene una lista paginada de todos los usuarios individuales.

### Endpoint

```
GET /api/v1/individuals
```

### Acceso

🔒 Requiere autenticación

### Headers

```
Authorization: Bearer {token}
```

### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| page | integer | No | 0 | Número de página (base 0) |
| size | integer | No | 20 | Elementos por página |
| sort | string | No | id,asc | Campo y dirección de ordenamiento |

### Respuesta Exitosa (200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "email": "usuario1@example.com",
      "firstName": "Juan",
      "lastName": "Pérez",
      "phone": "+57 300 1234567",
      "address": "Calle 123 #45-67",
      "city": "Medellín",
      "country": "Colombia",
      "createdAt": "2025-11-10T10:30:00"
    },
    {
      "id": 2,
      "userId": 2,
      "email": "usuario2@example.com",
      "firstName": "María",
      "lastName": "García",
      "phone": "+57 300 7654321",
      "address": "Carrera 45 #78-90",
      "city": "Bogotá",
      "country": "Colombia",
      "createdAt": "2025-11-10T11:00:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

## Obtener Usuario Individual por ID

Obtiene los detalles de un usuario individual específico.

### Endpoint

```
GET /api/v1/individuals/{id}
```

### Acceso

🔒 Requiere autenticación

### Headers

```
Authorization: Bearer {token}
```

### Path Parameters

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del usuario individual |

### Respuesta Exitosa (200 OK)

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
  "createdAt": "2025-11-10T10:30:00",
  "updatedAt": "2025-11-10T10:30:00"
}
```

### Errores

#### 404 - Usuario no encontrado

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario individual no encontrado con ID: 999",
  "path": "/api/v1/individuals/999"
}
```

---

## Crear Usuario Individual

Crea un nuevo usuario individual en el sistema.

### Endpoint

```
POST /api/v1/individuals
```

### Acceso

🔒 Requiere autenticación (Admin)

### Headers

```
Authorization: Bearer {token}
Content-Type: application/json
```

### Request Body

```json
{
  "email": "nuevo@example.com",
  "password": "Password123!",
  "firstName": "Carlos",
  "lastName": "Rodríguez",
  "phone": "+57 300 9876543",
  "address": "Avenida 80 #32-15",
  "city": "Cali",
  "country": "Colombia"
}
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| email | string | Sí | Email único del usuario |
| password | string | Sí | Contraseña (mínimo 8 caracteres) |
| firstName | string | Sí | Nombre del usuario |
| lastName | string | Sí | Apellido del usuario |
| phone | string | No | Número de teléfono |
| address | string | No | Dirección |
| city | string | No | Ciudad |
| country | string | No | País |

### Respuesta Exitosa (201 Created)

```json
{
  "id": 3,
  "userId": 10,
  "email": "nuevo@example.com",
  "firstName": "Carlos",
  "lastName": "Rodríguez",
  "phone": "+57 300 9876543",
  "address": "Avenida 80 #32-15",
  "city": "Cali",
  "country": "Colombia",
  "createdAt": "2025-11-10T12:00:00"
}
```

---

## Actualizar Usuario Individual

Actualiza la información de un usuario individual existente.

### Endpoint

```
PUT /api/v1/individuals/{id}
```

### Acceso

🔒 Requiere autenticación (propietario o admin)

### Headers

```
Authorization: Bearer {token}
Content-Type: application/json
```

### Path Parameters

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del usuario individual |

### Request Body

```json
{
  "firstName": "Carlos Alberto",
  "lastName": "Rodríguez Gómez",
  "phone": "+57 300 9999999",
  "address": "Nueva Dirección 123",
  "city": "Medellín",
  "country": "Colombia"
}
```

### Parámetros

Todos los campos son opcionales. Solo se actualizarán los campos proporcionados.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| firstName | string | Nombre del usuario |
| lastName | string | Apellido del usuario |
| phone | string | Número de teléfono |
| address | string | Dirección |
| city | string | Ciudad |
| country | string | País |

### Respuesta Exitosa (200 OK)

```json
{
  "id": 3,
  "userId": 10,
  "email": "nuevo@example.com",
  "firstName": "Carlos Alberto",
  "lastName": "Rodríguez Gómez",
  "phone": "+57 300 9999999",
  "address": "Nueva Dirección 123",
  "city": "Medellín",
  "country": "Colombia",
  "createdAt": "2025-11-10T12:00:00",
  "updatedAt": "2025-11-10T15:30:00"
}
```

---

## Eliminar Usuario Individual

Elimina un usuario individual del sistema.

### Endpoint

```
DELETE /api/v1/individuals/{id}
```

### Acceso

🔒 Requiere autenticación (Admin)

### Headers

```
Authorization: Bearer {token}
```

### Path Parameters

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del usuario individual |

### Respuesta Exitosa (204 No Content)

Sin contenido en el body.

### Errores

#### 403 - No autorizado

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permisos para eliminar este usuario",
  "path": "/api/v1/individuals/3"
}
```

---

## Ejemplos con cURL

### Listar Usuarios

```bash
curl -X GET "http://localhost:8080/api/v1/individuals?page=0&size=10" \
  -H "Authorization: Bearer TU_TOKEN"
```

### Obtener Usuario por ID

```bash
curl -X GET http://localhost:8080/api/v1/individuals/1 \
  -H "Authorization: Bearer TU_TOKEN"
```

### Crear Usuario

```bash
curl -X POST http://localhost:8080/api/v1/individuals \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "password": "Password123!",
    "firstName": "Carlos",
    "lastName": "Rodríguez"
  }'
```

### Actualizar Usuario

```bash
curl -X PUT http://localhost:8080/api/v1/individuals/3 \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+57 300 9999999",
    "city": "Medellín"
  }'
```

### Eliminar Usuario

```bash
curl -X DELETE http://localhost:8080/api/v1/individuals/3 \
  -H "Authorization: Bearer TU_TOKEN"
```

## Ver También

- [API de Autenticación](./authentication.md)
- [API de Organizaciones](./organizations.md)
- [Modelo de Usuario Individual](../models/individual.md)

