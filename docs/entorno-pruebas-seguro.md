# Entorno de pruebas seguro — ProyecTWIN MVP (sprint final, Paso 1)

> Objetivo del paso (B0 de la auditoría de fase 2): que PHPUnit y Playwright/E2E
> **no puedan borrar ni modificar** la base de datos de desarrollo (`proyectwin_mvp`).
> Las suites ahora se ejecutan **solo** contra bases dedicadas y **abortan** ante
> cualquier configuración insegura (nunca se degradan a una advertencia).

## 1. Bases de datos y entornos

| Entorno | Config que la define | Base de datos | Uso |
|---|---|---|---|
| Desarrollo | `backend/.env` | `proyectwin_mvp` | trabajo diario; las suites ya NO lo tocan |
| PHPUnit | `backend/phpunit.xml` + `backend/.env.testing` (local, **no versionado**) | `proyectwin_test` | `php artisan test` |
| E2E (Playwright) | variables de proceso en `frontend/playwright.config.js` y `frontend/e2e/global-setup.js` | `proyectwin_e2e` | suites E2E + backend servido en `:8001` |

Creación de las bases dedicadas (ya ejecutada el 18-oct-2026; este SQL se conserva para recrearlas y **requiere autorización explícita**):

```sql
CREATE DATABASE proyectwin_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE proyectwin_e2e  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`backend/.env.testing` y `backend/.env.e2e` se generaron copiando `.env` con dos overrides
(`APP_ENV` y `DB_DATABASE`) y están **ignorados por git** (`.gitignore` ampliado en este paso).

## 2. PHPUnit

Cadena real de resolución (verificada con `php artisan config:show`, no supuesta):

1. `phpunit.xml` fija por `<env>`: `APP_ENV=testing`, `DB_CONNECTION=mysql`, `DB_HOST=127.0.0.1`, `DB_DATABASE=proyectwin_test`.
2. Laravel carga `backend/.env.testing` (existe) porque `APP_ENV=testing`; de ahí salen `APP_KEY` y credenciales locales.
3. `tests/CreatesApplication.php` aplica la **guarda dura**: aborta si `APP_ENV !== testing`, si la BD no es exactamente `proyectwin_test` o si el host no es loopback. La excepción se lanza durante el bootstrap, **antes de cualquier setUp o escritura**.

Notas:
- No debe existir `bootstrap/cache/config.php` (config cacheada ignora `.env`). Si aparece: `php artisan config:clear`.
- La suite sigue usando `DatabaseTransactions`; con la BD correcta el rollback aplica como siempre.

Ejecución:

```
cd backend
php artisan test
```

## 3. E2E (Playwright)

> **Actualizado en 6AA (1-nov-2026):** el harness vigente es `frontend/playwright.config.js`
> + `frontend/e2e/global-setup.js`. Los runners `guardas-e2e.mjs`, `run-aislado.mjs`,
> `run-full.mjs`, `run-volumen.mjs` y `playwright.volumen.config.js` fueron retirados.
> Los conteos y gates de la sección 5 son históricos.

Guardas en capas — **todas** deben pasar:

| Capa | Qué exige | Dónde |
|---|---|---|
| 1. Autorización explícita | `E2E_ALLOW_RESET=1` en el proceso; sin ella `globalSetup` aborta con excepción antes de tocar la BD | `frontend/e2e/global-setup.js` |
| 2. Backend fijado | El webServer usa `APP_ENV=e2e`, `DB_HOST=127.0.0.1`, `DB_DATABASE=proyectwin_e2e` y API en `:8001`; no se toma de un `.env` de desarrollo | `frontend/playwright.config.js` |
| 3. Comando Artisan | `E2E_ALLOW_RESET=1`, `APP_ENV=e2e`, BD exactamente `proyectwin_e2e`, host loopback, sin `DATABASE_URL` ni config cacheada, conexión efectiva verificada y **lock** anti-reseeds concurrentes (`storage/framework/e2e-reset.lock`) | `backend/app/Console/Commands/E2eReset.php` |
| 4. Preview sin reutilización | `reuseExistingServer: false` para que el proxy corresponda a esta corrida | `frontend/playwright.config.js` |

Entradas: `npm run test:e2e` (Playwright). `npm test` ejecuta Vitest y no toca la BD E2E.
No existen ya las entradas `test:e2e:aislado`, `test:agentes*`, `test:volumen` ni `test:full`.

Preparación (con autorización previa):

```powershell
# 1) base dedicada creada y backend E2E disponible en :8001
# 2) frontend con autorización del reseed
cd frontend
$env:E2E_ALLOW_RESET='1'
npm run test:e2e
```

Advertencias:
- El harness fija `proyectwin_e2e`; usar otra base exigiría modificar `playwright.config.js`,
  `global-setup.js` y las guardas de `e2e:reset`. No se hace sin autorización.
- `playwright.config.js` usa un solo worker; los escenarios comparten la BD tras un único reset.
- El spec de correo requiere Mailpit en `127.0.0.1:1025` y `http://localhost:8025`.

