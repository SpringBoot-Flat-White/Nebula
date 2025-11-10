# API de Organizaciones

Endpoints para gestionar organizaciones y sus miembros.

## Listar Organizaciones

Obtiene una lista paginada de todas las organizaciones.

### Endpoint

```
GET /api/v1/organizations
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
      "userId": 5,
      "email": "org1@example.com",
      "organizationName": "Tech Solutions S.A.S",
      "taxId": "900123456-7",
      "phone": "+57 300 7654321",
      "address": "Carrera 45 #78-90",
      "city": "Bogotá",
      "country": "Colombia",
      "createdAt": "2025-11-10T10:30:00"
    },
    {
      "id": 2,
      "userId": 6,
      "email": "org2@example.com",
      "organizationName": "Digital Innovations Ltda",
      "taxId": "800987654-3",
      "phone": "+57 300 1112222",
      "address": "Calle 100 #20-30",
      "city": "Medellín",
      "country": "Colombia",
      "createdAt": "2025-11-10T11:00:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0
}
```

---

## Obtener Organización por ID

Obtiene los detalles de una organización específica.

### Endpoint

```
GET /api/v1/organizations/{id}
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
| id | long | ID de la organización |

### Respuesta Exitosa (200 OK)

```json
{
  "id": 1,
  "userId": 5,
  "email": "org1@example.com",
  "organizationName": "Tech Solutions S.A.S",
  "taxId": "900123456-7",
  "phone": "+57 300 7654321",
  "address": "Carrera 45 #78-90",
  "city": "Bogotá",
  "country": "Colombia",
  "createdAt": "2025-11-10T10:30:00",
  "updatedAt": "2025-11-10T10:30:00"
}
```

### Errores

#### 404 - Organización no encontrada

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Organización no encontrada con ID: 999",
  "path": "/api/v1/organizations/999"
}
```

---

## Crear Organización

Crea una nueva organización en el sistema.

### Endpoint

```
POST /api/v1/organizations
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
  "email": "nuevaorg@example.com",
  "password": "Password123!",
  "organizationName": "Cloud Services Corp",
  "taxId": "900555666-7",
  "phone": "+57 300 4445555",
  "address": "Avenida El Dorado #90-10",
  "city": "Bogotá",
  "country": "Colombia"
}
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| email | string | Sí | Email único de la organización |
| password | string | Sí | Contraseña (mínimo 8 caracteres) |
| organizationName | string | Sí | Nombre de la organización |
| taxId | string | No | Número de identificación tributaria |
| phone | string | No | Número de teléfono |
| address | string | No | Dirección |
| city | string | No | Ciudad |
| country | string | No | País |

### Respuesta Exitosa (201 Created)

```json
{
  "id": 3,
  "userId": 15,
  "email": "nuevaorg@example.com",
  "organizationName": "Cloud Services Corp",
  "taxId": "900555666-7",
  "phone": "+57 300 4445555",
  "address": "Avenida El Dorado #90-10",
  "city": "Bogotá",
  "country": "Colombia",
  "createdAt": "2025-11-10T12:00:00"
}
```

---

## Actualizar Organización

Actualiza la información de una organización existente.

### Endpoint

```
PUT /api/v1/organizations/{id}
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
| id | long | ID de la organización |

### Request Body

```json
{
  "organizationName": "Cloud Services Corporation S.A.S",
  "phone": "+57 300 9999999",
  "address": "Nueva Sede - Carrera 7 #32-16",
  "city": "Bogotá",
  "country": "Colombia"
}
```

### Parámetros

Todos los campos son opcionales. Solo se actualizarán los campos proporcionados.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| organizationName | string | Nombre de la organización |
| taxId | string | Número de identificación tributaria |
| phone | string | Número de teléfono |
| address | string | Dirección |
| city | string | Ciudad |
| country | string | País |

### Respuesta Exitosa (200 OK)

