# Guía de entrega para el siguiente desarrollador

Esta guía resume qué recibe, cómo levantar ProyecTWIN y qué no debe tocarse.
Los detalles de instalación están en [`INSTALACION-LOCAL.md`](INSTALACION-LOCAL.md)
y las pruebas aisladas en [`entorno-pruebas-seguro.md`](entorno-pruebas-seguro.md).

## 1. Qué contiene el repositorio

```text
backend/    API Laravel 10, migraciones, seeders y PHPUnit
frontend/   SPA React 19 + Vite 8, Vitest y Playwright
docs/       instalación, arquitectura, despliegue, release y documentación histórica
docker/     entrypoint y Nginx para la imagen de despliegue
Movil/      aplicación Android mantenida por Oscar; no forma parte de esta entrega web
```

El repositorio **no incluye una base de datos ni un dump**. Tampoco se debe
copiar el `.env`, `vendor/`, `node_modules/`, `storage/` ni `dist/` de otro
equipo.

## 2. Requisitos

- PHP 8.2+ (recomendado 8.3) con `pdo_mysql`, `mbstring`, `dom`, `fileinfo`,
  `openssl`, `tokenizer`, `xml`, `ctype`, `filter`, `hash` y `session`.
- Composer 2.x.
- Node.js 22.12+ y npm.
- MySQL 8 o MariaDB 10.4+.
- Git.
- Mailpit solo si se van a probar activación, recuperación o E2E.

## 3. Puesta en marcha

### Backend

```powershell
cd backend
composer install
Copy-Item .env.example .env
php artisan key:generate
```

Crea **tu propia base local vacía** y ajusta `DB_DATABASE`. El nombre
`proyectwin_mvp` es solo el ejemplo del `.env.example` y el nombre de la base
del propietario actual; no es una base compartida ni viene incluida. Después,
solo si la base es nueva y vacía:

```powershell
php artisan migrate --seed
php artisan serve --host=127.0.0.1 --port=8000
```

`migrate --seed` es para local. En producción se usa
`php artisan migrate --force` y `php artisan admin:inicial` con un correo real.
No ejecutes seeders demo en producción.

### Frontend

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. No hace falta crear `frontend/.env`: Vite reenvía
`/v1` y `/sanctum` a `http://127.0.0.1:8000`.

### Credenciales y secretos

No se publican contraseñas, tokens ni credenciales compartidas. `backend/.env`
no se versiona. Si usas el seed demo local, define tu propia `DEMO_PASSWORD` en
tu `.env`; para producción crea el administrador con `admin:inicial`. El manual
técnico de `docs/manual-tecnico/` es histórico y no debe usarse como fuente de
credenciales ni como guía de despliegue.

## 4. Pruebas

```powershell
# Backend: crea, migra y elimina una base ptwin_test_* desechable
cd backend
composer test

# Frontend: lint + Vitest + build
cd ../frontend
npm run quality

# E2E: requiere Mailpit y autorización; crea una base ptwin_e2e_6an_* desechable
npm run test:e2e
```

Prueba individual:

```powershell
cd backend
composer test -- --filter NombreDelTest
```

No ejecutes las suites contra `proyectwin_mvp`, `proyectwin_test` ni
`proyectwin_e2e`. Los wrappers crean y eliminan sus propias bases. Consulta
[`entorno-pruebas-seguro.md`](entorno-pruebas-seguro.md).

## 5. Qué está terminado

El MVP web/backend incluye, y está cubierto por pruebas hasta donde se verificó:

- autenticación con activación por correo, recuperación de contraseña y sesión
  por cookie httpOnly/CSRF;
- roles aprendiz, instructor y administrador con autorización por recurso;
- fichas, aprendices, instructores, catálogos y equipos;
- propuestas: borrador, envío, revisión, aprobación, rechazo, reenvío e
  historial;
