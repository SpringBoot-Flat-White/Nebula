---
name: Documentation Update
about: Template para Pull Requests de documentación
title: 'docs: [Breve descripción del cambio]'
labels: documentation
assignees: ''

---

## 📚 Tipo de Cambio en Documentación

<!-- Marca con 'x' las opciones que apliquen -->

- [ ] Nueva funcionalidad documentada
- [ ] Actualización de API existente
- [ ] Corrección de errores en docs
- [ ] Mejora de ejemplos
- [ ] Actualización de guías
- [ ] Corrección de typos
- [ ] Actualización de diagramas
- [ ] Otro: _____

## 📝 Descripción del Cambio

<!-- Describe los cambios realizados en la documentación -->



## 🔗 Features Relacionadas

<!-- Si este PR documenta una feature del backend, enlaza el PR correspondiente -->

- Feature PR: #
- Issue relacionado: #

## 📋 Checklist

### Contenido
- [ ] Los ejemplos de código son correctos y probados
- [ ] Las respuestas JSON son válidas
- [ ] Los endpoints están documentados completamente
- [ ] Se incluyen códigos de error esperados
- [ ] Se actualizó la tabla de contenidos si es necesario

### Calidad
- [ ] No hay errores de ortografía
- [ ] El formato Markdown es correcto
- [ ] Los links internos funcionan
- [ ] Los links externos son válidos
- [ ] El código tiene syntax highlighting correcto

### Completitud
- [ ] Se actualizó `api/overview.md` si se agregaron nuevos endpoints
- [ ] Se actualizó `models/overview.md` si hay nuevos modelos
- [ ] Se actualizó `security/overview.md` si hay cambios de seguridad
- [ ] Se actualizaron las guías de instalación si hay nuevos requisitos
- [ ] Se actualizó el sidebar si se agregaron nuevas páginas

## 🧪 Testing de Documentación

- [ ] `npm run docs:build` compila sin errores
- [ ] `npm run docs:start` muestra correctamente los cambios
- [ ] No hay broken links (verificado en el build)
- [ ] Los ejemplos con cURL fueron probados
- [ ] Las capturas de pantalla están actualizadas (si aplica)

## 📸 Screenshots (Opcional)

<!-- Si hay cambios visuales significativos, agrega screenshots -->



## 📌 Notas Adicionales

<!-- Cualquier información adicional que los reviewers deban saber -->



## ✅ Para el Reviewer

- [ ] Revisar que los ejemplos sean correctos
- [ ] Verificar que la terminología sea consistente
- [ ] Confirmar que sigue el estilo de docs existente
- [ ] Probar los ejemplos de código
- [ ] Verificar que el build pase

