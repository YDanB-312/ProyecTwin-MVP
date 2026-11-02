# Gate E2E — ejecuciones 6AN, 6AP y Fase 6 (2-nov-2026)

> **Nota:** el gate ya está ejecutado. El archivo se llamó
> `E2E-GATE-PENDIENTE.md` durante la fase de preparación; el nombre actual
> refleja su estado real.
>
> **Estado: EJECUTADO Y APROBADO.** Última corrida (Fase 6): 11 specs / 25 tests,
> **25 passed**, exit 0, base desechable
> `ptwin_e2e_6an_20261102_162236_a61ac3`. No se accedió a `proyectwin_mvp`,
> `proyectwin_test` ni `proyectwin_e2e`.
>
> **Actualización Fase 3:** `E2E_DB_DATABASE` es obligatorio y solo se aceptan
> bases desechables `ptwin_e2e_6an_*`. Las menciones posteriores a
> `proyectwin_e2e` como valor por defecto son históricas y ya no aplican.

## 0. Reejecución final Fase 6

| Dato | Valor |
|---|---|
| Fecha | 2-nov-2026 |
| Base | `ptwin_e2e_6an_20261102_162236_a61ac3` (desechable, eliminada) |
| Resultado | **25/25 passed**, 0 failed, 0 skipped, **exit 0** |
| Duración | ~1.9 min |
| Extra | Reauditoría visual/accesibilidad **5/5** con base `ptwin_e2e_6an_20261102_162532_2fea20`; axe sin violaciones |

Se ejecutó después de las correcciones de rol/ficha en `DetalleUsuario`, la
restricción de notificaciones `sistema` y las correcciones de accesibilidad
H-01/H-02/H-03/H-04/H-05. Las dos bases se eliminaron y no se tocaron las bases
protegidas.

## 0b. Reejecución 6AP (histórica)

| Dato | Valor |
|---|---|
| Base | `ptwin_e2e_6an_20261102_022626` (desechable, eliminada) |
| Sesiones | `database` (base desechable) |
| Caché | `array` |
| Lock/views | La corrida 6AP usó rutas temporales; el wrapper actual usa `backend/storage/framework` |
| Resultado | **25/25 passed**, 0 failed, 0 skipped, **exit 0** |
| Duración | ~1.9 min |
| Log | `%TEMP%\ptwin-6ap-e2e-final2.log` |

La corrida 6AP se hizo después de los cambios de backend/frontend y del
endurecimiento del entrypoint. La base se eliminó y Mailpit se detuvo.

## 1. Resultado 6AN (histórico)

| Dato | Valor |
|---|---|
| Fecha | 2-nov-2026 |
| Base | `ptwin_e2e_6an_20261102_005108` (desechable, eliminada al terminar) |
| Backend | `127.0.0.1:8001` |
| Preview | `http://localhost:4173` |
| Correo | Mailpit `127.0.0.1:1025` / UI `127.0.0.1:8025` |
| Comando | `npx playwright test` |
| Resultado | **11 specs / 25 tests, 25 passed, 0 failed, 0 skipped** |
| Duración | ~1.9 min |
| Exit code | **0** |
| Log | `%TEMP%\ptwin-6an-e2e-rerun.log` |

La primera corrida de 6AN usó `ptwin_e2e_6an_20261102_002956`. Tras corregir
la propagación de errores de `migrate:fresh` y usar la ruta efectiva de la
configuración cacheada, se repitió el gate completo con
`ptwin_e2e_6an_20261102_005108`: **25/25, exit 0**.

### Resultado por spec

| Spec | Tests | Resultado |
|---|---:|---|
| `acceso.spec.js` | 4 | OK |
| `admin-usuarios-eliminar.spec.js` | 3 | OK |
| `correo.spec.js` | 2 | OK |
| `dashboards-permisos.spec.js` | 4 | OK |
| `fichas.spec.js` | 3 | OK |
| `notificaciones.spec.js` | 2 | OK |
| `propuestas.spec.js` | 1 | OK |
| `red-eliminacion-conserva-evidencia.spec.js` | 1 | OK |
| `similitud.spec.js` | 3 | OK |
| `soporte.spec.js` | 1 | OK |
| `usuarios.spec.js` | 1 | OK |
| **Total** | **25** | **25/25** |

Las 2 pruebas de `correo.spec.js` se ejecutaron contra Mailpit real en
`127.0.0.1:1025`. El resto no depende del correo, pero comparte el mismo
`globalSetup` y la misma base.