## 4. Cómo comprobar que la BD de desarrollo no se toca

1. Antes/después de una corrida autorizada: conteos y `CHECKSUM TABLE` de tablas clave de `proyectwin_mvp` deben ser idénticos.
2. Los logs del backend E2E deben mostrar conexión a `proyectwin_e2e`.
3. Prueba de humo de guardas: sin `E2E_ALLOW_RESET` o apuntando a `:8000`, la ejecución debe **fallar**, no advertir.

## 5. Estado de la implementación

> **Nota 6AA:** los conteos y gates de esta sección son históricos (Lotes 1–13, oct-2026).
> La suite vigente es **225 tests / 1280 aserciones** (PHPUnit) y **90 tests / 26 archivos**
> (Vitest); E2E vigente: **11 specs / 25 tests**, no ejecutados en 6AA.

- **Paso 1 (histórico):** quedaron implementadas las guardas PHPUnit + E2E, el comando `e2e:reset`, los entornos locales, los runners actualizados y este documento; la creación de las bases y la ejecución de las suites quedaron pendientes de autorización.
- **Ejecutado y verificado (18-oct-2026):**
  - `proyectwin_test` y `proyectwin_e2e` creadas y migradas.
  - `php artisan test` con la guarda activa: **314/314 pruebas, 1797 aserciones** (certificación histórica de ese punto — Lote 1; `proyectwin_test` limpia al terminar).
  - **Resultado posterior (misma fecha, Lote 6): 324/324 pruebas, 1883 aserciones**, tras añadir las pruebas de D-01 (borrado de usuarios con evidencia).
  - **Re-certificación (19-oct-2026, Lote 12):** `php artisan test` → **341/341 pruebas, 1985 aserciones** (exit 0, ~27 s), tras añadir C-02/C-03/C-06/C-09/M5; suite Android **169/169** + `assembleDebug` OK.
  - **Lote 13 (19-oct-2026):** `php artisan test` → **344/344 pruebas, 1998 aserciones** (exit 0, ~26 s; P-03/P-01); Android unit **175/175** + `assembleDebug` + `assembleDebugAndroidTest` OK; smoke instrumentado de arranque OK en dispositivo físico (componentes pendientes de corrida en dispositivo libre).
  - Gate E2E integral: **5/5 etapas, 347 pruebas** (53 archivos aislados + agentes/cobertura/negativos/volumen), con backend E2E en `:8001` y reseed contra `proyectwin_e2e`.
  - **Gate E2E aprobado (22-oct-2026): 5/5 etapas OK, 26.4 min**, con artefactos externos en
    `C:\Users\munoz\AppData\Local\Temp\proyectwin-e2e-gate-20261022-221818`.
    - Etapa 1: E2E determinista + navegación, OK.
    - Etapa 2: agentes de acción + simulaciones, OK.
    - Etapa 3: cobertura de controles, OK.
    - Etapa 4: negativos, OK.
    - Etapa 5: volumen de datos, OK.
    - Backend E2E temporal en `127.0.0.1:8001`; preview automático en `4173`; resets únicamente sobre `proyectwin_e2e`.
    - La primera corrida detectó un dato incorrecto en el agente admin (usuario semilla con historial). Se corrigió el playbook para borrar el usuario temporal creado por el propio agente y se repitió el gate completo.
    - Este resultado no resuelve C-07/BUG-037 (`TrustProxies`), que sigue pendiente de evidencia autorizada de Railway, ni autoriza operaciones sobre producción o `proyectwin_mvp`.
  - Los 4 specs E2E nuevos del Lote 2 (`doble-submit-completo`, `fechas-formato`, `notificacion-ficha-navegacion`, `red-evidencia-409`) se ejecutaron y aprobaron en el gate.
  - Guardas probadas en negativo: sin `E2E_ALLOW_RESET` o apuntando a `:8000` la corrida falla; PHPUnit aborta fuera de `testing` + `proyectwin_test`.
- **Requiere autorización explícita:** volver a ejecutar el gate E2E integral (un único run completo) y cualquier `e2e:reset`/migración sobre las bases dedicadas. Nota: los refuerzos del Lote 3 a 2 specs E2E de frontend (`fechas-formato` y `notificacion-ficha-navegacion`) fueron ejecutados y aprobados el 18-oct con autorización focal (5 tests autorizados + 4 adicionales aprobados por coincidencia de filtro).
- **Limitación conocida (histórico, Paso 1):** la dependencia de datos del seed de demo (p. ej. `SimilarityScopeTest`) quedó absorbida por la suite actual: no hay seeder en `phpunit.xml` ni llamadas a `seed()` en `tests/`, y `SimilarityScopeTest` limpia las similitudes preexistentes; si reaparece, se definirá un seeder de test determinista.
