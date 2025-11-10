# Nebula Cloud ☁️

Plataforma de gestión de infraestructura en la nube construida con Spring Boot.

## 🚀 Características

- 🔐 Autenticación y autorización segura con JWT
- 👤 Gestión de usuarios individuales y organizaciones
- 🏢 Sistema multi-tenant para organizaciones
- 🚀 Gestión de instancias, contenedores y engines
- 💳 Sistema de planes y pagos
- 🔑 Rotación de contraseñas para mayor seguridad

## 🛠️ Tecnologías

- **Spring Boot 3.5.7** - Framework principal
- **Java 21** - Lenguaje de programación
- **Spring Security** - Autenticación y autorización
- **JWT** - Tokens de sesión seguros
- **Spring Data JPA** - Persistencia de datos
- **MySQL** - Base de datos relacional
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias

## 📚 Documentación

La documentación completa del proyecto está disponible en la carpeta `docs/` construida con Docusaurus.

### Iniciar la Documentación

```bash
cd docs
npm install
npm start
```

La documentación estará disponible en `http://localhost:3000`

### Contenido de la Documentación

- **Primeros Pasos**: Instalación, configuración de base de datos y primeras peticiones
- **API Reference**: Documentación completa de todos los endpoints
- **Modelos de Datos**: Estructura de las entidades y sus relaciones
- **Seguridad**: Implementación de JWT, autenticación y mejores prácticas

## 🏃 Quick Start

### Prerrequisitos

- Java 21 o superior
- Maven 3.6+
- MySQL 8.0+

### Instalación

1. Clonar el repositorio:
```bash
git clone https://github.com/your-org/nebula-cloud.git
cd nebula-cloud
```

2. Configurar la base de datos en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nebula_cloud
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
jwt.secret=tu_secreto_jwt_muy_largo_y_seguro
```

3. Ejecutar la aplicación:
```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`

## 📖 Endpoints Principales

### Autenticación

- `POST /api/v1/auth/register` - Registrar nuevo usuario
- `POST /api/v1/auth/authenticate` - Iniciar sesión

### Usuarios Individuales

- `GET /api/v1/individuals` - Listar usuarios
- `GET /api/v1/individuals/{id}` - Obtener usuario por ID
- `PUT /api/v1/individuals/{id}` - Actualizar usuario

### Organizaciones

- `GET /api/v1/organizations` - Listar organizaciones
- `GET /api/v1/organizations/{id}` - Obtener organización por ID
- `POST /api/v1/organizations/{id}/members` - Agregar miembro

Para más detalles, consulta la [documentación completa](./docs).

## 🏗️ Arquitectura

```
Controller Layer (API REST)
       ↓
Service Layer (Lógica de negocio)
       ↓
Repository Layer (Acceso a datos)
       ↓
Database (MySQL)
```

## 🔒 Seguridad

- Autenticación mediante JWT (JSON Web Tokens)
- Contraseñas hasheadas con BCrypt
- Cookies HttpOnly para mayor seguridad
- CORS configurado
- Validación de entrada en todos los endpoints

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/nebula/nebulaCloud/
│   │   ├── config/              # Configuración (Security, JWT, CORS)
│   │   ├── controller/          # Controladores REST
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── model/               # Entidades JPA
│   │   ├── repository/          # Repositorios JPA
│   │   └── service/             # Lógica de negocio
│   └── resources/
│       └── application.properties
└── test/                        # Tests
docs/                            # Documentación Docusaurus
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

## 👥 Autores

- Tu Nombre - Desarrollo inicial

## 🙏 Agradecimientos

- Spring Boot Team
- Riwi
- Comunidad de desarrolladores