## 2. Diseño de aislamiento

El cambio mínimo consistió en parametrizar la base E2E sin debilitar las
guardas existentes:

- `E2E_DB_DATABASE` se lee en `frontend/playwright.config.js`,
  `frontend/e2e/global-setup.js` y `backend/app/Console/Commands/E2eReset.php`.
- **Valor por defecto histórico:** `proyectwin_e2e`. Desde la corrección de
  alcance de la Fase 3, ese default ya no existe y `proyectwin_e2e` no se acepta
  para pruebas.
- El comando actual `npm run test:e2e` crea una base desechable
  `ptwin_e2e_6an_<identificador>` y la elimina al terminar.
- Para una corrida directa se acepta solo:
  `^ptwin_e2e_6an_[a-z0-9_]{6,40}$`.
- Se rechazan `proyectwin_mvp`, `proyectwin_test`, `proyectwin_e2e` y
  cualquier nombre fuera del patrón.
- `DB_DATABASE` debe coincidir exactamente con `E2E_DB_DATABASE`.
- Se conservan: `E2E_ALLOW_RESET=1` obligatorio, `APP_ENV=e2e`, host loopback,
  rechazo de `DATABASE_URL`, rechazo de configuración cacheada, verificación de
  la conexión efectiva, lock exclusivo y `migrate:fresh --seed`.
- La base desechable se creó antes del reset. El comando no crea bases
  arbitrarias por sí solo.

### Evidencia de conexión efectiva

```text
Verificación E2E OK: APP_ENV=e2e
  · BD=ptwin_e2e_6an_20261102_005108
  · solicitada=ptwin_e2e_6an_20261102_005108
  · host=127.0.0.1 · puerto=3306
```

Comprobación directa posterior al gate:

```text
requested=ptwin_e2e_6an_20261102_005108
database=ptwin_e2e_6an_20261102_005108
tables=22
```

La base se eliminó solo después de verificar el nombre exacto y la conexión
efectiva. La comprobación posterior devolvió `missing` para ese nombre.

## 3. Bloqueo histórico de 6AM (resuelto)

6AM documentó el gate como **no ejecutado** porque:

1. `playwright.config.js` y `global-setup.js` fijaban `proyectwin_e2e`.
2. `E2eReset` solo aceptaba exactamente `proyectwin_e2e`.
3. No había Mailpit iniciado ni backend/preview en `:8001`/`:4173`.
4. No se autorizaba tocar `proyectwin_e2e`.

6AN resolvió los puntos 1-2 con `E2E_DB_DATABASE` y el patrón desechable, y los
puntos 3-4 iniciando solo Mailpit y usando una base nueva. Las guardas no se
desactivaron.

## 4. Limpieza

- Mailpit detenido (PIDs 49668 de la primera corrida y 156668 de la final).
- Puertos `1025`, `8025`, `8001` y `4173` sin listeners al finalizar.
- Bases `ptwin_e2e_6an_20261102_002956` y `ptwin_e2e_6an_20261102_005108`
  eliminadas y verificadas como ausentes.
- No se eliminaron imágenes, contenedores, volúmenes ni redes Docker.
- `frontend/test-results/.last-run.json` conserva el mismo contenido que el
  índice (`hash` de trabajo = `hash` indexado); Playwright actualizó su marca de
  tiempo, pero no el contenido versionado.

### Verificación negativa de guardas

| Caso | Resultado |
|---|---|
| Sin `E2E_ALLOW_RESET=1` | exit 1, abortado sin tocar la BD |
| `E2E_DB_DATABASE=base_arbitraria` | exit 1, patrón rechazado |
| `DB_DATABASE` distinto de `E2E_DB_DATABASE` | exit 1, coincidencia rechazada |

Log: `%TEMP%\ptwin-6an-guards-final.log`.

## 5. Reproducción actual

Requiere Mailpit iniciado:

```powershell
mailpit --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025
```

En otra terminal:

```powershell
cd frontend
npm run test:e2e
```

El wrapper crea una base desechable `ptwin_e2e_6an_<id>`, la resetea y la
elimina al terminar. No requiere exportar `E2E_ALLOW_RESET` ni
`E2E_DB_DATABASE`.

Para un escenario individual:

```powershell
npm run test:e2e -- --grep "nombre del escenario"
```

Las instrucciones anteriores de esta sección que exigían crear manualmente
`proyectwin_e2e` son históricas y ya no aplican.
