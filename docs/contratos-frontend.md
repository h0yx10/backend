# Contratos API para el cliente front - events-api

Referencia completa para que el front se autentique y consuma la API. Para el detalle por
recurso ver los demas archivos de `docs/`; Swagger en `/swagger-ui.html` (boton **Authorize**
para pegar el token).

- **Base URL:** `http://localhost:8080` (local) o la URL del backend desplegado.
- **Content-Type:** `application/json` en todos los requests con body.
- **Fechas:** `date` = `"yyyy-MM-dd"`; `datetime` = `"yyyy-MM-ddTHH:mm:ss"` **sin zona horaria**
  (hora local America/Bogota). `timestamp` del sobre de respuesta = Instant ISO-8601 en UTC.
- **IDs:** UUID en texto.
- **CORS:** origenes permitidos por `CORS_ORIGIN` (por defecto `http://localhost:4300` y
  `https://frontend-pi-olive-30.vercel.app`). El header `Authorization` esta permitido.

---

## 1. Flujo de autenticacion

1. El usuario se registra (`POST /api/auth/register`) o inicia sesion (`POST /api/auth/login`).
2. La respuesta trae `data.accessToken` y `data.expiresIn` (segundos). Guarda el token
   (ej. `localStorage`/`sessionStorage`) y calcula `expiresAt = Date.now() + expiresIn * 1000`.
3. En **cada** request a `/api/**` (excepto register/login) envia:
   ```
   Authorization: Bearer <accessToken>
   ```
4. Al recargar la app: si hay token no expirado, llama a `GET /api/auth/me` para restaurar el
   usuario. Si responde `401`, borra el token y manda a login.
5. Cualquier `401` en una ruta protegida = sesion expirada o token invalido → borrar token y
   redirigir a login. Un `403` = el usuario no tiene el rol (no cerrar sesion).
6. Logout: borrar el token en el cliente (no hay endpoint de logout; el token es stateless).
7. No hay refresh token: al expirar (por defecto 2 h) el usuario vuelve a iniciar sesion.

Cada usuario solo ve sus propios eventos/subtareas/capacidad. Pedir un recurso de otro
usuario devuelve `404`.

### Cambio de modelo de datos (sin cambios en el JSON)

El backend separo la cuenta de login del perfil de organizador:

- `usuarios`: `id`, `nombre`, `correo`, `password_hash`, `created_at` .
- `organizadores`: solo `usuario_id` (PK y FK a `usuarios.id`, relacion 1 a 1) y `activo`.
- `organizador_roles`: `usuario_id` (FK a `organizadores.usuario_id`) + `rol_id`. Los roles se leen del organizador.

Los endpoints, requests y responses **no cambian**: `POST /api/auth/register` ahora guarda
nombre, correo y password en `usuarios` y crea el perfil en `organizadores`. Como el id del
organizador es el mismo del usuario, `UsuarioResponse.id`, `EventoResponse.organizadorId` y el
`sub` del JWT siguen coincidiendo. Un login de cuenta con `organizadores.activo = false` responde
`401` igual que una contrasena incorrecta.

---

## 2. Sobre de respuesta

```ts
// Respuesta exitosa (2xx)
interface ApiResponse<T> {
  success: true;
  message: string;
  data: T;            // null en DELETE
  timestamp: string;  // ISO-8601 UTC
}

// Respuesta de error (4xx / 5xx)
interface ApiError {
  success: false;
  message: string;    // mensaje listo para mostrar al usuario
  timestamp: string;
}

// 409 por sobrecarga de capacidad (PATCH /api/subtasks/{id})
interface CapacityConflictError extends ApiError {
  plannedHours: number;
  limitHours: number;
  exceedsBy: number;
}
```

| Status | Significado | Que hacer en el front |
|---|---|---|
| 200 / 201 | OK | usar `data` |
| 400 | validacion o JSON mal formado | mostrar `message` en el formulario |
| 401 | sin token, token invalido/expirado, o login fallido | en login: mostrar `message`; en otra ruta: cerrar sesion |
| 403 | rol insuficiente | mostrar "sin permisos" |
| 404 | no existe o no es del usuario | mostrar "no encontrado" |
| 409 | correo ya registrado / sobrecarga diaria | mostrar `message` (y datos extra si es capacidad) |
| 500 | error inesperado | mensaje generico |

---

## 3. Tipos (TypeScript)

