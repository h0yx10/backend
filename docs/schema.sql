-- =============================================================================
-- events-api - Esquema de base de datos (PostgreSQL / Supabase)
-- Modelo de negocio + autenticacion con Spring Security:
--   usuarios (login: nombre, correo, password_hash)
--   organizadores (perfil 1:1 con usuarios: solo usuario_id PK/FK -> usuarios.id, y activo)
--   roles, organizador_roles (usuario_id -> organizadores.usuario_id)
--
-- El script es idempotente: sirve tanto para una base nueva como para migrar la base
-- anterior, donde organizadores guardaba nombre/correo/password_hash (esos datos se copian
-- a usuarios conservando el mismo id, asi que eventos.organizador_id no cambia).
-- Ejecutalo en Supabase > SQL Editor ANTES de desplegar el backend con esta version.
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. Usuarios: tabla que registra el login (correo + password con hash BCrypt).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre         VARCHAR(120) NOT NULL,
    correo         VARCHAR(180) NOT NULL UNIQUE,
    password_hash  VARCHAR(100),
    created_at     TIMESTAMP    DEFAULT now()
);

-- El backend guarda el correo en minusculas; este indice evita duplicados por mayusculas
-- si alguien inserta datos a mano.
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_correo_lower ON usuarios (lower(correo));

-- Migracion: si organizadores aun tiene las columnas de login, copiarlas a usuarios.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'organizadores' AND column_name = 'correo') THEN
        INSERT INTO usuarios (id, nombre, correo, password_hash, created_at)
        SELECT id, nombre, correo, password_hash, COALESCE(created_at, now())
        FROM organizadores
        ON CONFLICT (id) DO NOTHING;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Roles
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre  VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT ck_roles_nombre CHECK (nombre IN ('ORGANIZADOR', 'ADMIN'))
);

INSERT INTO roles (nombre) VALUES ('ORGANIZADOR'), ('ADMIN')
ON CONFLICT (nombre) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. Organizadores: perfil de organizador. Solo el usuario (PK + FK a usuarios, 1 a 1) y activo.
--    Su id es el mismo del usuario; eventos.organizador_id sigue apuntando aqui.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organizadores (
    usuario_id  UUID    PRIMARY KEY REFERENCES usuarios (id) ON DELETE CASCADE,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

-- Migracion: la tabla antigua tenia id + nombre + correo + password_hash + created_at.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'organizadores' AND column_name = 'id') THEN
        ALTER TABLE organizadores RENAME COLUMN id TO usuario_id;
    END IF;
END $$;
ALTER TABLE organizadores DROP COLUMN IF EXISTS nombre;
ALTER TABLE organizadores DROP COLUMN IF EXISTS correo;
ALTER TABLE organizadores DROP COLUMN IF EXISTS password_hash;
ALTER TABLE organizadores DROP COLUMN IF EXISTS created_at;
ALTER TABLE organizadores ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;

-- Si venia de la tabla antigua, la PK no tenia FK hacia usuarios: agregarla.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                   WHERE conrelid = 'organizadores'::regclass AND contype = 'f'
                     AND confrelid = 'usuarios'::regclass) THEN
        ALTER TABLE organizadores
            ADD CONSTRAINT fk_organizadores_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 3b. organizador_roles (N:M): apunta a organizadores.usuario_id (antes organizador_id).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organizador_roles (
    usuario_id  UUID NOT NULL REFERENCES organizadores (usuario_id) ON DELETE CASCADE,
    rol_id      UUID NOT NULL REFERENCES roles (id)                 ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

-- Migracion: la columna antigua organizador_id pasa a llamarse usuario_id. Su FK ya seguia a la
-- PK renombrada de organizadores, asi que queda apuntando a organizadores.usuario_id.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'organizador_roles' AND column_name = 'organizador_id') THEN
        ALTER TABLE organizador_roles RENAME COLUMN organizador_id TO usuario_id;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 4. Modelo de negocio: cada usuario es dueno de sus eventos.
