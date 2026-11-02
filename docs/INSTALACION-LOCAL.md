# Instalación local y portabilidad — ProyecTwin MVP

Esta guía permite ejecutar el proyecto en el computador actual o en otro equipo
Windows sin depender de rutas absolutas ni de archivos `.env` de otra máquina.

## Requisitos

| Herramienta | Versión recomendada |
|---|---|
| PHP | 8.3 (mínimo 8.2) |
| Composer | 2.x |
| Node.js | 22.12 o superior |
| npm | 10.x o superior |
| MySQL / MariaDB | MySQL 8.0 o MariaDB 10.4+ (el equipo actual usa MariaDB 10.4.32) |
| Git | 2.x |

Extensiones PHP necesarias: `pdo_mysql`, `mbstring`, `dom`, `fileinfo`,
`openssl`, `tokenizer`, `xml`, `ctype`, `filter`, `hash`, `session`.
`gd`/`zip` son opcionales (imágenes PDF y conveniencia de Composer).

## 1. Obtener el proyecto

```powershell
git clone https://github.com/YDanB-312/ProyecTwin-MVP.git
cd ProyecTwin-MVP
```

No copies `.env`, `vendor/`, `node_modules/`, `dist/` ni `storage/` desde otro
equipo. Se generan o instalan localmente.

## 2. Backend nativo

```powershell
cd backend
composer install
Copy-Item .env.example .env
php artisan key:generate
```

Edita `backend/.env` con tus datos locales. No publiques ese archivo.

Cada desarrollador debe crear **su propia base local vacía**. El nombre
`proyectwin_mvp` es el nombre de la base local del propietario actual y solo el
valor de ejemplo del `.env.example`; no incluye datos ni se copia desde otro
equipo. Si usas ese nombre, será tu copia local nueva. No ejecutes `migrate`,
seeders ni `migrate:fresh` sobre una base existente con datos sin autorización
específica.

Variables mínimas:

```dotenv
APP_ENV=local
APP_DEBUG=true
APP_URL=http://127.0.0.1:8000
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
# Nombre de TU base local vacía; puede ser distinto.
DB_DATABASE=proyectwin_mvp
DB_USERNAME=root
DB_PASSWORD=
SESSION_DRIVER=file
CACHE_DRIVER=file
QUEUE_CONNECTION=sync
MAIL_MAILER=smtp
MAIL_HOST=127.0.0.1
MAIL_PORT=1025
```

En un equipo nuevo, crea una base vacía propia en tu MySQL/MariaDB (por ejemplo
desde phpMyAdmin o con `CREATE DATABASE tu_base_local CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;`), ajusta `DB_DATABASE` y ejecuta solo sobre esa
base:

```powershell
# Solo si la base es nueva, vacía y propia (entorno local):
php artisan migrate --seed
```

No improvises `migrate:fresh` ni seeders sobre bases existentes. En producción
no uses `--seed`: ejecuta `php artisan migrate --force` y crea el administrador
con `php artisan admin:inicial`.

Si quieres usar `demo:preparar`, el nombre local debe ser `proyectwin_mvp` en
**tu** servidor; sigue siendo una base nueva y no la del autor. En un equipo
nuevo normalmente no necesitas ese comando: `migrate --seed` es suficiente. Si
usas el seed demo, define tu propia `DEMO_PASSWORD` en `.env`; no se publica
una contraseña compartida.

Inicia la API:

```powershell
php artisan serve --host=127.0.0.1 --port=8000
```

Comprobaciones:

```powershell
Invoke-WebRequest http://127.0.0.1:8000/up
Invoke-WebRequest http://127.0.0.1:8000/ready
```

`/up` es liveness (sin base de datos). `/ready` comprueba la conexión a MySQL.

## 3. Frontend nativo

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía `/v1` y `/sanctum` a
`http://127.0.0.1:8000`. `/apk` no se publica.

Correo local opcional: descarga Mailpit desde
`https://github.com/axllent/mailpit/releases`, ejecuta `mailpit.exe` y abre
`http://localhost:8025`. Configura `MAIL_MAILER=smtp`, `MAIL_HOST=127.0.0.1` y
`MAIL_PORT=1025` si vas a probar activación o recuperación.

`PROYECTWIN_API` es solo el destino del proxy de desarrollo/preview (por
defecto `http://127.0.0.1:8000`). `VITE_API_URL` es el prefijo que se compila en
el SPA (por defecto `/v1`); no confundas ambos. No hace falta crear
`frontend/.env`.

## 4. Docker local

Vuelve a la raíz del repositorio antes de construir:

```powershell
cd ..
docker build -t proyectwin:local .
```

Para una prueba local con MySQL en el host:

