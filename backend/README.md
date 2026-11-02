# ProyecTwin MVP — Backend

API REST de ProyecTwin (SENA): propuestas de proyecto, detección de similitudes,
revisión por instructores, historial, auditoría, notificaciones, fichas, equipos
y credenciales institucionales.

- **Stack:** Laravel 10 · PHP 8.2+ · MySQL · Sanctum.
- **Frontend:** `../frontend` (React + Vite).
- **Prefijo de API:** `/v1` (`routes/api-v1.php`).

## Requisitos

- MySQL 8 o MariaDB 10.4+ iniciado (el equipo actual usa MariaDB 10.4.32).
- PHP 8.2+ con `pdo_mysql`, `mbstring`, `dom`, `fileinfo`, `openssl`,
  `tokenizer`, `xml`, `ctype`, `filter`, `hash` y `session`. `gd`/`zip` son
  opcionales (imágenes PDF y conveniencia de Composer).
- Composer 2.x.
- Mailpit para probar el correo real en local.

## Instalación local (Windows)

1. Crear tu propia base local vacía:

`proyectwin_mvp` es el nombre de la base local del propietario actual y el valor
de ejemplo del `.env.example`. Cada desarrollador debe crear su propia base
vacía y ajustar `DB_DATABASE`; no se copia ni se comparte la base de otra
persona. No ejecutes migraciones ni seeders sobre una base existente con datos
sin autorización específica.

2. Instalar y configurar:

```powershell
# Desde la raíz del repositorio
cd backend

composer install
copy .env.example .env
php artisan key:generate
```

`.env.example` usa `proyectwin_mvp` solo como ejemplo. Ajusta `DB_DATABASE` al
nombre de tu base local vacía.

3. Editar `.env`:

```env
APP_ENV=local
APP_URL=http://127.0.0.1:8000
APP_DEBUG=true

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=proyectwin_mvp
DB_USERNAME=root
DB_PASSWORD=

FRONTEND_URL=http://localhost:5173
SANCTUM_STATEFUL_DOMAINS=localhost:5173,127.0.0.1:5173

MAIL_MAILER=smtp
MAIL_HOST=127.0.0.1
MAIL_PORT=1025
MAIL_USERNAME=null
MAIL_PASSWORD=null
MAIL_ENCRYPTION=null
MAIL_FROM_ADDRESS="no-responder@proyecttwin.local"
MAIL_FROM_NAME="${APP_NAME}"
```

4. Si la base es nueva, vacía y propia, preparar el esquema y la demo **solo en
   local**:

```powershell
php artisan migrate --seed
```

No ejecutes este comando sobre una base que ya contiene datos ni en producción.
En producción se usa `php artisan migrate --force` y
`php artisan admin:inicial` con un correo real.

5. Iniciar la API:

```powershell
php artisan serve --host=127.0.0.1 --port=8000
```

`demo:preparar` es una herramienta del entorno local del propietario: solo opera
sobre `proyectwin_mvp` y requiere autorización. En un equipo nuevo normalmente
no la necesitas; usa `php artisan migrate --seed` sobre tu propia base vacía.
No uses `migrate:fresh`, `db:wipe` ni truncamientos sobre la base de desarrollo.
Para una base existente exige flags explícitos (`--check-only`, `--migrar`,
`--actualizar-correos`, `--solo-credenciales`, `--sembrar`). `--check-only` es
siempre de solo lectura. En una base legacy que ya tiene `general_users` sin
`email_verified_at`, `demo:preparar --migrar` se detiene de forma intencional;
el propietario decide si aplica la migración idempotente
`2026_11_01_000001`.

## Migraciones

El historial local tiene 22 archivos en `database/migrations` más la migración
de Sanctum que Laravel carga desde `vendor/`. En total son 23 migraciones
efectivas. `create_general_users_table` incluye `email_verified_at` para bases
nuevas y `2026_11_01_000001_add_email_verified_at_to_general_users` la añade de
forma idempotente a bases anteriores. `create_account_activations_table` define
`expires_at` como `DATETIME`. No reescribas una migración ya aplicada: cualquier
cambio posterior debe ir en una migración nueva.

## Compatibilidad con bases anteriores

Ejecuta las migraciones normales (`php artisan migrate --force`). La migración
`2026_11_01_000001_add_email_verified_at_to_general_users` comprueba si la
columna existe y la añade solo cuando falta; no borra filas ni datos.

Las columnas legacy (`must_change_password`, `password_temporal`,
`credenciales_enviadas_en`, `credenciales_error`) pueden seguir presentes en una
base antigua; el código actual no las usa. Si se desea limpiarlas, hazlo con un
backup restaurable y fuera de la migración automática. **No ejecutes un ALTER
manual que añada `email_verified_at` después de `migrate`**: fallaría por columna
duplicada.

Después, si aplica a una base demo, continúa con:

```powershell
php artisan demo:preparar --migrar
php artisan demo:preparar --actualizar-correos
php artisan demo:preparar --solo-credenciales
php artisan demo:preparar --sembrar
```

Las cuentas administradas del seed se verifican y rotan con
`--solo-credenciales`. Las cuentas no administradas que existan en la base
quedan sin verificar y deben usar activación o recuperación.

## Correo local con Mailpit

Descarga el binario oficial de Mailpit desde
`https://github.com/axllent/mailpit/releases` y ejecuta:

```powershell
mailpit.exe
```

- SMTP: `127.0.0.1:1025`
- Bandeja web: `http://127.0.0.1:8025`

La activación, recuperación y aviso de cambio de correo usan las notificaciones
reales de Laravel. No hay enlace local por API. La entrega real a internet no se
prueba en local; Mailpit captura los mensajes. En producción el correo se
configura con `MAIL_MAILER=brevo` o un SMTP real.