```ts
type UUID = string;
type DateISO = string;      // "2026-05-01"
type DateTimeISO = string;  // "2026-05-20T18:00:00"

type Rol = 'ORGANIZADOR' | 'ADMIN';
type EstadoSubtarea = 'PENDING' | 'DONE' | 'POSTPONED';

// ---------- Auth ----------
interface RegisterRequest {
  nombre: string;    // requerido, max 120
  correo: string;    // requerido, email valido, max 180
  password: string;  // requerido, 8..72 caracteres
}

interface LoginRequest {
  correo: string;
  password: string;
}

// Viene de la tabla `usuarios` (login). `id` es tambien el id del organizador.
interface UsuarioResponse {
  id: UUID;            // = usuarios.id = organizadores.usuario_id = claim `sub` del JWT
  nombre: string;
  correo: string;
  roles: Rol[];    // de organizador_roles
  activo: boolean;     // organizadores.activo
  createdAt: DateTimeISO | null;
}

interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;   // segundos
  usuario: UsuarioResponse;
}

// ---------- Eventos ----------
interface SubtareaInicialRequest {
  nombre: string;          // requerido
  fechaObjetivo: DateISO;  // requerido
  horasEstimadas: number;  // requerido, > 0
}

interface CreateEventoRequest {
  nombre: string;              // requerido, max 180
  tipo: string;                // requerido
  cliente?: string;
  contactoCliente?: string;
  fechaHora: DateTimeISO;      // requerido
  lugar?: string;
  plazoLimite?: DateTimeISO;
  subtareas?: SubtareaInicialRequest[];
}

// PATCH: solo se aplican los campos enviados (no se puede "borrar" un campo enviando null)
interface UpdateEventoRequest {
  nombre?: string;
  tipo?: string;
  cliente?: string;
  contactoCliente?: string;
  fechaHora?: DateTimeISO;
  lugar?: string;
  plazoLimite?: DateTimeISO;
}

interface EventoResponse {
  id: UUID;
  nombre: string;
  tipo: string;
  cliente: string | null;
  contactoCliente: string | null;
  fechaHora: DateTimeISO;
  lugar: string | null;
  plazoLimite: DateTimeISO | null;
  organizadorId: UUID;         // siempre el usuario autenticado (= UsuarioResponse.id)
  subtareas: SubtareaResponse[];
}

interface ProgressResponse {
  done: number;
  total: number;
  percentage: number;          // 0..100
}

// ---------- Subtareas ----------
interface CreateSubtareaRequest {
  nombre: string;              // requerido
  fechaObjetivo: DateISO;      // requerido
  horasEstimadas: number;      // requerido, > 0
}

interface UpdateSubtareaRequest {   // PATCH parcial
  nombre?: string;
  fechaObjetivo?: DateISO;          // si cambia fecha u horas se valida sobrecarga (409)
  horasEstimadas?: number;          // > 0
}

interface ChangeSubtareaStatusRequest {
  estado: EstadoSubtarea;           // requerido
  nota?: string;                    // opcional (tipicamente al posponer)
}

interface SubtareaResponse {
  id: UUID;
  eventoId: UUID;
  nombre: string;
  fechaObjetivo: DateISO;
  horasEstimadas: number;
  estado: EstadoSubtarea;
  nota: string | null;
  doneAt: DateTimeISO | null;
  createdAt: DateTimeISO;
}

// ---------- Conflictos ----------
interface OverloadCheckRequest {
  fechaObjetivo?: DateISO;    // si se omite, usa la actual de la subtarea
  horasEstimadas?: number;    // si se omite, usa las actuales
}

interface OverloadCheckResponse {
  conflict: boolean;
  plannedHours: number;
  limitHours: number;
  exceedsBy: number;          // 0 si no hay conflicto
}

// ---------- Capacidad ----------
interface CapacidadRequest {
  limiteHoras: number;        // requerido, 1..16
}

interface CapacidadResponse {
  limiteHoras: number;
  porDefecto: boolean;        // true si nunca se configuro (6h)
  fecha: DateISO | null;
}

// ---------- Hoy ----------
interface TodayResponse {
  vencidas: SubtareaResponse[];
  paraHoy: SubtareaResponse[];
  proximas: SubtareaResponse[];
  regla: string;              // texto explicativo del orden
}
```

---

## 4. Endpoints

🔓 = publico · 🔒 = requiere `Authorization: Bearer` · 🛡️ = requiere rol ADMIN

### Autenticacion

| | Metodo | Ruta | Body | Respuesta `data` | Errores |
|---|---|---|---|---|---|
| 🔓 | POST | `/api/auth/register` | `RegisterRequest` | **201** `AuthResponse` | 400, 409 |
| 🔓 | POST | `/api/auth/login` | `LoginRequest` | 200 `AuthResponse` | 400, 401 |
| 🔒 | GET | `/api/auth/me` | - | 200 `UsuarioResponse` | 401 |
| 🛡️ | GET | `/api/admin/users` | - | 200 `UsuarioResponse[]` | 401, 403 |

### Eventos

| | Metodo | Ruta | Body | Respuesta `data` | Errores |
|---|---|---|---|---|---|
| 🔒 | GET | `/api/events` | - | 200 `EventoResponse[]` | 401 |
| 🔒 | GET | `/api/events/{id}` | - | 200 `EventoResponse` | 401, 404 |
| 🔒 | POST | `/api/events` | `CreateEventoRequest` | **201** `EventoResponse` | 400, 401 |
| 🔒 | PATCH | `/api/events/{id}` | `UpdateEventoRequest` | 200 `EventoResponse` | 400, 401, 404 |
| 🔒 | DELETE | `/api/events/{id}` | - | 200 `null` (borra sus subtareas) | 401, 404 |
| 🔒 | GET | `/api/events/{id}/progress` | - | 200 `ProgressResponse` | 401, 404 |

