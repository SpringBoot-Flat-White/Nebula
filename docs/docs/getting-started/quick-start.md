# Guía de Inicio Rápido

Esta guía te ayudará a poner en marcha Nebula Cloud en menos de 10 minutos.

## Paso 1: Verificar Prerrequisitos

Asegúrate de tener instalado:

```bash
# Verificar Java
java -version
# Debería mostrar: openjdk version "21" o superior

# Verificar Maven
mvn -version
# Debería mostrar: Apache Maven 3.6 o superior

# Verificar MySQL
mysql --version
# Debería mostrar: mysql Ver 8.0 o superior
```

## Paso 2: Configurar Base de Datos

```sql
-- Crear base de datos
CREATE DATABASE nebula_cloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario (opcional)
CREATE USER 'nebula_user'@'localhost' IDENTIFIED BY 'password_seguro';
GRANT ALL PRIVILEGES ON nebula_cloud.* TO 'nebula_user'@'localhost';
FLUSH PRIVILEGES;
```

## Paso 3: Configurar Aplicación

Edita `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/nebula_cloud
spring.datasource.username=nebula_user
spring.datasource.password=password_seguro

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=mi_clave_secreta_muy_larga_para_jwt_debe_ser_minimo_256_bits_de_longitud
jwt.expiration=86400000

# Server
server.port=8080
```

## Paso 4: Ejecutar la Aplicación

```bash
# Desde la raíz del proyecto
mvn spring-boot:run
```

Espera a ver el mensaje:
```
Started NebulaCloudApplication in X.XXX seconds
```

## Paso 5: Probar la API

### Registrar un usuario

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "userType": "INDIVIDUAL",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGci...",
  "email": "test@example.com",
  "userType": "INDIVIDUAL"
}
```

### Iniciar sesión

```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### Usar el token

```bash
# Guarda el token en una variable
TOKEN="tu_token_aqui"

# Obtener perfil
curl -X GET http://localhost:8080/api/v1/individuals/1 \
  -H "Authorization: Bearer $TOKEN"
```

## ✅ ¡Listo!

Tu aplicación Nebula Cloud está funcionando. Ahora puedes:

- 📖 Explorar la [documentación completa de la API](../api/overview.md)
- 🔒 Aprender sobre [seguridad y JWT](../security/overview.md)
- 🏢 Crear [organizaciones](../api/organizations.md)
- 🗄️ Conocer los [modelos de datos](../models/overview.md)

## Solución de Problemas Comunes

### Error: "Access denied for user"

- Verifica las credenciales de MySQL en `application.properties`
- Asegúrate de que el usuario tenga permisos sobre la base de datos

### Error: "Table doesn't exist"

- Verifica que `spring.jpa.hibernate.ddl-auto=update` esté configurado
- Reinicia la aplicación para que Hibernate cree las tablas

### Error: "Port 8080 already in use"

- Cambia el puerto en `application.properties`: `server.port=8081`
- O detén el proceso que está usando el puerto 8080

### Error: JWT "Secret key is too short"

- Asegúrate de que tu `jwt.secret` tenga al menos 256 bits (32 caracteres)

## Próximos Pasos

1. [Configurar la base de datos completa](./database-setup.md)
2. [Explorar todos los endpoints](../api/overview.md)
3. [Implementar tu primer flujo completo](./first-request.md)