```powershell
docker run --rm -p 8081:8080 `
  -e APP_ENV=local `
  -e APP_DEBUG=true `
  -e APP_KEY="base64:<clave-de-32-bytes>" `
  -e APP_URL=http://localhost:8081 `
  -e FRONTEND_URL=http://localhost:8081 `
  -e DB_CONNECTION=mysql `
  -e DB_HOST=host.docker.internal `
  -e DB_PORT=3306 `
  -e DB_DATABASE=ptwin_docker_local `
  -e DB_USERNAME=root `
  -e DB_PASSWORD= `
  -e SESSION_DRIVER=database `
  -e CACHE_DRIVER=file `
  -e QUEUE_CONNECTION=sync `
  proyectwin:local
```

Genera una clave válida desde `backend`:

```powershell
cd backend
php artisan key:generate --show
cd ..
```

Sustituye el placeholder del comando Docker por esa salida; no copies
`<clave-de-32-bytes>` literalmente. Se usa `8081` porque `8080` puede estar
ocupado por `AgentService.exe` en este equipo.

`ptwin_docker_local` es una base desechable solo para la prueba Docker: créala
vacía y elimínala al terminar. No apuntes el contenedor a `proyectwin_mvp`.

No uses `APP_ENV=production` con `APP_URL=http://...`: el entrypoint exige HTTPS.
Para validar el contenedor sin base de datos, usa el procedimiento aislado de
las fases de QA y no ejecutes migraciones contra bases protegidas.

Rutas:

- `/` → SPA React.
- `/v1/` → API Laravel.
- `/up` → liveness.
- `/ready` → readiness con MySQL.
- `/apk/**` → 404; el APK histórico no se distribuye.

## 5. APK Android

El APK histórico se conserva en:

```text
backend/public/apk/ProyecTWin.apk
```

- Paquete: `com.example.proyectwin`
- Versión: `1.3.0` (`versionCode 4`)
- Firma: APK Signature Scheme v2/v3, RSA 2048, `CN=Oscar`
- SHA-256: `A6C63BFE0B01C1020509649B8B163715AA8F21852D664740392FB8256B69B99E`

**No está autorizado para distribución pública.** No se incluye en nuevas
imágenes, `/apk/**` responde 404 y `/v1/app-version/android` devuelve
`download_url` vacío. El Home no muestra botón de descarga.

La publicación requiere autorización escrita de Oscar y un cambio de código
explícito; no se activa por variable de entorno.

## 6. Otro computador desde cero

1. Instala PHP, Composer, Node, npm, MySQL y Git.
2. Clona el repositorio.
3. En `backend`: `composer install`, copia `.env.example` a `.env` y ajusta
   `DB_DATABASE`.
4. Genera la clave con `php artisan key:generate`.
5. Si la base es nueva, vacía y propia: `php artisan migrate --seed`.
6. Inicia `php artisan serve` y, en otra terminal, `cd frontend`, `npm install`
   y `npm run dev`.
7. Comprueba `/up`, `/ready`, la Home y el login.

No se necesitan rutas absolutas ni bases de datos de otro equipo.

## 7. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| 500 al arrancar | Falta `APP_KEY` | `php artisan key:generate` |
| `SQLSTATE` de conexión | MySQL apagado o credenciales | Revisa `DB_*` y el servicio MySQL |
| `/ready` 503 | MySQL no responde | Inicia MySQL y revisa `DB_*` |
| Puerto ocupado | Otro servidor en 8000/5173 | Usa otro puerto o cierra el proceso |
| `mysql` o `docker` no se reconoce | Binario fuera del PATH | Añade `C:\xampp\mysql\bin` y el `bin` de Docker Desktop al PATH de usuario |
| Estilos/API rotos en Docker | `APP_URL` o proxy incorrecto | Usa el mismo origen y `/v1` |
| APK 404 | Comportamiento esperado | El APK histórico no se distribuye |
| Docker no arranca | Motor/WSL | Revisa Docker Desktop; no borres VHDX ni uses `prune` |

## 8. Pruebas

Backend: crea, migra y elimina automáticamente una base `ptwin_test_*`:

```powershell
cd backend
composer test

# Una prueba individual:
composer test -- --filter NombreDelTest
```

Frontend: Vitest no toca la base de datos:

```powershell
cd ..\frontend
npm test
npm run lint
npm run build
```

E2E: requiere Mailpit iniciado; crea y elimina una base `ptwin_e2e_6an_*`:

```powershell
npm run test:e2e

# Un escenario individual:
npm run test:e2e -- --grep "nombre del escenario"
```

No ejecutes estas suites contra `proyectwin_mvp`. Los wrappers crean sus propias
bases desechables y las eliminan al terminar.
