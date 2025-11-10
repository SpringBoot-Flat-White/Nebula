# Modelos de Datos

Esta sección documenta todos los modelos de datos utilizados en Nebula Cloud.

## Entidades Principales

### 👤 User (Usuario)

Entidad base para todos los usuarios del sistema. Implementa `UserDetails` de Spring Security.

**Tabla:** `users`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del usuario |
| email | String | Email único (max 120 caracteres) |
| password | String | Hash de contraseña (BCrypt) |
| userType | UserType | Tipo de usuario (INDIVIDUAL, ORGANIZATION) |
| createdAt | LocalDateTime | Fecha de creación (auto) |
| updatedAt | LocalDateTime | Fecha de actualización (auto) |

**Relaciones:**
- OneToOne → Individual (si userType = INDIVIDUAL)
- OneToOne → Organization (si userType = ORGANIZATION)

---

### 🙋 Individual (Usuario Individual)

Información específica de usuarios individuales.

**Tabla:** `individuals`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del individual |
| userId | Long | FK a users.id |
| firstName | String | Nombre (max 100 caracteres) |
| lastName | String | Apellido (max 100 caracteres) |
| phone | String | Teléfono (max 20 caracteres) |
| address | String | Dirección |
| city | String | Ciudad (max 100 caracteres) |
| country | String | País (max 100 caracteres) |

**Relaciones:**
- OneToOne → User

---

### 🏢 Organization (Organización)

Información de organizaciones corporativas.

**Tabla:** `organizations`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único de la organización |
| userId | Long | FK a users.id |
| organizationName | String | Nombre de la organización (max 200) |
| taxId | String | NIT o ID tributario (max 50) |
| phone | String | Teléfono (max 20 caracteres) |
| address | String | Dirección |
| city | String | Ciudad (max 100 caracteres) |
| country | String | País (max 100 caracteres) |

**Relaciones:**
- OneToOne → User
- OneToMany → OrganizationMember

---

### 👥 OrganizationMember (Miembro de Organización)

Relación entre organizaciones y sus miembros.

**Tabla:** `organization_members`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del miembro |
| organizationId | Long | FK a organizations.id |
| userId | Long | FK a users.id |
| role | String | Rol del miembro (OWNER, ADMIN, MEMBER) |
| permissions | String | Permisos específicos (JSON) |
| joinedAt | LocalDateTime | Fecha de ingreso |

**Relaciones:**
- ManyToOne → Organization
- ManyToOne → User

---

### 🚀 Instance (Instancia)

Instancias de máquinas virtuales o servidores.

**Tabla:** `instances`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único de la instancia |
| userId | Long | FK a users.id (propietario) |
| name | String | Nombre de la instancia |
| type | String | Tipo de instancia (t2.micro, etc) |
| status | String | Estado (RUNNING, STOPPED, etc) |
| ipAddress | String | Dirección IP asignada |
| region | String | Región del servidor |
| createdAt | LocalDateTime | Fecha de creación |
| stoppedAt | LocalDateTime | Fecha de detención |

**Relaciones:**
- ManyToOne → User

---

### 📦 Container (Contenedor)

Contenedores Docker u otros runtimes.

**Tabla:** `containers`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del contenedor |
| instanceId | Long | FK a instances.id |
| name | String | Nombre del contenedor |
| image | String | Imagen Docker |
| status | String | Estado (RUNNING, STOPPED, etc) |
| port | Integer | Puerto expuesto |
| createdAt | LocalDateTime | Fecha de creación |

**Relaciones:**
- ManyToOne → Instance

---

### ⚙️ Engine (Motor)

Motores de base de datos o servicios.

**Tabla:** `engines`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del engine |
| userId | Long | FK a users.id |
| type | String | Tipo (MySQL, PostgreSQL, Redis, etc) |
| version | String | Versión del engine |
| status | String | Estado (ACTIVE, INACTIVE) |
| endpoint | String | Endpoint de conexión |
| port | Integer | Puerto |
| createdAt | LocalDateTime | Fecha de creación |

**Relaciones:**
- ManyToOne → User

---

### 💎 Plan (Plan de Servicio)

Planes de suscripción disponibles.

**Tabla:** `plans`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del plan |
| name | String | Nombre del plan (Free, Pro, Enterprise) |
| description | String | Descripción del plan |
| price | BigDecimal | Precio mensual |
| maxInstances | Integer | Máximo de instancias permitidas |
| maxStorage | Integer | Almacenamiento en GB |
| maxBandwidth | Integer | Ancho de banda en GB |
| features | String | Características (JSON) |

---

### 💳 Payment (Pago)

Registro de pagos realizados.

**Tabla:** `payments`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del pago |
| userId | Long | FK a users.id |
| planId | Long | FK a plans.id |
| amount | BigDecimal | Monto del pago |
| currency | String | Moneda (USD, COP, etc) |
| status | String | Estado (PENDING, COMPLETED, FAILED) |
| paymentMethod | String | Método de pago |
| transactionId | String | ID de transacción externa |
| paidAt | LocalDateTime | Fecha de pago |

**Relaciones:**
- ManyToOne → User
- ManyToOne → Plan

---

### 🔐 PasswordRotation (Rotación de Contraseñas)

Historial de cambios de contraseña.

**Tabla:** `password_rotations`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID único del registro |
| userId | Long | FK a users.id |
| oldPasswordHash | String | Hash de la contraseña anterior |
| newPasswordHash | String | Hash de la nueva contraseña |
| rotatedAt | LocalDateTime | Fecha de rotación |
| reason | String | Razón del cambio |

**Relaciones:**
- ManyToOne → User

---

## Enumeraciones

### UserType

```java
public enum UserType {
    INDIVIDUAL,      // Usuario individual
    ORGANIZATION     // Organización
}
```

---

## Diagrama de Relaciones

```
User (1) ←→ (1) Individual
User (1) ←→ (1) Organization
User (1) ←→ (N) Instance
User (1) ←→ (N) Engine
User (1) ←→ (N) Payment
User (1) ←→ (N) PasswordRotation

Organization (1) ←→ (N) OrganizationMember
Instance (1) ←→ (N) Container
Plan (1) ←→ (N) Payment
```

## Ver También

- [User Entity](./user.md)
- [Individual Entity](./individual.md)
- [Organization Entity](./organization.md)

