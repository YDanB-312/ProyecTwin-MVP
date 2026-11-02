# Checklist — Instalación en un segundo equipo Windows

> **Estado:** pendiente de ejecución en un equipo físico distinto. 6AL validó una
> instalación limpia simulada en el mismo equipo; esta lista es el procedimiento
> de aceptación para el segundo equipo. No sustituye esa prueba.

## 0. Datos del equipo

- [ ] Windows 10/11 x64, actualizado.
- [ ] Arquitectura y usuario con permisos para instalar PHP, Composer, Node y MySQL.
- [ ] Espacio libre suficiente (mínimo 2 GB para dependencias y build).
- [ ] Puertos **8000** (backend) y **5173** (frontend) libres.

## 1. Requisitos

- [ ] PHP **8.2+** (recomendado 8.3) con `pdo_mysql`, `mbstring`, `dom`,
      `fileinfo`, `openssl`, `tokenizer`, `xml`, `ctype`, `filter`, `hash` y
      `session`. `gd`/`zip` son opcionales.
- [ ] Composer **2.x**.
- [ ] Node.js **22.12+** y npm.
- [ ] MySQL **8** o MariaDB **10.4+** iniciado y accesible en `127.0.0.1:3306`
      (el equipo actual usa MariaDB 10.4.32).
- [ ] Git.
- [ ] `php -v`, `composer -V`, `node -v`, `npm -v`, `mysql --version`.

Comprobación de extensiones:

```powershell
php -m | Select-String 'pdo_mysql|mbstring|dom|fileinfo|openssl|tokenizer|xml|ctype|filter|hash|session'
```

## 2. Clonar y preparar

```powershell
git clone https://github.com/YDanB-312/ProyecTwin-MVP.git
cd ProyecTwin-MVP
```

- [ ] No copiar `.env`, `vendor`, `node_modules`, `storage`, `dist` ni
      `test-results` desde otro equipo.

## 3. Backend

```powershell
cd backend
composer install
Copy-Item .env.example .env
```

- [ ] Crear una base MySQL/MariaDB **vacía y propia** para este equipo y
      ajustar `DB_DATABASE`. El nombre `proyectwin_mvp` es solo un ejemplo; no
      se copia la base del autor.
- [ ] Configurar `APP_URL=http://127.0.0.1:8000`, `DB_*` y correo local.
- [ ] Confirmar que la base es nueva y está vacía. No ejecutes `migrate` ni
      seeders sobre una base existente con datos.
- [ ] Generar clave:

```powershell
php artisan key:generate
```

- [ ] Si la base es **nueva, vacía y propia**, ejecutar en local
      `php artisan migrate --seed`. No ejecutar `migrate:fresh`, `db:wipe` ni
      resets sobre bases existentes. En producción usar `migrate --force` y
      `admin:inicial`, nunca el seeder demo. `demo:preparar` requiere el nombre
      `proyectwin_mvp` en el MySQL de este equipo y autorización.

- [ ] Iniciar backend:

```powershell
php artisan serve --host=127.0.0.1 --port=8000
```

## 4. Frontend

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

- [ ] Abrir `http://localhost:5173`.
- [ ] Confirmar que `/v1` y `/sanctum` llegan al backend.
- [ ] Confirmar que `/apk` responde 404; no hay proxy ni descarga local.

`PROYECTWIN_API` cambia el destino del proxy; `VITE_API_URL` es el prefijo
compilado. No confundir ambos.

### Correo local opcional

- [ ] Descargar Mailpit de `https://github.com/axllent/mailpit/releases`.
- [ ] Ejecutar `mailpit.exe` y abrir `http://localhost:8025`.
- [ ] Configurar `MAIL_MAILER=smtp`, `MAIL_HOST=127.0.0.1`, `MAIL_PORT=1025`.
- [ ] Probar activación/recuperación si se van a usar.

## 5. Verificación funcional