### Subtareas

| | Metodo | Ruta | Body | Respuesta `data` | Errores |
|---|---|---|---|---|---|
| 🔒 | POST | `/api/events/{eventId}/subtasks` | `CreateSubtareaRequest` | **201** `SubtareaResponse` | 400, 401, 404 |
| 🔒 | GET | `/api/events/{eventId}/subtasks` | - | 200 `SubtareaResponse[]` | 401, 404 |
| 🔒 | PATCH | `/api/subtasks/{id}` | `UpdateSubtareaRequest` | 200 `SubtareaResponse` | 400, 401, 404, **409** `CapacityConflictError` |
| 🔒 | PATCH | `/api/subtasks/{id}/status` | `ChangeSubtareaStatusRequest` | 200 `SubtareaResponse` | 400, 401, 404 |
| 🔒 | DELETE | `/api/subtasks/{id}` | - | 200 `null` | 401, 404 |
| 🔒 | POST | `/api/subtasks/{id}/conflicts/overload` | `OverloadCheckRequest` | 200 `OverloadCheckResponse` (no guarda) | 401, 404 |

### Capacidad diaria

| | Metodo | Ruta | Body | Respuesta `data` | Errores |
|---|---|---|---|---|---|
| 🔒 | GET | `/api/capacity` | - | 200 `CapacidadResponse` | 401 |
| 🔒 | PUT | `/api/capacity` | `CapacidadRequest` | 200 `CapacidadResponse` | 400, 401 |

### Vista Hoy

| | Metodo | Ruta | Query | Respuesta `data` | Errores |
|---|---|---|---|---|---|
| 🔒 | GET | `/api/today` | `eventId?: UUID`, `status?: EstadoSubtarea` | 200 `TodayResponse` | 401 |

---

## 5. Ejemplos

### Registro

```http
POST /api/auth/register
Content-Type: application/json

{ "nombre": "Camila Restrepo", "correo": "camila@correo.com", "password": "Secreta123" }
```

```json
{
  "success": true,
  "message": "La cuenta se creo correctamente.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "usuario": {
      "id": "6f1c1f5e-7d0a-4c55-9a43-2b9f0f1e9c11",
      "nombre": "Camila Restrepo",
      "correo": "camila@correo.com",
      "roles": ["ORGANIZADOR"],
      "activo": true,
      "createdAt": "2026-10-02T10:15:30"
    }
  },
  "timestamp": "2026-10-02T15:15:30.123Z"
}
```

### Login fallido

```json
{ "success": false, "message": "Correo o contrasena incorrectos.", "timestamp": "2026-10-02T15:16:00Z" }
```

### Ruta protegida sin token / token expirado

```json
{ "success": false, "message": "Debes iniciar sesion para acceder a este recurso.", "timestamp": "..." }
{ "success": false, "message": "Tu sesion expiro o el token no es valido. Inicia sesion nuevamente.", "timestamp": "..." }
```

### Crear evento autenticado

```http
POST /api/events
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "nombre": "Boda Camila y Andres",
  "tipo": "Social",
  "fechaHora": "2026-12-20T18:00:00",
  "lugar": "Club Campestre",
  "subtareas": [
    { "nombre": "Reservar salon", "fechaObjetivo": "2026-11-10", "horasEstimadas": 3 }
  ]
}
```

---

## 6. Implementacion sugerida en el cliente

### Angular (interceptor funcional, Angular 15+)

```ts
// auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

const PUBLIC = ['/api/auth/login', '/api/auth/register'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('accessToken');
  const isPublic = PUBLIC.some(p => req.url.includes(p));

  const authReq = token && !isPublic
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isPublic) {
        localStorage.removeItem('accessToken');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};

// app.config.ts -> provideHttpClient(withInterceptors([authInterceptor]))
```

```ts
// auth.service.ts (resumen)
login(body: LoginRequest) {
  return this.http.post<ApiResponse<AuthResponse>>(`${API}/api/auth/login`, body).pipe(
    tap(r => {
      localStorage.setItem('accessToken', r.data.accessToken);
      localStorage.setItem('expiresAt', String(Date.now() + r.data.expiresIn * 1000));
    })
  );
}
isLoggedIn() { return Number(localStorage.getItem('expiresAt') ?? 0) > Date.now(); }
hasRole(user: UsuarioResponse, rol: Rol) { return user.roles.includes(rol); }
```

### fetch (cualquier framework)

```ts
async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('accessToken');
  const res = await fetch(`${API}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  const body = await res.json();
  if (!res.ok) {
    if (res.status === 401 && !path.startsWith('/api/auth/')) {
      localStorage.removeItem('accessToken');
      location.href = '/login';
    }
    throw body as ApiError;
  }
  return (body as ApiResponse<T>).data;
}
```

### Guards de rutas en el front

- Rutas privadas: exigir `isLoggedIn()`; si no, redirigir a `/login`.
- Rutas de administracion: exigir `usuario.roles.includes('ADMIN')` (el backend igual devuelve
  403 si no lo tiene; el guard es solo para UX).
