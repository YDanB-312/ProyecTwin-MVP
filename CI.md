# Integración continua

Este repositorio usa GitHub Actions para validar el MVP web sin conectarse a
producción ni a la base de datos de desarrollo.

## Disparadores

- Push a `master`.
- Pull requests.
- Ejecución manual desde **Actions → CI → Run workflow**.

## Jobs

### Backend

- PHP 8.3, alineado con el `Dockerfile` de despliegue.
- MySQL 8.0 como servicio del job.
- Base aislada y efímera `ptwin_test_ci_suite` (servicio MySQL del job).
- `composer install`.
- `php artisan key:generate`.
- `php artisan migrate --force`.
- Lint de todos los archivos PHP de `app`, `database`, `routes` y `tests`.
- PHPUnit completo.

### Frontend

- Node 22.
- `npm ci` reproducible desde `package-lock.json`.
- ESLint completo (`npm run lint`).
- Pruebas unitarias (`npm test`).
- Build de producción (`npm run build`).

## Aislamiento

- El workflow no define credenciales reales ni secretos.
- No se conecta a `proyectwin_mvp` ni a producción.
- PHPUnit solo acepta bases desechables `ptwin_test_*`; la base de desarrollo
  `proyectwin_mvp` queda rechazada por la guarda.
- No ejecuta E2E.
- Las pruebas que crean bases temporales (`RollbackAisladoTest` y
  `SeederIdempotenciaTest`) usan el usuario MySQL del servicio, que puede
  crear bases desechables.

## Ejecución local equivalente

Backend:

```powershell
cd backend
composer test
```

`composer test` crea, migra y elimina una base desechable `ptwin_test_*`. El
workflow de GitHub Actions usa directamente PHPUnit contra la base aislada del
servicio MySQL del job; no reutiliza bases locales.

Frontend:

```powershell
cd frontend
npm run lint
npm test
npm run build
```

Para el lint global, la configuración de Playwright usa globals de Node. Si
se agrega un nuevo archivo de configuración en la raíz de `frontend`, debe
incluirse en la regla correspondiente de `eslint.config.js`.

## Estado

El workflow está versionado, pero no se ha ejecutado todavía en GitHub Actions
desde este entorno local. La validación local de las suites no sustituye la
ejecución real del workflow.
