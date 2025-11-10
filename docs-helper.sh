#!/bin/bash

# Script helper para tareas comunes de documentación
# Uso: ./docs-helper.sh [comando]

set -e

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

DOCS_DIR="docs"

# Función para mostrar uso
show_usage() {
    echo -e "${BLUE}Nebula Cloud - Documentation Helper${NC}"
    echo ""
    echo "Uso: ./docs-helper.sh [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  start           - Inicia el servidor de desarrollo"
    echo "  build           - Compila la documentación"
    echo "  serve           - Sirve la documentación compilada"
    echo "  test            - Verifica que no haya errores"
    echo "  create-branch   - Crea una nueva rama de documentación"
    echo "  add-endpoint    - Wizard para documentar un nuevo endpoint"
    echo "  add-model       - Wizard para documentar un nuevo modelo"
    echo "  check-links     - Verifica links rotos"
    echo "  install         - Instala dependencias"
    echo ""
}

# Función para verificar que estamos en la raíz del proyecto
check_root() {
    if [ ! -d "$DOCS_DIR" ]; then
        echo -e "${RED}Error: Este script debe ejecutarse desde la raíz del proyecto${NC}"
        exit 1
    fi
}

# Comando: start
cmd_start() {
    echo -e "${BLUE}🚀 Iniciando servidor de documentación...${NC}"
    cd "$DOCS_DIR" && npm start
}

# Comando: build
cmd_build() {
    echo -e "${BLUE}📦 Compilando documentación...${NC}"
    cd "$DOCS_DIR" && npm run build
    echo -e "${GREEN}✅ Build completado exitosamente${NC}"
}

# Comando: serve
cmd_serve() {
    echo -e "${BLUE}🌐 Sirviendo documentación compilada...${NC}"
    cd "$DOCS_DIR" && npm run serve
}

# Comando: test
cmd_test() {
    echo -e "${BLUE}🧪 Ejecutando tests de documentación...${NC}"

    # Verificar que compile
    echo -e "${YELLOW}Verificando que compile...${NC}"
    cd "$DOCS_DIR" && npm run build

    # Verificar links rotos
    echo -e "${YELLOW}Verificando links...${NC}"
    npm run build 2>&1 | grep -i "broken" && echo -e "${RED}❌ Se encontraron links rotos${NC}" || echo -e "${GREEN}✅ No hay links rotos${NC}"

    echo -e "${GREEN}✅ Tests completados${NC}"
}

# Comando: create-branch
cmd_create_branch() {
    echo -e "${BLUE}🌿 Crear nueva rama de documentación${NC}"
    echo ""
    read -p "Nombre de la feature (ej: new-endpoint): " feature_name

    if [ -z "$feature_name" ]; then
        echo -e "${RED}Error: Debes proporcionar un nombre${NC}"
        exit 1
    fi

    branch_name="docs/$feature_name"

    echo -e "${YELLOW}Creando rama: $branch_name${NC}"
    git checkout -b "$branch_name"

    echo -e "${GREEN}✅ Rama creada exitosamente${NC}"
    echo -e "Ahora puedes empezar a documentar en: ${BLUE}$branch_name${NC}"
}

# Comando: add-endpoint
cmd_add_endpoint() {
    echo -e "${BLUE}📋 Wizard: Documentar nuevo endpoint${NC}"
    echo ""

    read -p "Nombre del módulo (auth/individuals/organizations/otro): " module
    read -p "Método HTTP (GET/POST/PUT/DELETE): " method
    read -p "Endpoint (ej: /api/v1/users): " endpoint
    read -p "Descripción breve: " description

    echo ""
    echo -e "${GREEN}Información del endpoint:${NC}"
    echo "  Módulo: $module"
    echo "  Método: $method"
    echo "  Endpoint: $endpoint"
    echo "  Descripción: $description"
    echo ""
    echo -e "${YELLOW}Ahora debes editar:${NC}"
    echo "  📄 docs/docs/api/${module}.md"
    echo ""
    echo "Agrega una sección como esta:"
    echo ""
    echo "## $description"
    echo ""
    echo "### Endpoint"
    echo ""
    echo "\`\`\`"
    echo "$method $endpoint"
    echo "\`\`\`"
    echo ""
    echo "### Request Body"
    echo ""
    echo "\`\`\`json"
    echo "{"
    echo "  // Tu JSON aquí"
    echo "}"
    echo "\`\`\`"
    echo ""
    echo -e "${BLUE}💡 Tip: Copia una sección existente como plantilla${NC}"
}

# Comando: add-model
cmd_add_model() {
    echo -e "${BLUE}📋 Wizard: Documentar nuevo modelo${NC}"
    echo ""

    read -p "Nombre del modelo (ej: User): " model_name
    read -p "Nombre de la tabla (ej: users): " table_name

    echo ""
    echo -e "${GREEN}Información del modelo:${NC}"
    echo "  Modelo: $model_name"
    echo "  Tabla: $table_name"
    echo ""
    echo -e "${YELLOW}Ahora debes editar:${NC}"
    echo "  📄 docs/docs/models/overview.md"
    echo ""
    echo "Agrega una sección como esta:"
    echo ""
    echo "### $model_name"
    echo ""
    echo "**Tabla:** \`$table_name\`"
    echo ""
    echo "| Campo | Tipo | Descripción |"
    echo "|-------|------|-------------|"
    echo "| id | Long | ID único |"
    echo "| // Tus campos aquí |"
    echo ""
}

# Comando: check-links
cmd_check_links() {
    echo -e "${BLUE}🔗 Verificando links en la documentación...${NC}"

    cd "$DOCS_DIR" && npm run build 2>&1 | tee /tmp/docs-build.log

    if grep -qi "broken" /tmp/docs-build.log; then
        echo -e "${RED}❌ Se encontraron links rotos${NC}"
        grep -i "broken" /tmp/docs-build.log
        exit 1
    else
        echo -e "${GREEN}✅ No se encontraron links rotos${NC}"
    fi
}

# Comando: install
cmd_install() {
    echo -e "${BLUE}📦 Instalando dependencias...${NC}"
    cd "$DOCS_DIR" && npm install
    echo -e "${GREEN}✅ Dependencias instaladas${NC}"
}

# Main
check_root

case "$1" in
    start)
        cmd_start
        ;;
    build)
        cmd_build
        ;;
    serve)
        cmd_serve
        ;;
    test)
        cmd_test
        ;;
    create-branch)
        cmd_create_branch
        ;;
    add-endpoint)
        cmd_add_endpoint
        ;;
    add-model)
        cmd_add_model
        ;;
    check-links)
        cmd_check_links
        ;;
    install)
        cmd_install
        ;;
    *)
        show_usage
        exit 1
        ;;
esac

