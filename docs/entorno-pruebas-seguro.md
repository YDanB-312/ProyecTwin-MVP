# Entorno de pruebas seguro — ProyecTWIN MVP (sprint final, Paso 1)

> Objetivo del paso (B0 de la auditoría de fase 2): que PHPUnit y Playwright/E2E
> **no puedan borrar ni modificar** tu base de datos de desarrollo (por ejemplo `proyectwin_mvp`).
> Las suites ahora se ejecutan **solo** contra bases dedicadas y **abortan** ante
> cualquier configuración insegura (nunca se degradan a una advertencia).

## 1. Bases de datos y entornos

| Entorno | Config que la define | Base de datos | Uso |
|---|---|---|---|
| Desarrollo | `backend/.env` | base local propia; `proyectwin_mvp` es solo el nombre de ejemplo | trabajo diario; las suites NO la tocan |
| PHPUnit | `composer test` + `backend/scripts/run-tests.php` | base desechable `ptwin_test_<id>` | `composer test` |
| E2E (Playwright) | `npm run test:e2e` + `backend/scripts/run-e2e.php` | base desechable `ptwin_e2e_6an_<id>` obligatoria | suites E2E + backend servido en `:8001` |

`proyectwin_mvp` es el nombre de la base local del propietario actual y el valor
de ejemplo del `.env.example`; cada desarrollador debe crear su propia base
local vacía y ajustar `DB_DATABASE`. No existe una base fija compartida.
Las pruebas no usan bases fijas: crean una base desechable propia y la eliminan
al terminar.

Los wrappers crean y eliminan automáticamente las bases `ptwin_test_*` y
`ptwin_e2e_6an_*`. **No las crees manualmente** para ejecutar las suites; si un
procedimiento autorizado lo exige, verifica antes que el nombre no exista y que
no sea una base protegida.

Los wrappers actuales **no usan** `backend/.env.testing` ni `backend/.env.e2e`:
inyectan `APP_ENV` y `DB_DATABASE` directamente en el proceso hijo. No crees ni
versionas esos archivos. Antes de cualquier `migrate` o seed, confirma que la
base sea desechable y no `proyectwin_mvp`.

## 2. PHPUnit

Comando principal:

```powershell
cd backend
composer test
```

`composer test` ejecuta `scripts/run-tests.php`:

1. comprueba que exista `backend/.env` y que no haya configuración cacheada;
2. rechaza `DATABASE_URL`/`DB_URL` y valida MySQL, loopback y puerto 3306;
3. crea una base desechable `ptwin_test_*`;
4. fuerza esa base en Laravel y verifica `SELECT DATABASE()`/`@@port`;
5. migra en el mismo proceso y ejecuta PHPUnit con el nombre exacto;
6. elimina la base desechable al terminar, incluso si la suite falla.

Para una prueba individual:

```powershell
cd backend
composer test -- --filter NombreDelTest
```

`phpunit.xml` fija `APP_ENV=testing`, `DB_CONNECTION=mysql` y `DB_HOST=127.0.0.1`,
pero **no fija `DB_DATABASE`**. La guarda de `tests/CreatesApplication.php`
rechaza `proyectwin_mvp`, `proyectwin_test`, `proyectwin_e2e` y cualquier nombre
fuera de `ptwin_test_*`.

Notas:
- No debe existir `bootstrap/cache/config.php`. Si aparece:
  `php artisan config:clear`.
- La suite sigue usando `DatabaseTransactions`.
- **C-06 (6AO):** `tests/Core/Operations/E2eResetGuardasTest.php` cubre las
  ramas de aborto de `e2e:reset`.

## 3. E2E (Playwright)

> **Actualizado en 6AA (1-nov-2026):** el harness vigente es `frontend/playwright.config.js`
> + `frontend/e2e/global-setup.js`. Los runners `guardas-e2e.mjs`, `run-aislado.mjs`,
> `run-full.mjs`, `run-volumen.mjs` y `playwright.volumen.config.js` fueron retirados.
> Los conteos y gates de la sección 5 son históricos.

Guardas en capas — **todas** deben pasar:

| Capa | Qué exige | Dónde |
|---|---|---|
| 1. Autorización explícita | `E2E_ALLOW_RESET=1` en el entorno (Playwright lo fija en el proceso); sin ella `globalSetup` aborta con excepción antes de tocar la BD | `frontend/e2e/global-setup.js` |
| 2. Backend fijado | El webServer usa `APP_ENV=e2e`, `DB_HOST=127.0.0.1`, `DB_DATABASE=E2E_DB_DATABASE` (obligatorio, sin default) y API en `:8001`; no se toma de un `.env` de desarrollo | `frontend/playwright.config.js` |
| 3. Comando Artisan | `E2E_ALLOW_RESET=1`, `APP_ENV=e2e`, BD igual a `E2E_DB_DATABASE` (solo desechable `ptwin_e2e_6an_<id>`), host loopback, sin `DATABASE_URL` ni config cacheada, conexión efectiva verificada y **lock** anti-reseeds concurrentes (`storage/framework/e2e-reset.lock`) | `backend/app/Console/Commands/E2eReset.php` |
| 4. Preview sin reutilización | `reuseExistingServer: false` para que el proxy corresponda a esta corrida | `frontend/playwright.config.js` |

Entradas: `npm run test:e2e` ejecuta el wrapper `backend/scripts/run-e2e.php`,
que verifica Mailpit, crea una base desechable, ejecuta Playwright y la elimina.
`npm test` ejecuta Vitest y no toca la BD E2E. No existen ya las entradas
`test:e2e:aislado`, `test:agentes*`, `test:volumen` ni `test:full`.

**Límites conocidos:** `E2E_ALLOW_RESET` se lee del entorno de Laravel; una
ejecución directa de Artisan puede tomarlo del `.env`, mientras que Playwright
lo fija en el proceso. No es un control frente a un operador con acceso al
servidor. La validación de host comprueba el loopback configurado y la base y
puerto reales; un túnel local a MySQL no es distinguible desde la aplicación.
Las guardas de base (patrón, coincidencia y conexión efectiva) siguen activas.

Comando principal:

```powershell
cd frontend
npm run test:e2e
```

El wrapper:

1. verifica que Mailpit escuche en `127.0.0.1:1025` y `127.0.0.1:8025`;
2. crea una base desechable `ptwin_e2e_6an_*`;
3. define `E2E_ALLOW_RESET=1` y `E2E_DB_DATABASE`;
4. ejecuta Playwright;
5. elimina la base al terminar, incluso si la suite falla.

Prueba individual:

```powershell
cd frontend
npm run test:e2e -- --grep "nombre del escenario"
```

Si Mailpit no está disponible, el comando aborta antes de crear la base con un
mensaje claro. La base desechable debe cumplir
`^ptwin_e2e_6an_[a-z0-9_]{6,40}$`; el comando rechaza `proyectwin_mvp`,
`proyectwin_test`, `proyectwin_e2e` y nombres fuera del patrón.

`playwright.config.js` usa un solo worker; los escenarios comparten la BD tras un
único reset. El spec de correo requiere Mailpit en `127.0.0.1:1025` y
`http://127.0.0.1:8025`.

## 4. Cómo comprobar que la BD de desarrollo no se toca

1. Antes/después de una corrida autorizada: conteos y `CHECKSUM TABLE` de tablas clave de tu base de desarrollo (por ejemplo `proyectwin_mvp`) deben ser idénticos.
2. Los logs del backend E2E deben mostrar conexión a la base desechable
   indicada por `E2E_DB_DATABASE`.
3. Prueba de humo de guardas: sin `E2E_ALLOW_RESET` o apuntando a `:8000`, la ejecución debe **fallar**, no advertir.

## 5. Estado de la implementación

> **Nota Fase 6:** los conteos y gates de esta sección son históricos (Lotes 1–13,
> oct-2026). Las menciones a `proyectwin_test` y `proyectwin_e2e` son
> **históricas**: hoy solo se usan `ptwin_test_*` y `ptwin_e2e_6an_*`; no
> recrees ni migres las bases protegidas.
>
> Última evidencia (2-nov-2026): **312 tests / 1698 aserciones** (PHPUnit),
> **110 tests / 31 archivos** (Vitest) y **25 E2E** aprobados. El E2E se
> reejecutó con la base desechable `ptwin_e2e_6an_20261102_162236_a61ac3`
> (eliminada) y la reauditoría visual/accesibilidad sumó 5/5 con axe sin
> violaciones.

