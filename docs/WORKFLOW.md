# 🔄 Flujo de Trabajo para Documentación

Esta guía describe el flujo de trabajo recomendado para mantener la documentación actualizada mientras desarrollas el backend.

## 📋 Estrategia de Branches

### Ramas de Documentación

```
main (producción)
  ├── develop (desarrollo)
  │   ├── feature/nueva-funcionalidad
  │   └── docs/nueva-funcionalidad
  └── docs/initial-documentation (rama actual)
```

### Convenciones de Nombres

- `docs/initial-documentation` - Documentación inicial completa
- `docs/feature-nombre` - Documentación de una feature específica
- `docs/update-api` - Actualizaciones de API
- `docs/fix-typos` - Correcciones menores

## 🚀 Flujo Recomendado

### 1. Desarrollando una Nueva Feature

Cuando trabajes en una nueva funcionalidad del backend:

```bash
# Crear rama de feature desde develop
git checkout develop
git checkout -b feature/new-endpoint

# Desarrollar la funcionalidad
# ... código ...

# Crear rama de documentación paralela
git checkout -b docs/new-endpoint

# Documentar la nueva funcionalidad
# Editar archivos en docs/docs/
```

### 2. Actualizar Documentación

**Opción A: Rama Separada (Recomendado para cambios grandes)**

```bash
# Crear rama de docs desde la rama de feature actual
git checkout -b docs/nombre-feature

# Actualizar documentación
# docs/docs/api/nuevo-endpoint.md
# docs/docs/models/nuevo-modelo.md

# Commit
git add docs/
git commit -m "docs: Add documentation for new feature"

# Push
git push origin docs/nombre-feature
```

**Opción B: Mismo Commit (Para cambios menores)**

```bash
# En tu rama de feature
git add src/ docs/
git commit -m "feat: Add new endpoint with documentation"
```

### 3. Mantener Sincronizada con Develop

```bash
# Actualizar tu rama de docs con cambios de develop
git checkout docs/initial-documentation
git merge develop

# Resolver conflictos si existen
# Actualizar documentación si hay nuevos cambios
```

## 📝 Checklist de Documentación

Cuando agregues una nueva feature, asegúrate de actualizar:

### ✅ Para Nuevos Endpoints

- [ ] Agregar endpoint a `docs/docs/api/[modulo].md`
- [ ] Incluir ejemplo de request
- [ ] Incluir ejemplo de response
- [ ] Incluir posibles errores
- [ ] Agregar ejemplo con cURL
- [ ] Actualizar `docs/docs/api/overview.md` si es necesario

### ✅ Para Nuevos Modelos

- [ ] Agregar modelo a `docs/docs/models/overview.md`
- [ ] Documentar campos y tipos
- [ ] Documentar relaciones con otros modelos
- [ ] Actualizar diagrama de relaciones si es necesario

### ✅ Para Cambios de Seguridad

- [ ] Actualizar `docs/docs/security/overview.md`
- [ ] Documentar nuevas configuraciones
- [ ] Actualizar ejemplos si es necesario

### ✅ Para Nuevas Configuraciones

- [ ] Actualizar `docs/docs/getting-started/installation.md`
- [ ] Actualizar `application.properties` de ejemplo
- [ ] Documentar nuevas variables de entorno

## 🔧 Comandos Útiles

### Ver Documentación Local

```bash
# Desde la raíz del proyecto
npm run docs:start

# Desde docs/
cd docs
npm start
```

### Build para Revisión

```bash
# Compilar documentación
npm run docs:build

# Servir compilación
npm run docs:serve
```

### Verificar Links Rotos

```bash
cd docs
npm run build  # Los links rotos fallarán el build
```

## 📦 Merge a Producción

### Antes de Merge a Main

1. **Revisar documentación completa**
   ```bash
   npm run docs:build
   npm run docs:serve
   ```

2. **Verificar todos los ejemplos**
   - Probar todos los cURL commands
   - Verificar que las respuestas sean correctas
   - Actualizar versiones si es necesario

3. **Merge desde develop**
   ```bash
   git checkout docs/initial-documentation
   git merge develop
   # Resolver conflictos
   git push origin docs/initial-documentation
   ```

4. **Crear Pull Request**
   - Título: `docs: [Descripción del cambio]`
   - Descripción: Lista de cambios en la documentación
   - Asignar reviewers

5. **Después de aprobar PR**
   ```bash
   git checkout main
   git merge docs/initial-documentation
   git push origin main
   ```

## 🎯 Best Practices

### ✅ DO (Hacer)

- ✅ Actualizar docs con cada feature nueva
- ✅ Incluir ejemplos reales y probados
- ✅ Usar commits descriptivos: `docs: Add user authentication examples`
- ✅ Revisar documentación antes de hacer merge
- ✅ Mantener consistencia en formato y estilo
- ✅ Actualizar versiones cuando sea necesario

### ❌ DON'T (No Hacer)

- ❌ Hacer merge sin actualizar documentación
- ❌ Dejar ejemplos obsoletos
- ❌ Usar commits genéricos: `update docs`
- ❌ Ignorar links rotos
- ❌ Documentar features que aún no existen
- ❌ Copiar/pegar sin adaptar a tu proyecto

## 📅 Ciclo de Actualización

### Durante Desarrollo (Diario/Semanal)

- Actualizar docs en rama paralela
- Probar ejemplos localmente
- Commit incremental de cambios

### Pre-Release (Antes de cada release)

- Revisar toda la documentación
- Actualizar números de versión
- Verificar todos los ejemplos
- Build completo para verificar links

### Post-Release (Después de cada release)

- Merge docs a main
- Tag de versión si es necesario
- Deploy a GitHub Pages (opcional)

## 🔍 Revisión de Documentación

### Checklist de Revisión

- [ ] Todos los ejemplos funcionan
- [ ] No hay links rotos
- [ ] Código de ejemplo tiene syntax correcto
- [ ] Respuestas JSON son válidas
- [ ] Configuraciones están actualizadas
- [ ] Screenshots están actualizados (si aplica)
- [ ] Versiones son correctas
- [ ] No hay información sensible (passwords, secrets)

## 🚢 Deploy (Opcional)

### GitHub Pages

```bash
cd docs
npm run build

# Deploy manual
npm run deploy

# O configurar GitHub Actions para auto-deploy
```

### Netlify

1. Conectar repositorio a Netlify
2. Build command: `cd docs && npm run build`
3. Publish directory: `docs/build`
4. Auto-deploy en cada push a main

### Vercel

1. Importar proyecto desde GitHub
2. Root directory: `docs`
3. Framework preset: Docusaurus
4. Auto-deploy configurado

## 📞 Contacto y Soporte

Si tienes dudas sobre la documentación:

1. Revisa esta guía de workflow
2. Consulta `DOCUMENTATION_GUIDE.md`
3. Lee la documentación de Docusaurus: https://docusaurus.io/

---

**Mantener la documentación actualizada es crucial para el éxito del proyecto.** 📚✨

La documentación es el primer contacto de los desarrolladores con tu API - ¡hazla brillar! 🌟

