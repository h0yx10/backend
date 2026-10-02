# Autenticacion

Base path: `/api/auth` (y `/api/admin` para operaciones de administrador).

Autenticacion stateless con **JWT** (HS256). El front obtiene un `accessToken` al registrarse o
iniciar sesion y lo envia en cada peticion protegida:

```
Authorization: Bearer <accessToken>
```

No hay cookies ni sesion de servidor: cerrar sesion = borrar el token en el cliente.

---

## POST /api/auth/register (publica)

Crea el usuario (tabla `usuarios`: nombre, correo, password con hash) con rol `ORGANIZADOR`, su
perfil en `organizadores` (activo) y devuelve su token (queda logueado).

**Request body** (`RegisterRequest`)

| Campo | Tipo | Requerido | Validacion |
|---|---|---|---|
| `nombre` | string | si | no vacio, max 120 |
| `correo` | string | si | email valido, max 180. Se guarda en minusculas |
| `password` | string | si | 8 a 72 caracteres |

**Response 201** - `data`: `AuthResponse`.

**Errores:** `400` validacion, `409` "Ya existe una cuenta con ese correo."

---

## POST /api/auth/login (publica)

**Request body** (`LoginRequest`)

| Campo | Tipo | Requerido |
|---|---|---|
| `correo` | string | si (no distingue mayusculas) |
| `password` | string | si |

**Response 200** - `data`: `AuthResponse`.

**Errores:** `400` validacion, `401` "Correo o contrasena incorrectos." (mismo mensaje si el
correo no existe, la contrasena falla o la cuenta esta inactiva).

---

## GET /api/auth/me (protegida)

Devuelve el usuario dueno del token. Util para restaurar la sesion al recargar el front.

**Response 200** - `data`: `UsuarioResponse`. **Errores:** `401`.

---

## GET /api/admin/users (protegida, rol ADMIN)

**Response 200** - `data`: array de `UsuarioResponse`. **Errores:** `401` sin token, `403` sin rol ADMIN.

---

## Contratos

`AuthResponse`

| Campo | Tipo | Descripcion |
|---|---|---|
| `accessToken` | string | JWT a enviar en `Authorization: Bearer ...` |
| `tokenType` | string | siempre `"Bearer"` |
| `expiresIn` | number | segundos de vigencia (por defecto 7200) |
| `usuario` | `UsuarioResponse` | usuario autenticado |

`UsuarioResponse`

| Campo | Tipo | Descripcion |
|---|---|---|
| `id` | UUID | id del usuario (es tambien el id del organizador y el `sub` del JWT) |
| `nombre` | string | |
| `correo` | string | |
| `roles` | string[] | `ORGANIZADOR` y/o `ADMIN` |
| `activo` | boolean | viene de `organizadores.activo`; `true` si el usuario no tiene perfil de organizador |
| `createdAt` | datetime ISO | puede ser `null` en cuentas anteriores al login |

Claims del JWT (por si el front quiere leerlos sin llamar a `/me`): `sub` (id), `correo`,
`nombre`, `roles`, `iss` = `events-api`, `iat`, `exp`.

## Rutas protegidas

| Ruta | Acceso |
|---|---|
| `POST /api/auth/register`, `POST /api/auth/login` | publica |
| `/swagger-ui.html`, `/api-docs/**`, `/actuator/health` | publica |
| `/api/admin/**` | rol `ADMIN` |
| cualquier otra ruta `/api/**` | token valido (cualquier rol) |

Ademas, cada recurso se filtra por el usuario del token: acceder a un evento o subtarea de
otro usuario devuelve `404`, igual que si no existiera.

Respuestas de error de seguridad (mismo formato `{ success, message, timestamp }`):

| Status | message |
|---|---|
| 401 (sin token) | `Debes iniciar sesion para acceder a este recurso.` |
| 401 (token invalido/expirado) | `Tu sesion expiro o el token no es valido. Inicia sesion nuevamente.` |
| 403 | `No tienes permisos para acceder a este recurso.` |
