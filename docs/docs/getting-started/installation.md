# Instalación y Configuración

Esta guía te ayudará a configurar Nebula Cloud en tu entorno de desarrollo local.

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java 21** o superior
- 📦 **Maven 3.6+**
- 🐬 **MySQL 8.0+**
- 🔧 **Git**
- 💻 Un IDE (recomendado: IntelliJ IDEA, Eclipse, o VS Code)

## Clonar el Repositorio

```bash
git clone https://github.com/your-org/nebula-cloud.git
cd nebula-cloud
```

## Configurar Variables de Entorno

Crea un archivo `application.properties` en `src/main/resources/` con la siguiente configuración:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/nebula_cloud
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=tu_secreto_jwt_muy_largo_y_seguro_minimo_256_bits
jwt.expiration=86400000

# Server Configuration
server.port=8080

# Logging
logging.level.com.nebula.nebulaCloud=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Instalar Dependencias

```bash
mvn clean install
```

## Ejecutar la Aplicación

### Usando Maven

```bash
mvn spring-boot:run
```

### Usando el IDE

Ejecuta la clase principal `NebulaCloudApplication.java`

### Usando el JAR compilado

```bash
mvn package
java -jar target/nebulaCloud-0.0.1-SNAPSHOT.jar
```

## Verificar la Instalación

Una vez que la aplicación esté en ejecución, deberías ver en los logs:

```
Started NebulaCloudApplication in X.XXX seconds
```

La API estará disponible en: `http://localhost:8080`

## Próximos Pasos

- [Configurar la Base de Datos](./database-setup.md)
- [Realizar tu primera petición](./first-request.md)

