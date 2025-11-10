# Seguridad en Nebula Cloud

Esta sección documenta la implementación de seguridad en Nebula Cloud.

## Visión General

Nebula Cloud implementa un sistema de seguridad robusto utilizando:

- 🔐 **JWT (JSON Web Tokens)** para autenticación stateless
- 🔒 **Spring Security** para autorización y control de acceso
- 🔑 **BCrypt** para hash de contraseñas
- 🍪 **HttpOnly Cookies** para almacenamiento seguro de tokens
- 🛡️ **CORS** configurado para peticiones cross-origin
- 📝 **Validación de entrada** en todos los endpoints

## Arquitectura de Seguridad

```
Cliente → JWT Filter → Spring Security → Controller → Service
```

### Flujo de Autenticación

1. **Registro/Login** → El usuario envía credenciales
2. **Validación** → El sistema valida las credenciales
3. **Generación de JWT** → Se genera un token firmado
4. **Respuesta** → El token se devuelve en el body y como cookie
5. **Peticiones subsecuentes** → El cliente envía el JWT en cada petición
6. **Validación del JWT** → El filtro valida el token antes de cada petición

## Componentes de Seguridad

### 1. JwtAuthenticationFilter

Filtro que intercepta todas las peticiones HTTP para validar el JWT.

**Ubicación:** `config/JwtAuthenticationFilter.java`

**Responsabilidades:**
- Extraer el JWT del header Authorization o cookies
- Validar el token
- Establecer el contexto de seguridad de Spring
- Permitir el acceso a endpoints públicos

**Endpoints Públicos (no requieren autenticación):**
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/authenticate`

---

### 2. SecurityConfig

Configuración principal de Spring Security.

**Ubicación:** `config/SecurityConfig.java`

**Configuraciones:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable())  // Deshabilitado para API REST
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**Características:**
- Sin estado (stateless) - no usa sesiones HTTP
- CSRF deshabilitado (no necesario para JWT)
- Rutas públicas: `/api/v1/auth/**`
- Todas las demás rutas requieren autenticación

---

### 3. JwtService

Servicio para crear y validar tokens JWT.

**Ubicación:** `service/JwtService.java`

**Métodos principales:**

```java
// Generar token
String generateToken(UserDetails userDetails)

// Validar token
boolean isTokenValid(String token, UserDetails userDetails)

// Extraer email del token
String extractUsername(String token)

// Verificar si el token ha expirado
boolean isTokenExpired(String token)
```

**Configuración del Token:**
- Algoritmo: HS256 (HMAC con SHA-256)
- Expiración: 24 horas (configurable)
- Claims incluidos: subject (email), issued at, expiration

---

### 4. AuthenticationService

Servicio que maneja el registro y autenticación de usuarios.

**Ubicación:** `service/AuthenticationService.java`

**Métodos principales:**

```java
// Registrar nuevo usuario
ResponseEntity<AuthenticationResponse> register(RegisterRequest request)

// Autenticar usuario
ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request)
```

**Proceso de Registro:**
1. Validar datos de entrada
2. Verificar que el email no esté registrado
3. Hash de la contraseña con BCrypt
4. Crear entidades User e Individual/Organization
5. Generar JWT
6. Establecer cookie HttpOnly
7. Retornar respuesta con token

**Proceso de Login:**
1. Validar credenciales con AuthenticationManager
2. Cargar detalles del usuario
3. Generar JWT
4. Establecer cookie HttpOnly
5. Retornar respuesta con token

---

## Hash de Contraseñas

Las contraseñas se hashean usando **BCrypt** con un factor de coste de 10.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Características de BCrypt:**
- ✅ Resistente a ataques de fuerza bruta
- ✅ Salt automático único por contraseña
- ✅ Factor de coste configurable
- ✅ Verificación de hash sin revelar la contraseña

---

## JWT (JSON Web Tokens)

### Estructura del Token

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYzMjU0MjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Partes:**
1. **Header** - Algoritmo y tipo de token
2. **Payload** - Claims (información del usuario)
3. **Signature** - Firma para verificar integridad

### Claims Incluidos

```json
{
  "sub": "usuario@example.com",      // Email del usuario
  "iat": 1699617600,                  // Fecha de emisión (timestamp)
  "exp": 1699704000                   // Fecha de expiración (timestamp)
}
```

### Configuración

En `application.properties`:

```properties
# Clave secreta para firmar JWT (debe ser >= 256 bits)
jwt.secret=tu_clave_secreta_muy_larga_y_segura_minimo_256_bits

# Tiempo de expiración en milisegundos (24 horas)
jwt.expiration=86400000
```

---

## Cookies HttpOnly

Los tokens también se envían como cookies seguras:

```java
ResponseCookie cookie = ResponseCookie.from("token", jwtToken)
    .httpOnly(true)           // No accesible desde JavaScript
    .secure(true)             // Solo HTTPS en producción
    .path("/")                // Disponible en toda la aplicación
    .maxAge(24 * 60 * 60)     // 24 horas
    .sameSite("Strict")       // Protección contra CSRF
    .build();
```

**Ventajas:**
- ✅ Protección contra XSS (no accesible desde JavaScript)
- ✅ Protección contra CSRF (SameSite=Strict)
- ✅ Enviado automáticamente en cada petición
- ✅ Más seguro que localStorage

---

## CORS (Cross-Origin Resource Sharing)

Configuración para permitir peticiones desde diferentes orígenes.

**Ubicación:** `config/WebConfig.java`

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:3000", "http://localhost:4200")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
}
```

---

## Validación de Entrada

Todas las DTOs utilizan anotaciones de validación de Jakarta Bean Validation:

```java
public class RegisterRequest {
    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es requerido")
    private String email;

    @NotBlank(message = "Contraseña es requerida")
    @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
    private String password;
    
    // ... más campos
}
```

---

## Mejores Prácticas Implementadas

✅ **Contraseñas hasheadas** - Nunca se almacenan en texto plano

✅ **Tokens con expiración** - Los JWT expiran después de 24 horas

✅ **Cookies HttpOnly** - Protección contra XSS

✅ **SameSite=Strict** - Protección contra CSRF

✅ **Validación de entrada** - Todos los datos se validan

✅ **Arquitectura stateless** - No se mantiene estado en el servidor

✅ **Separación de responsabilidades** - Filtros, servicios y configuración separados

## Recomendaciones para Producción

🔒 **HTTPS obligatorio** - Usar SSL/TLS en producción

🔐 **Variables de entorno** - Almacenar secretos JWT en variables de entorno

🔄 **Rotación de secretos** - Cambiar el secret JWT periódicamente

📊 **Logging de seguridad** - Registrar intentos de acceso fallidos

🛡️ **Rate limiting** - Implementar límites de peticiones

🔍 **Auditoría** - Mantener logs de acceso y cambios

## Ver También

- [JWT Service](./jwt.md)
- [Authentication API](../api/authentication.md)
- [User Model](../models/user.md)

