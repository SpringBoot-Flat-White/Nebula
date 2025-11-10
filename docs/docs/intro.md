# Introducción a Nebula Cloud

Bienvenido a la documentación de **Nebula Cloud**, una plataforma de gestión de infraestructura en la nube construida con Spring Boot.

## ¿Qué es Nebula Cloud?

Nebula Cloud es una plataforma que permite a usuarios individuales y organizaciones gestionar su infraestructura en la nube de manera eficiente. La plataforma ofrece:

- 🔐 **Autenticación y autorización segura** con JWT
- 👤 **Gestión de usuarios individuales y organizaciones**
- 🏢 **Sistema multi-tenant** para organizaciones
- 🚀 **Gestión de instancias, contenedores y engines**
- 💳 **Sistema de planes y pagos**
- 🔑 **Rotación de contraseñas** para mayor seguridad

## Tecnologías Utilizadas

- **Spring Boot 3.5.7** - Framework principal
- **Java 21** - Lenguaje de programación
- **Spring Security** - Autenticación y autorización
- **JWT (JSON Web Tokens)** - Tokens de sesión seguros
- **Spring Data JPA** - Persistencia de datos
- **MySQL** - Base de datos relacional
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias

## Arquitectura del Proyecto

El proyecto sigue una arquitectura en capas:

```
Controller Layer (API REST)
       ↓
Service Layer (Lógica de negocio)
       ↓
Repository Layer (Acceso a datos)
       ↓
Database (MySQL)
```

## Comenzando

Para comenzar a trabajar con Nebula Cloud:

1. [Instalación y Configuración](./getting-started/installation.md)
2. [Configuración de la Base de Datos](./getting-started/database-setup.md)
3. [Tu Primera Petición API](./getting-started/first-request.md)

## Recursos Adicionales

- [Referencia de la API](./api/overview.md)
- [Modelos de Datos](./models/overview.md)
- [Seguridad](./security/overview.md)

