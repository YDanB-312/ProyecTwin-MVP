# ProyecTWIN MVP

Plataforma de gestión y análisis de propuestas de proyecto del SENA. El
repositorio conserva el MVP actual: API Laravel, SPA React/Vite y la aplicación
Android que mantiene Oscar por separado.

## Qué es y qué resuelve

ProyecTWIN centraliza el registro de propuestas de proyecto formativo, la
revisión por instructores y la detección de similitudes entre proyectos del
mismo programa. También reúne fichas, equipos, notificaciones, soporte,
catálogos institucionales y auditoría de acciones sensibles.

## Roles

- **Aprendiz:** consulta su ficha, registra y envía propuestas, gestiona su
  equipo, revisa similitudes y recibe notificaciones.
- **Instructor:** administra sus fichas, revisa propuestas, aprueba o rechaza
  con observación y consulta las similitudes de su ámbito.
- **Administrador:** gestiona usuarios, fichas, catálogos, propuestas,
  supervisión de similitudes, configuración del motor, soporte y bitácora.

## Funcionalidades principales

- Autenticación con activación por correo, recuperación de contraseña, sesión
  web por cookie httpOnly y tokens para clientes móviles.
- Gestión de usuarios, aprendices, instructores, fichas, programas y redes.
- Propuestas con borrador, envío, revisión, aprobación, rechazo, reenvío,
  equipo e historial.
- Motor de similitud del mismo programa, pares vigentes e históricos y
  supervisión administrativa.
- Notificaciones (las de tipo `sistema` solo las genera el backend), soporte,
  bitácora de auditoría y reportes de falla.

## Flujo general

1. El administrador crea la cuenta y el aprendiz la activa desde el correo.
2. El aprendiz se une a una ficha con el código y registra una propuesta.
3. Al enviarla, el backend valida el contenido y ejecuta el motor de similitud.
4. El instructor revisa, aprueba o rechaza; el aprendiz recibe el aviso.
5. El administrador supervisa usuarios, catálogos, similitudes y auditoría.

Las funcionalidades y los estados exactos están en el código y en
[`docs/arquitectura.md`](docs/arquitectura.md). No se presentan como terminadas
las tareas que siguen pendientes.

## Stack

- **Backend:** Laravel 10, PHP 8.2+, MySQL/MariaDB, Sanctum.
- **Frontend:** React 19, Vite 8, React Router, CSS Modules.
- **Infraestructura:** Docker multi-stage (Nginx + PHP-FPM) para despliegue.
- **Android:** `Movil/`, responsabilidad de Oscar; no se modifica desde este
  repositorio.

## Estructura

```text
backend/    API Laravel, migraciones, seeders y pruebas PHPUnit
frontend/   SPA React, pruebas Vitest y E2E Playwright
docs/       documentación operativa, decisiones, arquitectura e histórico
docker/     entrypoint y configuración de Nginx
Movil/      aplicación Android (fuera del alcance de este equipo)
```

La documentación está indexada en [`docs/README.md`](docs/README.md).

## Requisitos previos

- PHP **8.2+** con `pdo_mysql`, `mbstring`, `dom`, `fileinfo`, `openssl`,
  `tokenizer`, `xml`, `ctype`, `filter`, `hash` y `session`.
- Composer 2.x.
- Node.js **22.12+** y npm.
- MariaDB **10.4+** o MySQL 8.
- Git.
- En Windows, XAMPP puede proporcionar PHP y MariaDB. Añade
  `C:\xampp\mysql\bin` al PATH si `mysql` no se reconoce.

## Instalación backend

```powershell
cd backend
composer install
Copy-Item .env.example .env
# Edita .env y ajusta DB_DATABASE al nombre de TU base local vacía.
php artisan key:generate
# Solo si acabas de crear una base vacía y propia:
php artisan migrate --seed
php artisan serve --host=127.0.0.1 --port=8000
```

La API queda en `http://127.0.0.1:8000`.

`migrate --seed` es solo para una base local nueva y vacía. En producción no se
ejecuta el seeder demo: se usa `php artisan migrate --force` y
`php artisan admin:inicial` con un correo real.

### Base de datos

- Cada desarrollador usa **su propia base local**. El nombre `proyectwin_mvp`
  es solo el valor de ejemplo del `.env.example` y el nombre de la base del
  propietario actual; no incluye datos ni se copia desde el equipo del autor.
- El repositorio no incluye una base de datos ni un dump. Crea una base vacía
  en tu MySQL/MariaDB y ajusta `DB_DATABASE` a ese nombre. Si usas
  `proyectwin_mvp`, será tu copia local nueva, no la base de otra persona.
- Si la base ya existe y tiene datos, **no ejecutes `migrate`, `migrate:fresh`
  ni seeders** sobre ella sin autorización.
- Para una instalación desde cero, ejecuta `php artisan migrate --seed` solo
  sobre la base vacía que acabas de crear.
