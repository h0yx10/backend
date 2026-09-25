# Subtareas

Plan de trabajo logistico, reprogramacion y ejecucion (US-02, US-03, US-06, US-09).

---

## POST /api/events/{eventId}/subtasks

**Descripcion:** crea una subtarea logistica para un evento (US-02).

**Path params**

| Nombre | Tipo |
|---|---|
| `eventId` | UUID |

**Request body** (`CreateSubtareaRequest`)

| Campo | Tipo | Requerido | Validacion |
|---|---|---|---|
| `nombre` | string | si | no vacio |
| `fechaObjetivo` | date ISO `yyyy-MM-dd` | si | - |
| `horasEstimadas` | number (decimal) | si | > 0 |
| `descripcion` | string | no | maximo 500 caracteres |

**Response 201** - `data`: `SubtareaResponse`.

**Response 400** si falla alguna validacion. **Response 404** si el evento no existe.

---

## GET /api/events/{eventId}/subtasks

**Descripcion:** lista las subtareas logisticas de un evento.

**Path params**

| Nombre | Tipo |
|---|---|
| `eventId` | UUID |

**Response 200** - `data`: array de `SubtareaResponse`.

---

## PATCH /api/subtasks/{id}

**Descripcion:** actualiza los campos enviados de una subtarea. Enviar `fechaObjetivo` y/o
`horasEstimadas` reprograma la subtarea (US-06) y valida sobrecarga diaria contra el limite
configurado en [capacidad.md](./capacidad.md) (US-07); si se supera, la operacion se rechaza con
**409** y no se guarda el cambio.

**Path params**

| Nombre | Tipo |
|---|---|
| `id` | UUID de la subtarea |

**Request body** (`UpdateSubtareaRequest`, todos los campos opcionales)

| Campo | Tipo | Validacion |
|---|---|---|
| `nombre` | string | - |
| `fechaObjetivo` | date ISO `yyyy-MM-dd` | - |
| `horasEstimadas` | number (decimal) | > 0 si se envia |
| `descripcion` | string | - |

**Response 200** - `data`: `SubtareaResponse`.

**Response 409** (contrato especial de conflicto, ver [README.md](./README.md#sobre-de-respuesta-estandar))
si la reprogramacion supera el limite diario. **Response 404** si la subtarea no existe.

---

## PATCH /api/subtasks/{id}/status

**Descripcion:** marca una subtarea como hecha o pospuesta, con nota opcional (US-09).

**Path params**

| Nombre | Tipo |
|---|---|
| `id` | UUID de la subtarea |

**Request body** (`ChangeSubtareaStatusRequest`)

| Campo | Tipo | Requerido | Notas |
|---|---|---|---|
| `estado` | enum: `PENDING`, `DONE`, `POSTPONED` | si | - |
| `nota` | string | no | usada sobre todo al posponer |

**Response 200** - `data`: `SubtareaResponse`.

**Response 404** si la subtarea no existe.

---

## DELETE /api/subtasks/{id}

**Descripcion:** elimina una subtarea logistica (US-03).

**Path params**

| Nombre | Tipo |
|---|---|
| `id` | UUID de la subtarea |

**Response 200** - `data: null`.

**Response 404** si la subtarea no existe.

---

## Contrato `SubtareaResponse`

Usado como `data` (o como elemento de un array) en las respuestas de crear, listar, actualizar y
cambiar estado de subtarea, y dentro de `EventoResponse.subtareas`.

| Campo | Tipo | Descripcion |
|---|---|---|
| `id` | UUID | identificador de la subtarea |
| `eventoId` | UUID | evento al que pertenece |
| `nombre` | string | |
| `fechaObjetivo` | date ISO `yyyy-MM-dd` | fecha planeada de ejecucion |
| `horasEstimadas` | number (decimal) | |
| `estado` | enum: `PENDING`, `DONE`, `POSTPONED` | |
| `nota` | string \| null | |
| `doneAt` | datetime ISO \| null | momento en que se marco `DONE` |
| `createdAt` | datetime ISO | momento de creacion |
| `descripcion` | string \| null | `null` si no se ha guardado descripcion |
