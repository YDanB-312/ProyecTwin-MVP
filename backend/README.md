# ProyecTwin MVP — Backend

API REST de ProyecTwin (SENA): propuestas de proyecto, detección de similitudes,
revisión por instructores, historial, auditoría, notificaciones, fichas, equipos
y credenciales institucionales.

- **Stack:** Laravel 10 · PHP 8.2+ · MySQL · Sanctum.
- **Frontend:** `../frontend` (React + Vite).
- **Prefijo de API:** `/v1` (`routes/api-v1.php`).

## Requisitos

- XAMPP con MySQL iniciado.
- PHP de XAMPP con `pdo_mysql`, `mbstring`, `openssl`, `fileinfo`, `gd` y `zip`.
- Composer.
- Mailpit para probar el correo real en local.

## Instalación local (XAMPP)

1. Crear la base en phpMyAdmin:

```sql
CREATE DATABASE proyectwin_mvp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. Instalar y configurar:

```powershell
cd C:\xampp\htdocs\ProyecTwin-master\MVP\backend

C:\xampp\php\php.exe composer install
copy .env.example .env
C:\xampp\php\php.exe artisan key:generate
```

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

4. Preparar la base:

```powershell
C:\xampp\php\php.exe artisan demo:preparar
C:\xampp\php\php.exe artisan serve --host=127.0.0.1 --port=8000
```

`demo:preparar` solo opera sobre `proyectwin_mvp` en local. No usa
`migrate:fresh`, `db:wipe` ni truncamientos. Para una base existente exige
flags explícitos (`--check-only`, `--migrar`, `--actualizar-correos`,
`--solo-credenciales`, `--sembrar`). `--check-only` es siempre de solo lectura.
En una base nueva, `--migrar` crea el esquema completo. En una base anterior
como `proyectwin_mvp`, las migraciones incluyen una compatibilidad idempotente
para añadir `email_verified_at` si falta.

## Migraciones

El historial local tiene 21 archivos en `database/migrations` más la migración
de Sanctum que Laravel carga desde `vendor/`. En total son 22 migraciones
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
C:\xampp\php\php.exe artisan demo:preparar --migrar
C:\xampp\php\php.exe artisan demo:preparar --actualizar-correos
C:\xampp\php\php.exe artisan demo:preparar --solo-credenciales
C:\xampp\php\php.exe artisan demo:preparar --sembrar
```

Las cuentas administradas (`@example.com` y `demo.temporal@example.com`) se
verifican y rotan con `--solo-credenciales`. Las cuentas no administradas que
existan en la base quedan sin verificar y deben usar activación o recuperación.

## Correo local con Mailpit

Descarga el binario oficial de Mailpit desde
`https://github.com/axllent/mailpit/releases` y ejecuta:

```powershell
mailpit.exe
```

- SMTP: `127.0.0.1:1025`
- Bandeja web: `http://localhost:8025`

La activación, recuperación y aviso de cambio de correo usan las notificaciones
reales de Laravel. No hay enlace local por API. La entrega real a internet no se
prueba en local; Mailpit captura los mensajes. En producción el correo se
configura con `MAIL_MAILER=brevo` o un SMTP real.

## Cuentas demo

Todas las cuentas del seed están verificadas y comparten la contraseña
`ProyecTwin.Demo#2026` (configurable con `DEMO_PASSWORD`). Los correos usan el
dominio reservado `@example.com`; no son buzones reales.

| Rol | Correo / username |
|---|---|
| Aprendiz | `maria.gonzalez@example.com` / `mgonzalez` |
| Instructor | `carlos.ruiz@example.com` / `cruiz` |
| Admin | `admin.proyectwin@example.com` / `a` |

La activación de una cuenta nueva se prueba creando el usuario desde el flujo
real del administrador y abriendo el enlace recibido en Mailpit.

La cuenta heredada `demo.temporal@soy.sena.edu.co` se administra con
`--actualizar-correos` y `--solo-credenciales` cuando existe en una base
anterior; no forma parte del seed nuevo. Las migraciones no marcan cuentas
existentes como verificadas: la verificación de las cuentas administradas se
hace explícitamente con `--solo-credenciales`.

