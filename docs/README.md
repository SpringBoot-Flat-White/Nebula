# Nebula Cloud - Documentación

Documentación oficial de Nebula Cloud construida con Docusaurus.

## Desarrollo

### Instalar Dependencias

```bash
npm install
```

### Iniciar el Servidor de Desarrollo

```bash
npm start
```

Esto iniciará el servidor de desarrollo en `http://localhost:3000`. Los cambios se reflejarán automáticamente.

### Build

```bash
npm run build
```

Este comando genera contenido estático en el directorio `build` que puede ser servido usando cualquier servidor de contenido estático.

## Estructura de la Documentación

```
docs/
├── intro.md                      # Página de introducción
├── getting-started/              # Guías de inicio
│   ├── installation.md           # Instalación y configuración
│   ├── database-setup.md         # Configuración de base de datos
│   └── first-request.md          # Primera petición API
├── api/                          # Referencia de API
│   ├── overview.md               # Visión general de la API
│   ├── authentication.md         # Endpoints de autenticación
│   ├── individuals.md            # Endpoints de usuarios individuales
│   └── organizations.md          # Endpoints de organizaciones
├── models/                       # Modelos de datos
│   └── overview.md               # Visión general de modelos
└── security/                     # Documentación de seguridad
    └── overview.md               # Visión general de seguridad
```

## Contribuir a la Documentación

1. Edita los archivos Markdown en la carpeta `docs/`
2. Los cambios se reflejarán automáticamente en el servidor de desarrollo
3. Asegúrate de seguir el formato Markdown estándar
4. Incluye ejemplos de código cuando sea posible

## Recursos

- [Docusaurus Documentation](https://docusaurus.io/)
- [Markdown Guide](https://www.markdownguide.org/)