- `composer install` no necesita Docker ni una base de datos.
- PHPUnit y E2E no usan la base de desarrollo: crean y eliminan bases
  desechables propias.

## Instalación frontend

En otra terminal, desde la raíz:

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía `/v1` y `/sanctum` a
`http://127.0.0.1:8000` por defecto. No hace falta crear `frontend/.env`.

### Problemas frecuentes

| Síntoma | Solución |
|---|---|
| `php` o `composer` no se reconoce | Añade PHP y Composer al PATH y abre una terminal nueva. |
| `mysql` no se reconoce | Añade `C:\xampp\mysql\bin` al PATH. |
| Error de conexión a la base | Verifica que MariaDB esté iniciada y que `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME` y `DB_PASSWORD` sean correctos. |
| `APP_KEY` vacío | Ejecuta `php artisan key:generate`. |
| `npm install` falla | Verifica Node 22+ y vuelve a ejecutar `npm install`. |
| Puerto 8000 o 5173 ocupado | Usa otro puerto: `php artisan serve --port=8001` o `npm run dev -- --port 5174`. |
| La API no responde desde React | Inicia Laravel en `127.0.0.1:8000` y revisa el proxy de Vite. |

## Pruebas

```powershell
# Backend: crea, migra y elimina una base desechable ptwin_test_*
cd backend
composer test

# Frontend
cd ../frontend
npm test

# E2E: requiere Mailpit; crea y elimina una base desechable ptwin_e2e_6an_*
npm run test:e2e
```

Pruebas individuales:

```powershell
# Backend
cd backend
composer test -- --filter NombreDelTest

# E2E
cd ../frontend
npm run test:e2e -- --grep "nombre del escenario"
```

`composer test` y `npm run test:e2e` crean sus propias bases desechables. No usan
`proyectwin_mvp` ni requieren exportar `DB_DATABASE`, `E2E_DB_DATABASE` o
`E2E_ALLOW_RESET` manualmente. Consulta
[`docs/entorno-pruebas-seguro.md`](docs/entorno-pruebas-seguro.md).

### Validación completa

Con Mailpit iniciado para la parte E2E:

```powershell
cd backend
composer test

cd ../frontend
npm run quality
npm run test:e2e
```

`npm run quality` ejecuta lint, Vitest y build. No se añadió un orquestador
único para no duplicar los comandos nativos de cada herramienta.

## Estado de entrega

- El APK histórico en `backend/public/apk/ProyecTWin.apk` **se conserva
  tracked, pero no está autorizado para distribución pública**. No se incluye
  en nuevas imágenes, no se anuncia y `/apk` responde 404. La publicación y la
  custodia de la firma requieren autorización escrita de Oscar.
- No se deben versionar secretos. `backend/.env` está ignorado. `frontend/.env`
  es un pendiente histórico de limpieza: no lo recrees ni agregues uno nuevo;
  su retiro del control de versiones requiere autorización.
- Esta copia de trabajo puede contener cambios locales sin commit; no ejecutes
  `git clean`, `git reset` ni `git checkout` sin revisar antes el estado.

## Entrega a Oscar

Este repositorio contiene el proyecto web (`frontend/`) y el backend
(`backend/`). La aplicación Android vive en `Movil/` y la mantiene Oscar por
separado: **no se debe sobrescribir, eliminar ni reemplazar ese trabajo móvil**
desde este equipo. Cualquier cambio en la app, la firma o la distribución del
APK requiere coordinación y autorización escrita de Oscar.

- Estado y confirmaciones pendientes: [`docs/APK-APROBACION-OSCAR.md`](docs/APK-APROBACION-OSCAR.md).
- Procedimiento de release: [`docs/release-android.md`](docs/release-android.md).

## Documentos clave

- [`docs/ENTREGA-DESARROLLADOR.md`](docs/ENTREGA-DESARROLLADOR.md)
- [`docs/INSTALACION-LOCAL.md`](docs/INSTALACION-LOCAL.md)
- [`docs/CHECKLIST-INSTALACION-WINDOWS.md`](docs/CHECKLIST-INSTALACION-WINDOWS.md)
- [`docs/deploy-railway.md`](docs/deploy-railway.md)
- [`docs/entorno-pruebas-seguro.md`](docs/entorno-pruebas-seguro.md)
- [`docs/APK-APROBACION-OSCAR.md`](docs/APK-APROBACION-OSCAR.md)
- [`docs/release-android.md`](docs/release-android.md)

## Reglas de mantenimiento

- Mantener Laravel 10, React y Vite; no se moderniza el stack en esta fase.
- Conservar CSS Modules y los tokens visuales existentes.
- No introducir Tailwind ni rediseñar pantallas.
- No modificar `Movil/`, APK, certificados ni archivos de Oscar.
- No tocar `backend/storage`, `composer.lock` ni `package-lock.json` sin
  necesidad demostrada y autorización.
