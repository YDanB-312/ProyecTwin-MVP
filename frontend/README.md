# ProyecTwin — Frontend

Frontend del MVP de ProyecTwin, plataforma de análisis de similitud para
proyectos formativos. Consume la API real de Laravel; no hay datos mock en la
aplicación.

- **Stack:** React 19 + Vite 8, React Router 7, CSS Modules, phosphor-react.
- **API:** `/v1` mediante el proxy de Vite hacia `http://127.0.0.1:8000`.
- **Pruebas:** Vitest + React Testing Library y Playwright.

## Cuentas demo y secretos

El seed demo está bloqueado fuera de `local`, `testing` y `e2e`. No se
publican correos, usuarios ni contraseñas compartidas en la documentación. Si
usas el seed local, define tu propia `DEMO_PASSWORD` en el `.env` del backend;
no uses esas cuentas en producción. Consulta el procedimiento en
[`../backend/README.md`](../backend/README.md).

## Scripts

```bash
npm install        # instalar dependencias
npm ci             # alternativa reproducible (CI)
npm run dev        # servidor de desarrollo
npm run build      # build de producción
npm run lint       # ESLint
npm run preview    # servir el build; lo usa Playwright
npm test           # Vitest + React Testing Library
npm run test:e2e   # Playwright con base desechable automática; requiere Mailpit
npm run quality    # lint + Vitest + build
```

`npm test` no ejecuta Playwright. `npm run test:e2e` ejecuta el wrapper
`backend/scripts/run-e2e.php`, que crea una base desechable
`ptwin_e2e_6an_*`, ejecuta Playwright y la elimina. Solo requiere Mailpit
iniciado; no hay que exportar `E2E_DB_DATABASE` ni `E2E_ALLOW_RESET`.

Prueba E2E individual:

```bash
npm run test:e2e -- --grep "nombre del escenario"
```

## Entorno local

1. Backend:

```bash
cd ../backend
composer install
cp .env.example .env
php artisan key:generate
# Crea tu propia base local vacía y ajusta DB_DATABASE.
# No migres ni recrees una base existente con datos sin autorización.
php artisan serve
```

2. Frontend:

```bash
npm install
npm run dev
```

No necesitas `frontend/.env`: `/v1` es el valor predeterminado. Solo define
`VITE_API_URL` si quieres cambiar el prefijo.

3. Correo local: descarga Mailpit desde
   `https://github.com/axllent/mailpit/releases`, ejecuta `mailpit.exe` y abre
   `http://127.0.0.1:8025`. El backend local usa SMTP en `127.0.0.1:1025`.

No uses `migrate:fresh`, `db:wipe` ni resets sobre tu base de desarrollo
(`proyectwin_mvp` es solo un ejemplo).

## Cobertura E2E

| Spec | Qué cubre |
|---|---|
| `acceso.spec.js` | Login/logout del aprendiz, credenciales inválidas, ruta protegida y landing pública |
| `dashboards-permisos.spec.js` | Indicadores por rol y acceso denegado a secciones ajenas |
| `usuarios.spec.js` | Alta de instructor/aprendiz por el admin y consulta del detalle |
| `fichas.spec.js` | Red + programa, ficha creada por admin y ficha creada por instructor |
| `propuestas.spec.js` | Borrador con integrantes, envío, rechazo, corrección, reenvío, aprobación e historial |
| `similitud.spec.js` | Coincidencias del aprendiz, del instructor y supervisión del admin |
| `notificaciones.spec.js` | Aviso de ficha navegable y marca de lectura de una notificación |
| `soporte.spec.js` | Reporte de falla del aprendiz y cierre con respuesta del admin |
| `correo.spec.js` | Activación y recuperación con correos reales capturados por Mailpit |
| `admin-usuarios-eliminar.spec.js` | Confirmación reforzada, borrado de usuario y correo duplicado |
| `red-eliminacion-conserva-evidencia.spec.js` | Borrado de red que conserva propuesta y similitud |

**Estado:** 25 escenarios en 11 archivos, ejecutados y aprobados (**25/25**).
La suite se ejecuta con `npm run test:e2e`; el wrapper crea y elimina una base
desechable `ptwin_e2e_6an_*`, y el `globalSetup` la resea con
`php artisan e2e:reset`. Playwright usa un solo worker. Los escenarios de correo
requieren Mailpit en `127.0.0.1:1025` y su bandeja en
`http://127.0.0.1:8025`.

## Tests unitarios

`npm test` - **110 tests en 31 archivos** (Fase 6, 2-nov-2026; lista representativa):

| Archivo | Qué cubre |
|---|---|
| `src/utils/helpers.test.js` | `iniciales`, `formatearFecha`, `agruparObservaciones` |
| `src/utils/validation.test.js` | email, contraseña y límites de formularios |
| `src/lib/api.test.js` | sesión expirada (401), descarga y login |
| `src/hooks/useFocusTrap.test.jsx` | foco inicial, ciclo Tab/Shift+Tab, Escape |
| `src/constants/routes.test.js` | rutas por rol, alertas y soporte |
| `src/components/FormField/FormField.test.jsx` | asociación label-control, error/help, requerido |
| `src/components/Input/Input.test.jsx` | `PasswordInput`: visibilidad y props |
| `src/components/DataTable/DataTable.test.jsx` | estado vacío y `data-label` para tarjetas |
| `src/components/ProtectedRoute/ProtectedRoute.test.jsx` | redirección y roles permitidos |
| `src/components/PerfilBase/PerfilBase.test.jsx` | edición de perfil y estados |
| `src/components/DashboardMetrics/DashboardMetrics.test.jsx` | indicadores y estados de carga |
| `src/components/Charts/Charts.test.jsx` | semántica, teclado y reglas axe de los gráficos |
| `src/pages/public/ActivarCuenta/ActivarCuenta.test.jsx` | activación, validación y errores |
| `src/components/GovernmentBar/GovernmentBar.test.jsx` | estado del motor con texto accesible y ARIA válida |
| `src/pages/admin/FichasAdmin/FichasAdmin.test.jsx` | `fieldset`/`legend` y buscador de aprendices con nombre accesible |
| `src/pages/admin/DetalleUsuario/DetalleUsuario.test.jsx` | cambio de rol y ficha en el mismo guardado |

Los recorridos completos se verifican en Playwright; no se duplican en Vitest.

## Arquitectura

```text
src/
├── components/   # componentes reutilizables y *Base compartidos
├── pages/        # páginas por rol: aprendiz, instructor, admin, public
├── layouts/      # DashboardLayout, LandingLayout, AuthLayout
├── contexts/     # AuthContext, TemaContext, MotorContext
├── constants/    # etiquetas, estados, roles y rutas
├── tokens/       # tokens de diseño CSS
├── utils/        # helpers y validaciones
└── App.jsx       # enrutado y control de acceso
```

- CSS Modules y estilos globales; sin Tailwind. Hay estilos inline puntuales
  donde el componente lo requiere (avatares, gráficas y la demo del Home).
- La sesión usa cookie httpOnly y CSRF.
- Los tests de Playwright y Vitest no comparten datos de la aplicación.
