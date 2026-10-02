# Arquitectura - events-api

API REST en Java 21 + Spring Boot 3.2.5 organizada como **arquitectura hexagonal (puertos y
adaptadores)**. El nucleo (dominio + aplicacion) no conoce Spring, HTTP, JWT ni la base de
datos: todo eso entra y sale por puertos. Las reglas se verifican automaticamente con ArchUnit
(`src/test/java/com/events/architecture/CleanArchitectureTest.java`).

## Vista general

```
                 ┌───────────────────────── infrastructure ─────────────────────────┐
  HTTP  ──►      │ adapter.in.rest  (controllers, DTOs, mappers, exception handler) │
  (front)        │        │ usa solo puertos de entrada (application.port.in)        │
                 │        ▼                                                          │
                 │   ┌──────────────── application ────────────────┐                │
                 │   │ port.in  (interfaces de casos de uso)        │                │
                 │   │ usecase  (implementaciones, Java puro)       │                │
                 │   │ port.out (interfaces que el caso de uso pide)│                │
                 │   └───────────────┬─────────────────────────────┘                │
                 │                   │ depende de                                    │
                 │             ┌─────▼─────┐                                         │
                 │             │  domain   │ entidades, enums, excepciones            │
                 │             └───────────┘                                         │
                 │        ▲ implementan port.out                                     │
                 │ adapter.out.persistence (JPA)   security (JWT, BCrypt, contexto)  │
                 │ config (wiring de beans, CORS, OpenAPI)                           │
                 └──────────────────────────────────────────────────────────────────┘
                        │                                   │
                   PostgreSQL (Supabase)            Spring Security filter chain
```

Las dependencias siempre apuntan hacia adentro: `infrastructure → application → domain`.

## Paquetes

| Paquete | Responsabilidad | Puede depender de |
|---|---|---|
| `com.events.domain.entity` | Entidades (`Usuario`, `Organizador`, `Rol`, `Evento`, `Subtarea`, `CapacidadDiaria`) y enums (`EstadoSubtarea`, `NombreRol`). Contienen las reglas de negocio (ej. horas > 0, limite 1..16, transiciones de estado). | solo `jakarta.persistence` (compromiso pragmatico, ver abajo) |
| `com.events.domain.exception` | Excepciones de negocio (`EventoNotFoundException`, `CapacityConflictException`, `CorreoYaRegistradoException`, `CredencialesInvalidasException`, ...). | nada |
| `com.events.application.port.in` | Un puerto (interfaz) por caso de uso: `CreateEventoPort`, `LoginPort`, `RegisterPort`, ... y records de resultado (`AuthResult`, `TodayGroups`, ...). | domain |
| `com.events.application.usecase` | Implementacion de cada caso de uso. Java puro, sin anotaciones de Spring. | domain, ports |
| `com.events.application.port.out` | Lo que los casos de uso necesitan del exterior: repositorios, `CurrentOrganizadorPort`, `PasswordHasherPort`, `TokenProviderPort`. | domain |
| `com.events.infrastructure.adapter.in.rest` | Adaptador de entrada HTTP: controllers, DTOs (request/response), mappers, `GlobalExceptionHandler`. | application.port.in, domain |
| `com.events.infrastructure.adapter.out.persistence` | Adaptadores de salida JPA (`*PersistenceAdapter`) sobre repositorios Spring Data (`Jpa*Repository`). | application.port.out, domain |
| `com.events.infrastructure.security` | Spring Security: `SecurityConfig` (rutas protegidas, JWT), `JwtTokenProviderAdapter`, `BCryptPasswordHasherAdapter`, `SecurityContextCurrentOrganizadorAdapter`, `RestAuthenticationErrorHandler`. | application.port.out, domain |
| `com.events.infrastructure.config` | Wiring: `UseCaseConfig` crea los beans de casos de uso; `CorsConfig`; `OpenApiConfig`. | todo |

## Reglas verificadas por ArchUnit

1. El dominio no depende de `application` ni de `infrastructure`.
2. El dominio no depende de Spring, Bean Validation ni Lombok.
3. La aplicacion no depende de `infrastructure`.
4. **La aplicacion no depende de Spring, Servlet ni Nimbus/JWT** (la seguridad entra por puertos).
5. Los adaptadores de entrada no dependen de los de salida, ni viceversa.
6. Los adaptadores de entrada usan puertos (`port.in`), nunca las clases `usecase` directamente.
7. Los puertos de entrada y de salida son interfaces (salvo los records de resultado listados).
8. Los `@Repository` viven en `adapter.out.persistence`; los `@RestController` en `adapter.in.rest.controller`.

**Compromiso pragmatico:** las entidades de dominio llevan anotaciones `jakarta.persistence`
para no duplicar cada entidad en (dominio + entidad JPA + mapper). Es la unica concesion al
framework dentro del dominio.

## Flujo de una peticion protegida

Ejemplo: `GET /api/events/{id}` con `Authorization: Bearer <jwt>`.

