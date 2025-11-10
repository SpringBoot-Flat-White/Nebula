# Configuración de la Base de Datos

Esta guía te ayudará a configurar la base de datos MySQL para Nebula Cloud.

## Crear la Base de Datos

Conéctate a MySQL y ejecuta:

```sql
CREATE DATABASE nebula_cloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Crear Usuario (Opcional pero Recomendado)

```sql
CREATE USER 'nebula_user'@'localhost' IDENTIFIED BY 'password_seguro';
GRANT ALL PRIVILEGES ON nebula_cloud.* TO 'nebula_user'@'localhost';
FLUSH PRIVILEGES;
```

## Esquema de la Base de Datos

La aplicación utiliza JPA/Hibernate con `spring.jpa.hibernate.ddl-auto=update`, por lo que las tablas se crearán automáticamente al ejecutar la aplicación.

### Tablas Principales

Las siguientes tablas serán creadas:

#### `users`
Almacena información de todos los usuarios del sistema.

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `individuals`
Información específica de usuarios individuales.

```sql
CREATE TABLE individuals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### `organizations`
Información de organizaciones.

```sql
CREATE TABLE organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    organization_name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(50),
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### Otras Tablas

- `organization_members` - Miembros de organizaciones
- `instances` - Instancias de cloud
- `containers` - Contenedores
- `engines` - Motores/Engines
- `plans` - Planes de servicio
- `payments` - Pagos
- `password_rotations` - Historial de rotación de contraseñas

## Verificar la Creación de Tablas

Después de ejecutar la aplicación por primera vez, verifica que las tablas se hayan creado:

```sql
USE nebula_cloud;
SHOW TABLES;
```

## Scripts SQL Útiles

### Consultar Usuarios

```sql
SELECT u.id, u.email, u.user_type, u.created_at
FROM users u
ORDER BY u.created_at DESC;
```

### Consultar Organizaciones

```sql
SELECT o.id, o.organization_name, u.email, o.created_at
FROM organizations o
INNER JOIN users u ON o.user_id = u.id;
```

## Migración de Datos (Producción)

Para entornos de producción, se recomienda:

1. Cambiar `spring.jpa.hibernate.ddl-auto` a `validate` o `none`
2. Usar herramientas de migración como **Flyway** o **Liquibase**
3. Mantener scripts SQL versionados

## Próximos Pasos

- [Realizar tu primera petición](./first-request.md)
- [Entender los modelos de datos](../models/overview.md)

