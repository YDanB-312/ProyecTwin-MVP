# Alcance del Backend – ProyecTwin MVP

> Este documento aclara qué parte del frontend cubre el backend actual y qué queda pendiente, respetando el patrón del instructor.

## 1. Estado del Backend actual

**Patrón seguido:** solo CRUD.

Cada controller tiene únicamente:

```php
index()  -> Model::included()->get()
store()  -> $request->validate([...]); Model::create($request->all())
show()   -> Model::included()->findOrFail($id)
update() -> $request->validate([...]); $model->update($request->all())
destroy()-> $model->delete()
```

* Sin `app/Services`, `Traits`, `Observers`, `FormRequests` ni `auth/Sanctum`.
* Validación inline, retorno directo del modelo.
* Excepción mínima: `ProjectController::index(Request $request)` acepta `?estado=` con un `where` simple (3 líneas).

**Cambios estructurales que SÍ se mantuvieron** (porque son simples `$table->...` y `$fillable`):

| Migración | Cambio |
|---|---|
| `training_programs` | `+ red` nullable |
| `class_groups` | `+ numero` nullable, `estado` → `activo,inactivo,finalizado` |
| `projects` | `+ id_class_group` FK nullable → `class_groups`, `estado` → `+en_progreso,completado,cancelado` |
| `notifications` | `tipo` → `+observacion` |
| `bug_reports` | `estado` → `+cerrado` |
| `comments` | `+ respuesta_a` FK nullable → `comments` (hilos) |

Modelos con solo `$fillable` / `$allowIncluded` / `scopeIncluded` / relaciones (`classGroup`, `parent/replies`) / `$casts` para `objetivos`, `entregables`, `detalles`.

**Seeder:** versión simple y legible en `database/seeders/DatabaseSeeder.php` (3 usuarios: 1 aprendiz, 1 instructor, 1 admin; 1 programa; 1 ficha; 1 aprendiz; 2 proyectos; 1 notificación; 1 bug report; 1 comentario). Sin lógica de similitud.

**Rutas:** `routes/api-v1.php` – solo CRUD de 13 recursos bajo prefijo `v1` (`/v1/projects`, `/v1/class-groups`, etc.). Verificado con `php artisan route:list`.

**Tests:** solo `ExampleTest` (unit + feature). Sin tests de lógica compleja.

## 2. Estado del Frontend

El frontend en `frontend/src/data/mockData.js` es **100% mock**.

* Persistencia: `localStorage` con clave `proyectwin_mock_v2` y `auth_user` / `sessionStorage` en `AuthContext.jsx`.
* No hay `fetch` / `axios` / `api.js`. Ninguna página hace llamadas REST.
* 58 funciones exportadas en `mockData.js` (ej. `detectarSimilitudes`, `joinFicha`, `getUnreadCount`, etc.) operan solo sobre el `state` en memoria + `localStorage`.

**Consecuencia:** el backend actual está listo como API, pero el frontend aún no está conectado a él. No hay desfase funcional hoy.

## 3. Qué SÍ cubre el backend simplificado

| Necesidad del frontend (`mockData.js`) | Endpoint CRUD que lo cubre |
|---|---|
| `getAllUsers` / `findUserById` / `createUser` / `updateUser` / `deleteUser` / `setUserActive` | `GET/POST /v1/general-users`, `GET/PUT/DELETE /v1/general-users/{id}` |
| `getAllFichas` / `findFichaById` / `createFicha` / `updateFicha` / `deleteFicha` | `GET/POST /v1/class-groups`, `GET/PUT/DELETE /v1/class-groups/{id}` |
| `getAllProjects` / `findProjectById` / `getProjectsByFicha` / `getPendingProjects` / `createProject` / `updateProject` / `updateProjectEstado` / `deleteProject` | `GET/POST /v1/projects`, `GET/PUT/DELETE /v1/projects/{id}` (el cambio de estado se hace vía `PUT` con `estado` en el body; filtro `?estado=pendiente` disponible) |
| `getAllSimilarities` / `findSimilarityById` / `updateSimilarityEstado` | `GET/POST /v1/similarities`, `GET/PUT/DELETE /v1/similarities/{id}` |
| `getAllBugReports` / `findBugReportById` / `createBugReport` / `updateBugReportEstado` | `GET/POST /v1/bug-reports`, `GET/PUT/DELETE /v1/bug-reports/{id}` |
| `getObservaciones` / `addObservacion` / `removeObservacion` (CRUD base) | `GET/POST /v1/comments`, `GET/PUT/DELETE /v1/comments/{id}` (con `respuesta_a`) |
| `getRedDePrograma` / `getProgramaDeProyecto` / `getAllPrograms` | `GET /v1/training-programs` + relaciones `?included=program,classGroup` |
| Listados generales (`assessments`, `apprentices`, `admins`) | Sus CRUD respectivos |