- motor de similitud del mismo programa y supervisión administrativa;
- notificaciones (las de tipo `sistema` solo las genera el backend), soporte y
  bitácora de auditoría;
- APK histórico conservado pero **no distribuido**.

Última evidencia (Fase 6, 2-nov-2026): backend **312 tests / 1698 aserciones**,
frontend **110 tests / 31 archivos**, lint y build OK, y E2E **25/25** con base
desechable `ptwin_e2e_6an_20261102_162236_a61ac3` (eliminada al terminar). La
revisión visual en Chromium y la reauditoría axe no detectaron violaciones en
las vistas comprobadas.

## 6. Pendientes abiertos

1. **Configuración real de producción:** depende del acceso a Railway, Brevo y
   la base real. Deben verificarse `APP_DEBUG=false`, `APP_KEY`, `APP_URL`,
   `FRONTEND_URL`, `TRUSTED_PROXIES`, correo y base.
2. **Integración remota:** no se ejecutó `fetch`; la divergencia con `origin`
   sigue sin comprobarse y requiere autorización.
3. **APK:** cualquier distribución requiere aprobación escrita de Oscar y
   custodia de la firma. El APK histórico no se sirve ni se anuncia.
4. **Residual visual menor (H-06):** en anchos móviles algunos placeholders de
   filtros y etiquetas de gráficos se truncan. No rompen el layout ni bloquean
   el uso; el detalle está en [`inventario-pendientes.md`](inventario-pendientes.md).
5. **Validación externa pendiente:** la aceptación en un segundo equipo Windows
   y en un dispositivo Android real no forma parte de esta fase.

Los puntos 1, 2, 3 y 5 dependen de terceros o de autorizaciones explícitas; no
deben declararse resueltos sin la evidencia correspondiente.

## 7. Antes de modificar

- No modificar `Movil/`, el APK, los keystores ni los archivos de Oscar.
- No modificar Docker, Railway o producción sin autorización.
- No leer ni publicar secretos, tokens o contraseñas.
- No ejecutar `migrate:fresh`, `db:wipe`, seeders o resets sobre bases con
  datos; usar solo bases locales propias o desechables.
- Revisar el estado del working tree antes de borrar o sobrescribir archivos:
  puede contener cambios locales sin commit.
- Mantener Laravel 10, React/Vite y CSS Modules; no introducir Tailwind ni una
  reescritura masiva.
- Consultar [`arquitectura.md`](arquitectura.md) y
  [`../backend/README.md`](../backend/README.md) antes de cambiar contratos de
  API.

## 8. Entrega a Oscar

Este repositorio contiene el proyecto web (`frontend/`) y el backend
(`backend/`). La aplicación Android existente está en `Movil/` y la mantiene
Oscar por separado.

- **No sobrescribir, eliminar ni reemplazar el trabajo móvil existente.**
- Cualquier cambio en la app, la firma o la distribución del APK debe
  coordinarse con Oscar.
- La publicación del APK requiere autorización escrita y custodia verificada de
  la firma; ver [`APK-APROBACION-OSCAR.md`](APK-APROBACION-OSCAR.md).
- El procedimiento técnico de release está en
  [`release-android.md`](release-android.md).

## 9. Documentos relacionados

- [`INSTALACION-LOCAL.md`](INSTALACION-LOCAL.md): instalación paso a paso.
- [`CHECKLIST-INSTALACION-WINDOWS.md`](CHECKLIST-INSTALACION-WINDOWS.md):
  aceptación en un segundo equipo Windows.
- [`entorno-pruebas-seguro.md`](entorno-pruebas-seguro.md): aislamiento de
  PHPUnit y E2E.
- [`deploy-railway.md`](deploy-railway.md): despliegue y variables.
- [`arquitectura.md`](arquitectura.md): recorrido de una petición.
- [`../CI.md`](../CI.md): integración continua.
- [`../README.md`](../README.md): entrada principal del repositorio.
