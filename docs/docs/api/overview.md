# API Overview

Bienvenido a la referencia completa de la API de Nebula Cloud. Esta sección documenta todos los endpoints disponibles, sus parámetros y respuestas.

## Base URL

```
http://localhost:8080/api/v1
```

## Autenticación

La mayoría de los endpoints requieren autenticación mediante **JWT (JSON Web Token)**. Hay dos formas de enviar el token:

### 1. Header Authorization (Recomendado)

```
Authorization: Bearer {token}
```

### 2. Cookie HttpOnly

El token se envía automáticamente en una cookie segura después del login.

## Estructura de la API

La API está organizada en los siguientes módulos:

### 🔐 Autenticación

- `POST /auth/register` - Registrar nuevo usuario
- `POST /auth/authenticate` - Iniciar sesión
- `POST /auth/logout` - Cerrar sesión

[Ver documentación completa →](./authentication.md)

### 👤 Usuarios Individuales

- `GET /individuals` - Listar usuarios individuales
- `GET /individuals/{id}` - Obtener usuario por ID
- `POST /individuals` - Crear usuario individual
- `PUT /individuals/{id}` - Actualizar usuario
- `DELETE /individuals/{id}` - Eliminar usuario

[Ver documentación completa →](./individuals.md)

### 🏢 Organizaciones

- `GET /organizations` - Listar organizaciones
- `GET /organizations/{id}` - Obtener organización por ID
- `POST /organizations` - Crear organización
- `PUT /organizations/{id}` - Actualizar organización
- `DELETE /organizations/{id}` - Eliminar organización
- `POST /organizations/{id}/members` - Agregar miembro
- `GET /organizations/{id}/members` - Listar miembros

[Ver documentación completa →](./organizations.md)

## Formatos de Respuesta

### Respuesta Exitosa

```json
{
  "data": {
    // ... objeto de respuesta
  },
  "status": 200,
  "message": "Success"
}
```

### Respuesta de Error

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/v1/endpoint"
}
```

## Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Petición exitosa |
| 201 | Created - Recurso creado exitosamente |
| 400 | Bad Request - Datos inválidos |
| 401 | Unauthorized - No autenticado |
| 403 | Forbidden - No autorizado |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error del servidor |

## Paginación

Los endpoints que retornan listas soportan paginación mediante parámetros de consulta:

```
GET /individuals?page=0&size=20&sort=createdAt,desc
```

**Parámetros:**
- `page` - Número de página (base 0)
- `size` - Elementos por página
- `sort` - Campo y dirección de ordenamiento

**Respuesta:**
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

## Rate Limiting

Actualmente no hay límites de tasa implementados, pero se recomienda no exceder:

- 100 peticiones por minuto por usuario
- 1000 peticiones por hora por usuario

## Versionado de la API

La API utiliza versionado en la URL (`/api/v1`). Los cambios que rompan compatibilidad se lanzarán en una nueva versión.

## Recursos Adicionales

- [Autenticación](./authentication.md)
- [Usuarios Individuales](./individuals.md)
- [Organizaciones](./organizations.md)
- [Seguridad](../security/overview.md)