```json
{
  "id": 3,
  "userId": 15,
  "email": "nuevaorg@example.com",
  "organizationName": "Cloud Services Corporation S.A.S",
  "taxId": "900555666-7",
  "phone": "+57 300 9999999",
  "address": "Nueva Sede - Carrera 7 #32-16",
  "city": "Bogotá",
  "country": "Colombia",
  "createdAt": "2025-11-10T12:00:00",
  "updatedAt": "2025-11-10T15:30:00"
}
```

---

## Eliminar Organización

Elimina una organización del sistema.

### Endpoint

```
DELETE /api/v1/organizations/{id}
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
| id | long | ID de la organización |

### Respuesta Exitosa (204 No Content)

Sin contenido en el body.

---

## Agregar Miembro a Organización

Agrega un usuario como miembro de una organización.

### Endpoint

```
POST /api/v1/organizations/{id}/members
```

### Acceso

🔒 Requiere autenticación (propietario de organización o admin)

### Headers

```
Authorization: Bearer {token}
Content-Type: application/json
```

### Path Parameters

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la organización |

### Request Body

```json
{
  "userId": 10,
  "role": "MEMBER",
  "permissions": ["READ", "WRITE"]
}
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| userId | long | Sí | ID del usuario a agregar |
| role | string | Sí | Rol del miembro: OWNER, ADMIN, MEMBER |
| permissions | array | No | Lista de permisos específicos |

### Respuesta Exitosa (201 Created)

```json
{
  "id": 5,
  "organizationId": 3,
  "userId": 10,
  "userEmail": "usuario@example.com",
  "role": "MEMBER",
  "permissions": ["READ", "WRITE"],
  "joinedAt": "2025-11-10T16:00:00"
}
```

---

## Listar Miembros de Organización

Obtiene la lista de todos los miembros de una organización.

### Endpoint

```
GET /api/v1/organizations/{id}/members
```

### Acceso

🔒 Requiere autenticación (miembro de organización o admin)

### Headers

```
Authorization: Bearer {token}
```

### Path Parameters

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la organización |

### Respuesta Exitosa (200 OK)

```json
[
  {
    "id": 1,
    "organizationId": 3,
    "userId": 15,
    "userEmail": "owner@example.com",
    "role": "OWNER",
    "permissions": ["ALL"],
    "joinedAt": "2025-11-10T12:00:00"
  },
  {
    "id": 5,
    "organizationId": 3,
    "userId": 10,
    "userEmail": "usuario@example.com",
    "role": "MEMBER",
    "permissions": ["READ", "WRITE"],
    "joinedAt": "2025-11-10T16:00:00"
  }
]
```

---

## Ejemplos con cURL

### Listar Organizaciones

```bash
curl -X GET "http://localhost:8080/api/v1/organizations?page=0&size=10" \
  -H "Authorization: Bearer TU_TOKEN"
```

### Obtener Organización por ID

```bash
curl -X GET http://localhost:8080/api/v1/organizations/1 \
  -H "Authorization: Bearer TU_TOKEN"
```

### Crear Organización

```bash
curl -X POST http://localhost:8080/api/v1/organizations \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevaorg@example.com",
    "password": "Password123!",
    "organizationName": "Cloud Services Corp",
    "taxId": "900555666-7"
  }'
```

### Actualizar Organización

```bash
curl -X PUT http://localhost:8080/api/v1/organizations/3 \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+57 300 9999999",
    "city": "Bogotá"
  }'
```

### Agregar Miembro

```bash
curl -X POST http://localhost:8080/api/v1/organizations/3/members \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10,
    "role": "MEMBER"
  }'
```

### Listar Miembros

```bash
curl -X GET http://localhost:8080/api/v1/organizations/3/members \
  -H "Authorization: Bearer TU_TOKEN"
```

## Ver También

- [API de Autenticación](./authentication.md)
- [API de Usuarios Individuales](./individuals.md)
- [Modelo de Organización](../models/organization.md)

