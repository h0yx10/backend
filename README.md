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

Cubre el backend de las historias US-01 a US-10 y US-12 con un **organizador demo** (modo
Sprint 0-1 del mini-proyecto: sin login). US-11 (autenticacion) y TS-04 quedan para una fase
posterior; el punto de extension ya existe (`CurrentOrganizadorPort` /
`DemoOrganizadorProvider`), por lo que agregar login no requiere tocar los casos de uso.

## Requisitos

- JDK 21
- Maven 3.9 o una version posterior (o usa el `mvn` de tu IDE)
- Un proyecto de Supabase (o cualquier Postgres accesible)

## Configuracion

Copia `.env.example` a `.env` y completa los valores de tu base de datos. `.env` esta en
`.gitignore`: nunca subas credenciales reales al repositorio.

| Variable | Valor predeterminado | Descripcion |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto en el que se ejecuta la API. |
| `CORS_ORIGIN` | `http://localhost:4200` | Origen autorizado para peticiones desde el frontend. |
| `DB_URL` | *(localhost, no funcional)* | Cadena JDBC de conexion a Postgres/Supabase. |
| `DB_USERNAME` | *(localhost, no funcional)* | Usuario de la base de datos. |
| `DB_PASSWORD` | *(localhost, no funcional)* | Password de la base de datos. |

Como Spring Boot no carga `.env` automaticamente, exporta las variables antes de ejecutar:

```bash
# bash
set -a; source .env; set +a
mvn spring-boot:run
```

```powershell
# PowerShell
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2]) }
}
mvn spring-boot:run
```

Al arrancar, la app crea (una sola vez) un organizador demo en la tabla `organizadores` y lo
reutiliza en cada peticion; las tablas se crean/actualizan automaticamente
(`spring.jpa.hibernate.ddl-auto=update`) sobre el esquema ya existente en Supabase
(`organizadores`, `eventos`, `subtareas`, `capacidades_diarias`).

## Documentacion interactiva

Con la aplicacion en ejecucion:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI: http://localhost:8080/api-docs

## Endpoints

Todas las respuestas exitosas usan el formato `{ success, message, data, timestamp }`. Los
errores usan `{ success: false, message, timestamp }` (409 de conflicto de capacidad agrega
ademas `plannedHours`, `limitHours`, `exceedsBy`).

| Metodo | Ruta | US | Descripcion |
|---|---|---|---|
| `POST` | `/api/events` | US-01, US-02 | Crea un evento y, opcionalmente, sus subtareas iniciales. |
| `GET` | `/api/events` | US-01 | Lista los eventos del organizador demo. |
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
mvn verify
```

Ejecuta las pruebas unitarias (dominio y casos de uso), las reglas de ArchUnit y el chequeo de
cobertura de JaCoCo. El reporte HTML se genera en `target/site/jacoco/index.html`.

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
mvn clean package
docker build -t events-api .
docker run --rm -p 8080:8080 --env-file .env events-api
```