## 4. Qué NO cubre el backend simplificado

Estas funciones existen en el mock y **requerirían** endpoints o lógica que se eliminó para respetar el patrón solo-CRUD:

| Función frontend | Qué faltaría en el backend |
|---|---|
| `joinFicha(codigo, estudiante)` | `POST /v1/class-groups/join` → buscar ficha por `codigo` (mayúsculas), validar `estado != inactivo`, `Apprentice::updateOrCreate` |
| `leaveFicha(userId)` | `POST /v1/class-groups/{id}/leave` |
| `getEstudiantesDeFicha(fichaId)` | `GET /v1/class-groups/{id}/estudiantes` (`Apprentice::where(id_class_group)->with(generalUser)`) |
| `setUserFicha(userId, fichaId)` | `PUT /v1/general-users/{id}/ficha` |
| `getProjectsByStudent(studentId)` / `getBugReportsByReporter` | `GET /v1/projects? id_creador=` / `GET /v1/bug-reports? id_usuario=` o `GET .../user?id_usuario=` |
| `getProjectsByInstructor` / `getFichasDelInstructor` / `instructorVeProyecto` | `GET /v1/instructors/{id}/projects`, `/fichas` |
| `getNotificationsByUser` / `getUnreadNotificationsByUser` / `getUnreadCount` | `GET /v1/notifications/user?id_usuario=`, `GET /v1/notifications/unread?id_usuario=` |
| `markNotificationAsRead` / `markAllNotificationsAsRead` | `POST /v1/notifications/{id}/read`, `POST /v1/notifications/read-all` |
| `detectarSimilitudes(projectId)` | Motor inline en `ProjectController` (`tokenizar` → NFD/normalize/lower/split/`>2`/stopwords, `jaccard`, `UMBRAL 0.2`, corpus `estado=aprobado` mismo `TrainingProgram` vía `classGroup`, dedupe, `DB::transaction`, crea `Similarity` + `Notification`) |
| `createBugReport` → notifica a admins activos | Lógica en `BugReportController::store` (`GeneralUser::where(rol=admin, estado=true)` → `Notification::create` tipo `sistema`) |
| `addObservacion` con notificación por rol | `POST /v1/projects/{id}/comments` + `notificarObservacion` (aprendiz→instructor, instructor/admin→creador+integrantes) |

## 5. Conclusión

* **Cobertura actual con solo CRUD:** ~60-65% de las operaciones de datos del mock.
* **Faltante:** consultas filtradas por usuario/relación (`?id_usuario`, `?id_creador`, `unread`) y 3 piezas de lógica de negocio (`detectarSimilitudes`, `join/leave ficha`, notificaciones automáticas).
* **Todas las piezas faltantes se pueden implementar siguiendo el mismo patrón CRUD** (filtros `where` simples en el controller, sin Services), tal como se hizo en la versión previa verificada con `8 tests passed`. La decisión de no incluirlas ahora es explícita para mantener el código al nivel enseñado por el instructor.
* **El frontend no se rompe** por estar pendiente de conectar: seguirá funcionando con `localStorage` hasta que se reemplacen las llamadas a `mockData.js` por `fetch` a `/v1/...`.