--    eventos.organizador_id -> organizadores.usuario_id (NOT NULL). Las subtareas pertenecen al
--    usuario a traves de su evento, y la capacidad diaria es por usuario.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eventos (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre            VARCHAR(180) NOT NULL,
    tipo              VARCHAR(100) NOT NULL,
    cliente           VARCHAR(180),
    contacto_cliente  VARCHAR(180),
    fecha_hora        TIMESTAMP    NOT NULL,
    lugar             VARCHAR(240),
    plazo_limite      TIMESTAMP,
    organizador_id    UUID         NOT NULL REFERENCES organizadores (usuario_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subtareas (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre           VARCHAR(180)  NOT NULL,
    descripcion      VARCHAR(255),
    fecha_objetivo   DATE          NOT NULL,
    horas_estimadas  NUMERIC(8, 2) NOT NULL CHECK (horas_estimadas > 0),
    estado           VARCHAR(20)   NOT NULL CHECK (estado IN ('PENDING', 'DONE', 'POSTPONED')),
    nota             VARCHAR(1000),
    done_at          TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL DEFAULT now(),
    evento_id        UUID          NOT NULL REFERENCES eventos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS capacidades_diarias (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    organizador_id  UUID          NOT NULL REFERENCES organizadores (usuario_id) ON DELETE CASCADE,
    fecha           DATE          NOT NULL,
    limite_horas    NUMERIC(8, 2) NOT NULL CHECK (limite_horas BETWEEN 1 AND 16),
    CONSTRAINT uk_capacidad_organizador_fecha UNIQUE (organizador_id, fecha)
);

-- En bases ya existentes, garantiza que todo evento tenga dueno.
ALTER TABLE eventos ALTER COLUMN organizador_id SET NOT NULL;

-- Indices para las consultas filtradas por usuario (todas las rutas protegidas filtran asi).
CREATE INDEX IF NOT EXISTS ix_eventos_organizador             ON eventos (organizador_id);
CREATE INDEX IF NOT EXISTS ix_subtareas_evento                ON subtareas (evento_id);
CREATE INDEX IF NOT EXISTS ix_subtareas_evento_fecha_estado   ON subtareas (evento_id, fecha_objetivo, estado);
CREATE INDEX IF NOT EXISTS ix_capacidades_organizador_fecha   ON capacidades_diarias (organizador_id, fecha DESC);

-- -----------------------------------------------------------------------------
-- 5. Usuarios existentes sin rol (ej. el antiguo "Organizador Demo") reciben ORGANIZADOR.
--    Ojo: el demo no tiene password_hash, por lo que NO puede iniciar sesion.
-- -----------------------------------------------------------------------------
INSERT INTO organizador_roles (usuario_id, rol_id)
SELECT o.usuario_id, r.id
FROM organizadores o
CROSS JOIN roles r
WHERE r.nombre = 'ORGANIZADOR'
  AND NOT EXISTS (SELECT 1 FROM organizador_roles x WHERE x.usuario_id = o.usuario_id)
ON CONFLICT DO NOTHING;

COMMIT;

-- =============================================================================
-- OPERACIONES MANUALES (ejecutar segun necesidad, fuera de la transaccion anterior)
-- =============================================================================

-- A) Pasar los eventos y la capacidad del organizador demo a un usuario real ya registrado
--    (registrate primero por POST /api/auth/register y reemplaza el correo):
--
-- UPDATE eventos
--    SET organizador_id = (SELECT id FROM usuarios WHERE correo = 'tu-correo@dominio.com')
--  WHERE organizador_id = (SELECT id FROM usuarios WHERE correo = 'demo@organizador.local');
-- DELETE FROM capacidades_diarias
--  WHERE organizador_id = (SELECT id FROM usuarios WHERE correo = 'demo@organizador.local');
-- DELETE FROM usuarios WHERE correo = 'demo@organizador.local';  -- borra su organizador en cascada

-- B) Otorgar rol ADMIN a un usuario (necesario para GET /api/admin/users).
--    El usuario debe volver a iniciar sesion para que el nuevo rol viaje en su token.
--    Los roles cuelgan de organizadores: el usuario debe tener fila en organizadores
--    (si no, primero: INSERT INTO organizadores (usuario_id) SELECT id FROM usuarios WHERE correo = '...').
--
-- INSERT INTO organizador_roles (usuario_id, rol_id)
-- SELECT u.id, r.id FROM usuarios u, roles r
--  WHERE u.correo = 'admin@dominio.com' AND r.nombre = 'ADMIN'
-- ON CONFLICT DO NOTHING;

-- C) Desactivar un organizador (ya no podra iniciar sesion; los tokens emitidos expiran solos):
--
-- UPDATE organizadores SET activo = FALSE
--  WHERE usuario_id = (SELECT id FROM usuarios WHERE correo = 'usuario@dominio.com');

-- D) Opcional, una vez migrado el demo: exigir password en todos los usuarios.
--
-- ALTER TABLE usuarios ALTER COLUMN password_hash SET NOT NULL;