1. **Spring Security** (`SecurityConfig`): el resource server valida firma HS256, `iss` y `exp`
   del JWT. Si falta o es invalido → `RestAuthenticationErrorHandler` responde `401` JSON.
   Si la ruta es `/api/admin/**` y el token no trae `ROLE_ADMIN` → `403`.
2. El `Jwt` queda en el `SecurityContext`; el claim `roles` se convierte en `ROLE_*`.
3. **`EventoController`** (adaptador de entrada) llama a `GetEventoPort.execute(id)`.
4. **`GetEventoUseCase`** pide el usuario actual a `CurrentOrganizadorPort` (implementado por
   `SecurityContextCurrentOrganizadorAdapter`, que lee el `sub` del JWT) y busca con
   `EventoRepositoryPort.findByIdAndOrganizadorId(id, organizadorId)`.
5. **`EventoPersistenceAdapter`** ejecuta la consulta JPA filtrando por `organizador_id`.
   Si el evento es de otro usuario no se encuentra → `EventoNotFoundException` → `404`.
6. El controller mapea la entidad a `EventoResponse` y la envuelve en `ApiResponse`.

## Flujo de registro / login

```
POST /api/auth/register ─► AuthController ─► RegisterPort (RegisterUseCase)
                                               ├─ UsuarioRepositoryPort.existsByCorreo      (409 si existe)
                                               ├─ RolRepositoryPort.findByNombre(ORGANIZADOR)
                                               ├─ PasswordHasherPort.hash      ◄── BCryptPasswordHasherAdapter
                                               ├─ UsuarioRepositoryPort.save  (crea usuario + perfil organizador en cascada)
                                               └─ TokenProviderPort.generate   ◄── JwtTokenProviderAdapter (Nimbus)

POST /api/auth/login ─► AuthController ─► LoginPort (LoginUseCase)
                                            ├─ findByCorreo + organizador activo + password_hash != null
                                            ├─ PasswordHasherPort.matches   (401 si falla)
                                            └─ TokenProviderPort.generate
```

El caso de uso nunca ve BCrypt ni JWT: solo puertos. Cambiar a otro algoritmo o a tokens
opacos = escribir otro adaptador, sin tocar `application`.

## Propiedad de los datos (multiusuario)

- `usuarios` registra el login (nombre, correo, password_hash, created_at). `organizadores` es el
  perfil de organizador: solo `usuario_id` (PK y FK hacia `usuarios.id`) y `activo` (relacion 1 a 1, opcional
  para el usuario, obligatoria para el organizador); comparte
  el id del usuario, por eso el `sub` del JWT es a la vez el id de usuario y de organizador.
  Los roles cuelgan de `organizadores` (`organizador_roles.usuario_id` -> `organizadores.usuario_id`);
  un usuario sin fila en `organizadores` no tiene roles ni eventos. Nombre y correo de un
  organizador se leen por la relacion con `usuarios` (`Organizador.getNombre()/getCorreo()`).
  Cada organizador es dueno de:
  - sus `eventos` (`eventos.organizador_id`),
  - las `subtareas` de esos eventos (via `subtareas.evento_id`),
  - su `capacidades_diarias` (`capacidades_diarias.organizador_id`).
- **Todos** los casos de uso que reciben un id (evento o subtarea) filtran por el
  organizador del token: `findByIdAndOrganizadorId`, `existsByIdAndOrganizadorId`.
- Los listados (`/api/events`, `/api/today`, `/api/capacity`) ya filtraban por
  `CurrentOrganizadorPort`, que ahora resuelve al usuario autenticado en vez del antiguo
  organizador demo.

Modelo de datos completo en [schema.sql](./schema.sql):

```
usuarios 1───0..1 organizadores 1───* eventos 1───* subtareas
   │ 1                    │ 1
   │                      └───* capacidades_diarias
                          └───* organizador_roles *───1 roles
```

## Como agregar un caso de uso nuevo

1. Puerto de entrada en `application/port/in` (interfaz `XxxPort`).
2. Implementacion en `application/usecase` (Java puro; si necesita el usuario actual, inyecta
   `CurrentOrganizadorPort` y filtra por el).
3. Si necesita algo externo, un puerto en `application/port/out` y su adaptador en
   `infrastructure/adapter/out/...` (o `infrastructure/security`).
4. Bean en `UseCaseConfig`.
5. Endpoint en un controller de `adapter/in/rest/controller` usando solo el puerto.
6. Si la ruta debe ser publica o restringida por rol, ajustala en `SecurityConfig`.
7. Test unitario del caso de uso con mocks de los puertos.

## Configuracion de seguridad

| Propiedad | Variable | Default |
|---|---|---|
| `app.security.jwt.secret` | `JWT_SECRET` | clave de desarrollo (cambiar en produccion, >= 32 caracteres) |
| `app.security.jwt.expiration-minutes` | `JWT_EXPIRATION_MINUTES` | `120` |
| `app.security.jwt.issuer` | - | `events-api` |