- [ ] `GET http://127.0.0.1:8000/up` → `{"status":"ok"}`.
- [ ] `GET http://127.0.0.1:8000/ready` → `{"status":"ready"}`.
- [ ] Home y login cargan sin errores de consola.
- [ ] Login con una cuenta creada/seed local.
- [ ] Recarga del SPA y navegación interna.
- [ ] `GET /v1/app-version/android` responde con `download_url=""`.
- [ ] `/apk/ProyecTWin.apk` y `/apk/` responden 404.
- [ ] El Home no muestra botón ni enlace de descarga del APK.

### Pruebas automatizadas

- [ ] Backend: `cd backend` y `composer test`.
- [ ] Frontend: `cd frontend` y `npm test`.
- [ ] E2E (con Mailpit iniciado): `npm run test:e2e`.
- [ ] Una prueba individual: `composer test -- --filter NombreDelTest`.
- [ ] Un escenario E2E: `npm run test:e2e -- --grep "nombre del escenario"`.

Los comandos de backend y E2E crean y eliminan sus propias bases desechables.

## 6. Docker opcional

Desde la raíz del repositorio:

```powershell
docker build -t proyectwin:local .
```

Para probar con una base desechable en el MySQL local:

```powershell
# APP_KEY: ejecuta `php artisan key:generate --show` desde backend y pega el valor.
docker run --rm -p 8081:8080 `
  -e APP_ENV=local -e APP_DEBUG=true `
  -e APP_KEY="<salida-de-key-generate-show>" `
  -e APP_URL=http://localhost:8081 `
  -e FRONTEND_URL=http://localhost:8081 `
  -e DB_CONNECTION=mysql -e DB_HOST=host.docker.internal `
  -e DB_PORT=3306 -e DB_DATABASE=ptwin_docker_local `
  -e DB_USERNAME=root -e DB_PASSWORD= `
  proyectwin:local
```

No uses `APP_ENV=production` con `APP_URL=http://...`. No montes volúmenes del
host ni bases protegidas. `ptwin_docker_local` es una base desechable solo para
esta prueba: créala vacía y elimínala al terminar. El puerto `8081` evita el
conflicto con `AgentService.exe`, que ocupa `8080` en el equipo actual.

- [ ] `docker version` muestra Client y Server.
- [ ] La imagen contiene PHP 8.3 y no incluye el APK histórico.
- [ ] `/up` y `/ready` responden.
- [ ] No se montan volúmenes del host ni bases protegidas.

## 6.1 Errores frecuentes

| Síntoma | Causa / solución |
|---|---|
| 500 al arrancar | Falta `APP_KEY`; ejecuta `php artisan key:generate` |
| Error SQLSTATE | MySQL apagado o `DB_*` incorrectos |
| `/ready` 503 | MySQL no responde; revisa credenciales y servicio |
| Pantalla en blanco o API 404 | Proxy/API incorrectos; confirma `/v1` y `PROYECTWIN_API` |
| Puerto ocupado | Cambia 8000/5173 o cierra el proceso |
| APK 404 | Comportamiento esperado: el APK histórico no se distribuye |
| Docker no inicia | Motor/WSL; no borres VHDX ni uses `prune` |

## 7. APK

- [ ] Archivo histórico conservado: `backend/public/apk/ProyecTWin.apk`.
- [ ] Versión de referencia: **1.3.0 / versionCode 4**.
- [ ] SHA-256 de referencia:
      `A6C63BFE0B01C1020509649B8B163715AA8F21852D664740392FB8256B69B99E`.
- [ ] Firma v2/v3, `CN=Oscar`, RSA 2048.
- [ ] No se copia a imágenes, no se sirve y no se anuncia.
- [ ] No re-firmar ni sustituir sin autorización de Oscar.

## 8. Registro de aceptación

Anotar sin secretos:

- Fecha y equipo.
- Versiones de PHP, Composer, Node, npm y MySQL.
- Resultado de migraciones.
- Resultado de `/up`, `/ready`, login y `/apk` (debe ser 404).
- Errores encontrados y solución.
- Firma del responsable.

**Criterio de aceptación:** instalación reproducible, migraciones completas,
backend/frontend conectados, APK histórico no distribuido y ningún secreto
versionado.