## Seeders

`DatabaseSeeder` llama a `DemoSeeder`, que contiene el escenario completo. Los
registros demo se identifican en `demo_seed_records`; una segunda ejecución no
duplica ni sobrescribe usuarios reales. El escenario incluye usuarios, redes,
programas, fichas, aprendices, propuestas en todos los estados, integrantes,
similitudes calculadas por el motor, comentarios, historial, auditoría,
notificaciones y reportes.

## Backup y restauración

No uses la redirección `>` de PowerShell para crear el respaldo: puede cambiar
la codificación del SQL. Usa `--result-file`:

```powershell
New-Item -ItemType Directory -Force C:\backups

C:\xampp\mysql\bin\mysqldump.exe -u root --single-transaction --routines --triggers --default-character-set=utf8mb4 --result-file="C:\backups\proyectwin_mvp_backup.sql" proyectwin_mvp
```

Si MySQL tiene contraseña, añade `-p` y escríbela cuando se solicite. Nunca la
incluyas en el comando ni la imprimas:

```powershell
C:\xampp\mysql\bin\mysqldump.exe -u root -p --single-transaction --routines --triggers --default-character-set=utf8mb4 --result-file="C:\backups\proyectwin_mvp_backup.sql" proyectwin_mvp
```

Comprueba el archivo:

```powershell
Get-Item C:\backups\proyectwin_mvp_backup.sql
Select-String -Path C:\backups\proyectwin_mvp_backup.sql -Pattern 'CREATE TABLE `general_users`'
```

Verifica la restauración en una base temporal, nunca directamente sobre
`proyectwin_mvp`:

```powershell
C:\xampp\mysql\bin\mysql.exe -u root -e "CREATE DATABASE proyectwin_mvp_restore_check CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
C:\xampp\mysql\bin\mysql.exe -u root proyectwin_mvp_restore_check < C:\backups\proyectwin_mvp_backup.sql
```

La segunda línea usa `<`, que es una entrada de archivo, no una redirección de
salida. No ejecutes la restauración sobre la base original sin autorización.

## Pruebas

```powershell
C:\xampp\php\php.exe composer test
# Equivalente:
C:\xampp\php\php.exe artisan test
```

La suite tiene **225 tests en 39 archivos**. `phpunit.xml` fuerza
`APP_ENV=testing`, `proyectwin_test`, mail `array`, sesión `array` y cache
`array`; la guarda de `tests/CreatesApplication.php` rechaza `DATABASE_URL` y
conexiones que no sean loopback. Las suites no dependen de archivos de entorno
adicionales: solo se mantiene `.env` para la configuración local y
`.env.example` como plantilla. `phpunit.xml` y `playwright.config.js` definen
las variables de sus respectivas bases. Para ejecutarlas desde cero, crea
`proyectwin_test` y `proyectwin_e2e` en el MySQL local; no se crean
automáticamente.

Para crear el esquema de `proyectwin_test` sin archivos de entorno adicionales,
define la base explícitamente antes de migrar:

```powershell
$env:APP_ENV = 'testing'
$env:DB_DATABASE = 'proyectwin_test'
php artisan migrate --force
```

No ejecutes `--env=testing` sin `DB_DATABASE`: Laravel cargaría `.env` y podría
apuntar a la base local.

Playwright usa `proyectwin_e2e` de forma exclusiva. PHPUnit usa
`proyectwin_test`. La base local `proyectwin_mvp` nunca es objetivo de tests.

```powershell
cd ../frontend
$env:E2E_ALLOW_RESET = '1'
npm run test:e2e
```

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

## Comandos útiles

```powershell
C:\xampp\php\php.exe artisan demo:preparar --check-only
C:\xampp\php\php.exe artisan demo:preparar --solo-credenciales
C:\xampp\php\php.exe artisan similitud:recalcular
C:\xampp\php\php.exe artisan similitud:calibrar
C:\xampp\php\php.exe artisan perfiles:docentes
```