- **Paso 1 (histórico):** quedaron implementadas las guardas PHPUnit + E2E, el comando `e2e:reset`, los entornos locales, los runners actualizados y este documento; la creación de las bases y la ejecución de las suites quedaron pendientes de autorización.
- **Ejecutado y verificado (18-oct-2026, histórico):**
  - Se usaron bases fijas `proyectwin_test` y `proyectwin_e2e`; ese procedimiento ya no está permitido.
  - `php artisan test` con la guarda activa: **314/314 pruebas, 1797 aserciones** (certificación histórica de ese punto — Lote 1; `proyectwin_test` limpia al terminar).
  - **Resultado posterior (misma fecha, Lote 6): 324/324 pruebas, 1883 aserciones**, tras añadir las pruebas de D-01 (borrado de usuarios con evidencia).
  - **Re-certificación (19-oct-2026, Lote 12):** `php artisan test` → **341/341 pruebas, 1985 aserciones** (exit 0, ~27 s), tras añadir C-02/C-03/C-06/C-09/M5; suite Android **169/169** + `assembleDebug` OK.
  - **Lote 13 (19-oct-2026):** `php artisan test` → **344/344 pruebas, 1998 aserciones** (exit 0, ~26 s; P-03/P-01); Android unit **175/175** + `assembleDebug` + `assembleDebugAndroidTest` OK; smoke instrumentado de arranque OK en dispositivo físico (componentes pendientes de corrida en dispositivo libre).
  - Gate E2E integral: **5/5 etapas, 347 pruebas** (53 archivos aislados + agentes/cobertura/negativos/volumen), con backend E2E en `:8001` y reseed contra `proyectwin_e2e`.
  - **Gate E2E aprobado (22-oct-2026): 5/5 etapas OK, 26.4 min**, con artefactos externos en
    `%TEMP%\proyectwin-e2e-gate-20261022-221818` (ruta local del autor).
    - Etapa 1: E2E determinista + navegación, OK.
    - Etapa 2: agentes de acción + simulaciones, OK.
    - Etapa 3: cobertura de controles, OK.
    - Etapa 4: negativos, OK.
    - Etapa 5: volumen de datos, OK.
    - Backend E2E temporal en `127.0.0.1:8001`; preview automático en `4173`; resets únicamente sobre `proyectwin_e2e`.
    - La primera corrida detectó un dato incorrecto en el agente admin (usuario semilla con historial). Se corrigió el playbook para borrar el usuario temporal creado por el propio agente y se repitió el gate completo.
    - Este resultado no resuelve C-07/BUG-037 (`TrustProxies`), que sigue pendiente de evidencia autorizada de Railway, ni autoriza operaciones sobre producción o `proyectwin_mvp`.
  - Los 4 specs E2E nuevos del Lote 2 (`doble-submit-completo`, `fechas-formato`, `notificacion-ficha-navegacion`, `red-evidencia-409`) se ejecutaron y aprobaron en el gate.
  - Guardas probadas en negativo: sin `E2E_ALLOW_RESET` o apuntando a `:8000` la corrida falla; PHPUnit aborta fuera de `testing` o con una base que no sea `ptwin_test_*`.
  - **Gate E2E 6AN (2-nov-2026):** **11 specs / 25 tests, 25 passed, exit 0**, con Mailpit y la base desechable `ptwin_e2e_6an_20261102_005108`; backend `:8001`, preview `:4173`; la base se eliminó y Mailpit se detuvo al terminar. No se accedió a `proyectwin_mvp`, `proyectwin_test` ni `proyectwin_e2e`.
  - **Gate E2E 6AP (2-nov-2026):** reejecución final **11 specs / 25 tests, 25 passed, exit 0**, base `ptwin_e2e_6an_20261102_022626`, sesiones en base de datos, caché en array y lock/views en rutas temporales; la base se eliminó y Mailpit se detuvo.
- **Autorización y estado:** el gate E2E integral se reejecutó en Fase 6 (25/25, base desechable eliminada). Cualquier nueva corrida integral y cualquier `e2e:reset`/migración sobre las bases dedicadas sigue requiriendo autorización explícita. La corrida aislada sobre `ptwin_e2e_6an_<id>` ya está habilitada por el patrón y no requiere tocar las bases protegidas. Nota: los refuerzos del Lote 3 a 2 specs E2E de frontend (`fechas-formato` y `notificacion-ficha-navegacion`) fueron ejecutados y aprobados el 18-oct con autorización focal (5 tests autorizados + 4 adicionales aprobados por coincidencia de filtro).
- **Limitación conocida (histórico, Paso 1):** la dependencia de datos del seed de demo (p. ej. `SimilarityScopeTest`) quedó absorbida por la suite actual: no hay seeder en `phpunit.xml` ni llamadas a `seed()` en `tests/`, y `SimilarityScopeTest` limpia las similitudes preexistentes; si reaparece, se definirá un seeder de test determinista.
