# events-api

API REST para organizadores de eventos independientes: crear eventos y su plan de trabajo
logistico, ver la vista "Hoy" con lo urgente, reprogramar ante imprevistos detectando
sobrecarga diaria, y registrar la ejecucion con barra de progreso. Desarrollada con Java 21 y
Spring Boot 3.2.5 siguiendo una arquitectura hexagonal, y persistida en Supabase (Postgres).

Este proyecto es la refactorizacion de `Back-Task` para cumplir el backlog del mini-proyecto
"Organizador de Eventos Independientes" (US-01 a US-12, TS-01 a TS-03).

## Tecnologias principales

- Java 21, Spring Boot 3.2.5, Maven
- Spring Data JPA + Postgres (Supabase)
- Springdoc OpenAPI y Swagger UI
- JUnit 5, Mockito y AssertJ
- ArchUnit para validar la arquitectura
- JaCoCo para medir la cobertura

## Alcance de esta version

Cubre el backend de las historias US-01 a US-12. US-11 (autenticacion) usa Spring Security
con JWT (Bearer, HS256): registro, login, roles (`ORGANIZADOR`, `ADMIN`) y rutas protegidas.
Cada usuario solo ve y modifica sus propios eventos, subtareas y capacidad diaria.

- Arquitectura: [docs/arquitectura.md](docs/arquitectura.md)
- Esquema SQL (usuarios, roles y modelo de negocio): [docs/schema.sql](docs/schema.sql)
- Contratos para el front (auth + todos los endpoints): [docs/contratos-frontend.md](docs/contratos-frontend.md)

## Requisitos

- JDK 21
- Maven Wrapper incluido (`mvnw`/`mvnw.cmd`), con Maven 3.9.16; no necesitas instalar Maven por separado.
- Un proyecto de Supabase (o cualquier Postgres accesible)

El wrapper descarga Maven en el primer uso. Para iniciar desde la carpeta `backend`:

```bash
./mvnw spring-boot:run
```

En Windows: `.\mvnw.cmd spring-boot:run`. Java debe estar disponible en PATH o mediante
JAVA_HOME, y la base de datos debe tener el esquema indicado abajo. Si el wrapper indica
que JAVA_HOME no está definido correctamente, configurar la carpeta del JDK antes de ejecutar:

```bash
export JAVA_HOME=/ruta/al/jdk
./mvnw spring-boot:run
```

## Configuracion

Copia `.env.example` a `.env` y completa los valores de tu base de datos. `.env` esta en
`.gitignore`: nunca subas credenciales reales al repositorio.

| Variable | Valor predeterminado | Descripcion |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto en el que se ejecuta la API. |
| `CORS_ORIGIN` | `http://localhost:4300,https://frontend-pi-olive-30.vercel.app` | Origenes autorizados, separados por comas, sin ruta ni barra final. |
| `DB_URL` | *(localhost, no funcional)* | Cadena JDBC de conexion a Postgres/Supabase. |
| `DB_USERNAME` | *(localhost, no funcional)* | Usuario de la base de datos. |
| `DB_PASSWORD` | *(localhost, no funcional)* | Password de la base de datos. |

Para el backend desplegado, configura `CORS_ORIGIN=https://frontend-pi-olive-30.vercel.app`
y vuelve a desplegarlo. Esta variable reemplaza los origenes predeterminados, incluido
cuando se carga desde `.env`.

La aplicacion importa `.env` si existe. Tambien puedes exportar las variables antes de ejecutar:

```bash
# bash
set -a; source .env; set +a
./mvnw spring-boot:run
```

```powershell
# PowerShell
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2]) }
}
.\mvnw.cmd spring-boot:run
```

Antes del primer despliegue ejecuta [docs/schema.sql](docs/schema.sql) en una **base nueva**
de PostgreSQL/Supabase. Crea `usuarios`, `roles`, `usuario_roles`, `organizadores` con UUID
propio y las tablas de negocio. No migra ni borra datos existentes. Hibernate usa
`spring.jpa.hibernate.ddl-auto=validate`: verifica el esquema, no lo crea ni lo altera.
Para habilitar el primer ADMIN, registra una cuenta y sigue el SQL comentado al final del
script o [docs/auth.md](docs/auth.md).