## Cuentas demo y secretos

El seed demo crea cuentas de ejemplo **solo** en `local`, `testing` o `e2e`.
No se publican correos, usuarios ni contraseñas compartidas en la
documentación.

Para una base local nueva:

1. Define tu propia contraseña temporal en `backend/.env`:
   `DEMO_PASSWORD="<tu-clave-local>"` (entre comillas si contiene `#`).
2. Ejecuta `php artisan migrate --seed` sobre esa base vacía.
3. Consulta los identificadores definidos en
   `app/Support/CuentasDemo.php` o crea tu propia cuenta con el flujo de
   administrador.

No uses estas cuentas ni la contraseña local en producción. Para producción,
crea el administrador con `php artisan admin:inicial`, un correo real y el
procedimiento de `../docs/deploy-railway.md`.

La activación de una cuenta nueva se prueba creando el usuario desde el flujo
real del administrador y abriendo el enlace recibido en Mailpit. Las
migraciones no marcan cuentas existentes como verificadas.

## Seeders

`DatabaseSeeder` llama a `DemoSeeder`, que contiene el escenario completo. Los
registros demo se identifican en `demo_seed_records`; una segunda ejecución no
duplica ni sobrescribe usuarios reales. El escenario incluye usuarios, redes,
programas, fichas, aprendices, propuestas en todos los estados, integrantes,
similitudes calculadas por el motor, comentarios, historial, auditoría,
notificaciones y reportes.

## Backup y restauración

> Los comandos de esta sección usan `proyectwin_mvp` como ejemplo. Sustituye el
> nombre por el de tu base local; no apuntes a la base de otra persona.

No uses la redirección `>` de PowerShell para crear el respaldo: puede cambiar
la codificación del SQL. Usa `--result-file`:

```powershell
New-Item -ItemType Directory -Force $env:USERPROFILE\backups

mysqldump -u root --single-transaction --routines --triggers --default-character-set=utf8mb4 --result-file="$env:USERPROFILE\backups\proyectwin_mvp_backup.sql" proyectwin_mvp
```

Si MySQL tiene contraseña, añade `-p` y escríbela cuando se solicite. Nunca la
incluyas en el comando ni la imprimas:

```powershell
mysqldump -u root -p --single-transaction --routines --triggers --default-character-set=utf8mb4 --result-file="$env:USERPROFILE\backups\proyectwin_mvp_backup.sql" proyectwin_mvp
```

Comprueba el archivo:

```powershell
Get-Item $env:USERPROFILE\backups\proyectwin_mvp_backup.sql
Select-String -Path $env:USERPROFILE\backups\proyectwin_mvp_backup.sql -Pattern 'CREATE TABLE `general_users`'
```

Verifica la restauración en una base temporal, nunca directamente sobre
`proyectwin_mvp`:

```powershell
mysql -u root -e "CREATE DATABASE proyectwin_mvp_restore_check CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
cmd /c "mysql -u root proyectwin_mvp_restore_check < %USERPROFILE%\backups\proyectwin_mvp_backup.sql"
```

PowerShell no admite `<` como redirección de entrada; `cmd /c` conserva los
bytes del dump UTF-8. No ejecutes la restauración sobre la base original sin
autorización.

## Pruebas

```powershell
composer test
```

La suite tiene **312 tests en 49 archivos** (Fase 6, 2-nov-2026).

```powershell
cd backend
composer test
```

`composer test` ejecuta `scripts/run-tests.php`: crea una base desechable
`ptwin_test_*`, migra, ejecuta PHPUnit y la elimina al terminar. No es necesario
exportar `DB_DATABASE` ni crear la base manualmente.

Prueba individual:

```powershell
cd backend
composer test -- --filter NombreDelTest
```

`phpunit.xml` no fija una base por defecto. La guarda de
`tests/CreatesApplication.php` rechaza `DATABASE_URL`, `proyectwin_mvp`,
`proyectwin_test`, `proyectwin_e2e` y cualquier nombre fuera de `ptwin_test_*`.

E2E se ejecuta desde `frontend` con `npm run test:e2e`. El wrapper verifica
Mailpit, crea una base desechable `ptwin_e2e_6an_*`, ejecuta Playwright y la
elimina al terminar. No es necesario exportar `E2E_ALLOW_RESET` ni
`E2E_DB_DATABASE`.

## Reglas relevantes

- **Estados de propuesta:** `borrador → pendiente → aprobado|rechazado`.
  Aprobada queda bloqueada; rechazada debe corregirse y reenviarse.
- **Motor de similitud:** corre al enviar/reenviar y al editar en revisión.
  Solo las aprobadas del mismo programa, dentro de la ventana de 12 meses,
  forman el corpus. Los pares históricos se conservan como evidencia.
- **Autorización:** middleware de rol y alcance por ficha; el admin supervisa,
  el instructor solo sus fichas y el aprendiz su ámbito.
- **Auditoría:** `audit_logs` es inmutable y redacta PII; `project_histories`
  guarda la evolución funcional.
- **Borrado:** las propuestas con evidencia de similitud responden 409; una
  ficha con datos se anula en lugar de borrarse.
- **Notificaciones:** las de tipo `sistema` solo las genera el backend mediante
  `NotificacionesService`. `POST /v1/notifications` rechaza ese tipo con 422 y
  `PUT` no permite convertir un aviso manual en `sistema`; los demás tipos
  conservan las reglas de alcance por rol.

## Comandos útiles

```powershell
php artisan demo:preparar --check-only
php artisan demo:preparar --solo-credenciales
php artisan similitud:recalcular
php artisan similitud:calibrar
php artisan perfiles:docentes
```