Variables de seguridad:

| Variable | Valor predeterminado | Descripcion |
|---|---|---|
| `JWT_SECRET` | *(clave de desarrollo)* | Clave HS256 para firmar tokens, minimo 32 caracteres. **Obligatoria en produccion.** |
| `JWT_EXPIRATION_MINUTES` | `120` | Vigencia del token de acceso. |

## Documentacion interactiva

### Esquema existente: cliente opcional

Si crear un evento devuelve `null value in column "cliente" ... violates not-null constraint`,
ejecuta [este script SQL](docs/sql/2026-09-19_eventos_cliente_nullable.sql) en el SQL Editor
de la base de datos Supabase usada por el backend. El contrato permite omitir `cliente`,
pero una tabla existente puede conservar la restriccion `NOT NULL`.
El script elimina esa restriccion sin cambiar los datos y consulta `is_nullable`, que debe
mostrar `YES`. Luego vuelve a intentar crear el evento; este cambio no requiere redespliegue.

Con la aplicacion en ejecucion:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI: http://localhost:8080/api-docs

## Endpoints

Todas las respuestas exitosas usan el formato `{ success, message, data, timestamp }`. Los
errores usan `{ success: false, message, timestamp }` (409 de conflicto de capacidad agrega
ademas `plannedHours`, `limitHours`, `exceedsBy`).

Todas las rutas `/api/**` exigen `Authorization: Bearer <token>`, excepto registro y login.
Los permisos se consultan en BD en cada petición. Las rutas de negocio exigen ORGANIZADOR
con perfil activo. El JWT identifica al usuario; el UUID del organizador es independiente.
La eliminación devuelve 409 si existen eventos/capacidades o se trata del último ADMIN habilitado.

| Metodo | Ruta | US | Descripcion |
|---|---|---|---|
| `POST` | `/api/auth/register` | US-11 | Publica. Crea un usuario (rol ORGANIZADOR) y devuelve su token. |
| `POST` | `/api/auth/login` | US-11 | Publica. Devuelve un token de acceso. |
| `POST` | `/api/auth/logout` | US-11 | Revoca el Bearer actual; el frontend borra su token local. |
| `GET` | `/api/auth/me` | US-11 | Usuario dueno del token. |
| `GET` | `/api/admin/users` | - | ADMIN: lista los usuarios. |
| `GET` | `/api/admin/users/{id}` | - | ADMIN: consulta un usuario. |
| `POST` | `/api/admin/users` | - | ADMIN: crea cuenta y roles, sin emitir token. |
| `PATCH` | `/api/admin/users/{id}` | - | ADMIN: edita datos, roles y actividad del perfil. |
| `DELETE` | `/api/admin/users/{id}` | - | ADMIN: elimina cuenta sin datos asociados. |
| `PATCH` | `/api/auth/me` | - | Edita perfil propio; cambio de password exige passwordActual. |
| `DELETE` | `/api/auth/me` | - | Elimina cuenta propia sin datos asociados. |
| `POST` | `/api/events` | US-01, US-02 | Crea un evento y, opcionalmente, sus subtareas iniciales. |
| `GET` | `/api/events` | US-01 | Lista los eventos del usuario autenticado. |
| `GET` | `/api/events/{id}` | US-01 | Consulta un evento con sus subtareas. |
| `PATCH` | `/api/events/{id}` | US-03 | Actualiza los campos enviados de un evento. |
| `DELETE` | `/api/events/{id}` | US-03 | Elimina un evento y sus subtareas (cascada). |
| `GET` | `/api/events/{id}/progress` | US-10 | Progreso de preparacion (`done`/`total`/`percentage`). |
| `POST` | `/api/events/{eventId}/subtasks` | US-02 | Crea una subtarea logistica del evento. |
| `GET` | `/api/events/{eventId}/subtasks` | US-02 | Lista las subtareas de un evento. |
| `PATCH` | `/api/subtasks/{id}` | US-03, US-06, US-07 | Edita nombre/fecha/horas; valida sobrecarga si cambian fecha u horas. |
| `PATCH` | `/api/subtasks/{id}/status` | US-09 | Marca DONE o POSTPONED (con nota opcional). |
| `DELETE` | `/api/subtasks/{id}` | US-03 | Elimina una subtarea. |
| `GET` | `/api/today?eventId=&status=` | US-04, US-05 | Subtareas no DONE agrupadas en Vencidas/Para hoy/Proximas, con filtros. |
| `POST` | `/api/subtasks/{id}/conflicts/overload` | US-07, US-08 | Previsualiza sobrecarga sin guardar el cambio. |
| `GET` | `/api/capacity` | US-12 | Limite diario configurado (6h por defecto). |
| `PUT` | `/api/capacity` | US-12 | Actualiza el limite diario (rango 1..16). |

### Regla de agrupacion/orden de "Hoy" (US-04)

Vencidas, luego Para hoy, luego Proximas. Dentro de cada grupo: por fecha (mas antigua o mas
cercana primero) y, en empate, por menor esfuerzo (`horasEstimadas`). Se excluyen las
subtareas `DONE`. La respuesta de `/api/today` incluye el texto de la regla en el campo
`regla`.

### Conflicto de sobrecarga diaria (US-07)

Al editar `fechaObjetivo` y/o `horasEstimadas` de una subtarea (via `PATCH /api/subtasks/{id}`
o al previsualizar con `POST /api/subtasks/{id}/conflicts/overload`), se suman las horas no
`DONE` planificadas para ese organizador en la fecha destino (excluyendo la propia subtarea) y
se compara contra el limite diario (US-12, 6h por defecto). Si se supera, `PATCH` responde
`409` sin guardar el cambio; el cliente resuelve reintentando con otra fecha (mover) o con
menos horas (reducir), que es la misma operacion sin conflicto.

## Pruebas y cobertura

```bash
./mvnw verify
```

Ejecuta las pruebas unitarias (dominio y casos de uso), las reglas de ArchUnit y el chequeo de
cobertura de JaCoCo. El reporte HTML se genera en `target/site/jacoco/index.html`.

Para comprobar el SQL, JPA y flujos HTTP sobre PostgreSQL, crea una base **vacía y desechable**
con nombre `events_test_*` y ejecuta:

```bash
TEST_DB_URL=jdbc:postgresql://localhost:5432/events_test_users \
TEST_DB_USERNAME=postgres TEST_DB_PASSWORD=postgres ./mvnw -Ppostgres-it verify
```

Este perfil aplica `docs/schema.sql`, valida el modelo JPA y limpia los datos entre pruebas.
Necesita una base vacía nueva en cada ejecución; no debe apuntar a la base de la aplicación.
Las pruebas verifican aislamiento multiusuario, CRUD/perfil, roles actuales con JWT existente,
contraseñas BCrypt, bloqueos de eliminación y concurrencia de registros/bajas de ADMIN.

El umbral de cobertura (60% lineas / 50% ramas) aplica solo sobre dominio y capa de
aplicacion (la logica de negocio real); DTOs, mappers, controladores, adaptadores de
persistencia y configuracion quedan fuera del calculo por ser codigo de paso con poco valor
en pruebas unitarias aisladas.

## Arquitectura

```text
src/main/java/com/events
├── domain          Entidades (con anotaciones JPA) y excepciones de negocio
├── application     Puertos de entrada, puertos de salida y casos de uso
└── infrastructure  Controladores REST, DTO, mapeadores, persistencia JPA y configuracion
```

Los controladores dependen de puertos de entrada; los casos de uso acceden a la persistencia
mediante puertos de salida. Las entidades de dominio llevan anotaciones JPA directamente (en
vez de una entidad de persistencia separada) para no duplicar clases dado el alcance del
mini-proyecto; ArchUnit sigue verificando que el dominio no dependa de Spring, Bean Validation
ni Lombok.

## Docker

```bash
./mvnw clean package
docker build -t events-api .
docker run --rm -p 8080:8080 --env-file .env events-api
```

Para actualizar una base que ya usa el esquema actual, ejecutar
[docs/migrations/001_logout.sql](docs/migrations/001_logout.sql) antes de desplegar logout.
Las bases nuevas incluyen `tokens_revocados` en `docs/schema.sql`. El cierre de sesión
invalida el JWT actual en BD, incluso tras reinicios y entre instancias; otros tokens siguen
vigentes. Detalle y respuestas JSON en [docs/auth.md](docs/auth.md).
