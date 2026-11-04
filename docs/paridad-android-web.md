# Paridad funcional web → Android

Fecha de auditoría: 2026-11-02
Rama: `master`
Alcance: paridad funcional e informativa. La interfaz Android es nativa; no se
copia el CSS ni se rediseña la web.

## Leyenda

| Estado | Significado |
|---|---|
| **Completo** | Flujo equivalente implementado en Android y respaldado por el contrato actual del backend. |
| **Parcial** | Flujo principal disponible, con una diferencia deliberada o una parte bloqueada. |
| **Ausente** | La web lo ofrece y Android no lo tiene. |
| **Bloqueado por API/infraestructura** | No se puede completar sin un cambio externo no autorizado. |
| **No aplica** | Elemento propio de la web que no tiene equivalente necesario en la app. |

**Actualización 3-nov-2026:** la verificación por rol se realizó contra el
backend desplegado usando únicamente cuentas de prueba autorizadas y el emulador
`Medium_Phone_API_36.1`. Se verificaron login, dashboards de aprendiz, instructor
y administrador, centro de actualizaciones y aviso/notificación de nueva versión.
No se modificaron datos de producción. Las capturas reales están en
`Movil/app/src/main/res/drawable-nodpi/manual_*.jpg` y el detalle está en
`docs/manual-usuario-android.md`.

## Matriz

### Autenticación y cuenta

| Flujo web | Implementación Android | Estado | Notas |
|---|---|---|---|
| Login con correo | `LoginScreen`, `AuthRepositoryImpl`, `LoginRequest` | **Completo** | El correo es el único identificador de acceso. Valida localmente el formato y muestra el error del backend para cuentas suspendidas o pendientes. La política de 12 caracteres se aplica al establecer/restablecer la contraseña. |
| Recuperar contraseña | `ForgotPasswordScreen`, `ConfirmationScreen`, `ResetPasswordScreen` | **Completo** | Conserva el contrato de token/correo y la política de 12 caracteres. |
| Activar cuenta con enlace | `ActivateAccountScreen`, `/auth/activation/validate`, `/auth/activation` | **Parcial** | Permite pegar el enlace completo o el código y valida antes de mostrar el formulario. La apertura automática desde el correo requiere App Links/asset links y cambios de infraestructura no autorizados. |
| Cuenta pendiente de activación | Login 403 `cuenta_no_activada` + CTA a activación | **Completo** | El error tipado muestra el botón para pegar el enlace. |
| `must_change_password` legado | `CambioObligatorioScreen` conservada como respaldo | **No aplica** | El backend actual ya no emite ese bloqueo; se deja el respaldo para cuentas antiguas sin mostrarlo en el flujo nuevo. |

### Aprendiz

| Pantalla web | Implementación Android | Estado | Notas |
|---|---|---|---|
| Dashboard | `DashboardScreen` | **Completo** | Sin cambios de contrato en esta fase. |
| Propuestas | `ProjectsScreen` | **Completo** | Listado, edición y detalle existentes. |
| Detalle de propuesta | `ProjectDetailScreen` | **Completo** | Incluye envío explícito; al enviar pasa por el análisis. |
| Analizando proyecto | `AnalizandoProyectoScreen` | **Completo** | Espera el resultado real de `/similarities?proyecto_id=`; no simula el 100 %. Avisa integrantes no vinculados. |
| Resultado del análisis | `ResultadoAnalisisScreen` | **Completo** | Máxima coincidencia, grado relativo al umbral real, pares y recomendaciones. |
| Similitudes / detalle | `SimilitudesScreen`, `SimilarityDetailScreen` | **Completo** | Ya existentes; usan umbral y pares reales. |
| Mi ficha | `FichaDetailScreen` | **Completo** | Incluye roster y enlace al instructor cuando el contrato trae su usuario. |
| Perfil de compañero | `DetalleCompaneroScreen` | **Completo** | Ya existente. |
| Mi instructor | `DetalleInstructorScreen` | **Completo** | Nuevo; perfil público y fichas a cargo. |
| Reportar falla | `ReportIssueScreen` | **Completo** | Compartido con instructor. |
| Alertas | `AlertsScreen` | **Completo** | Ya existente. |
| Perfil | `ProfileScreen` | **Completo** | Incluye cambio de correo, contraseña y soporte. |

### Instructor

| Pantalla web | Implementación Android | Estado | Notas |
|---|---|---|---|
| Dashboard | `InstructorDashboardScreen` | **Completo** | Existente. |
| Revisión de propuestas | `RevisionPropuestasScreen` + `ProjectDetailScreen` | **Completo** | Aprobar/rechazar con observación. |
| Detalle de propuesta | `ProjectDetailScreen` | **Completo** | Envío permitido; el resultado del motor se refleja al recargar. |
| Similitudes / detalle | `SimilitudesInstructorScreen`, `SimilarityDetailScreen` | **Completo** | Existente. |
| Fichas | `FichaDirectoryScreen`, `ManageFichasScreen` | **Completo** | Incluye directorio y roster. |
| Detalle de ficha | `DetalleFichaInstructorScreen` | **Completo** | Datos, aprendices y edición. |
| Crear/editar ficha y aprendiz | `CrearFichaScreen` | **Completo** | Alta de aprendiz con activación real, no contraseña temporal. |
| Reportar falla | `ReportIssueScreen` | **Completo** | Compartido. |
| Alertas / perfil | `AlertsScreen`, `InstructorProfileScreen` | **Completo** | Existente. |

### Administrador

| Pantalla web | Implementación Android | Estado | Notas |
|---|---|---|---|
| Dashboard | `AdminDashboardScreen` | **Completo** | Existente; métricas y salud del motor. |
| Usuarios | `UserManagementScreen` | **Parcial** | Alta con `{usuario, activacion}`, estado “Pendiente”, reenvío desde la fila y detalle completo. La eliminación directa desde la lista no replica el diálogo de decisión por historial; debe hacerse desde el detalle. |
| Alta de usuario | `NewUserScreen` | **Completo** | Sin contraseña temporal; muestra correo, estado del enlace y reenvío. |
| Detalle de usuario | `UserDetailScreen`, `UserDetailViewModel` | **Completo** | Edita nombre, correo, documento, foto, rol y estado; reenvía activación; restablecimiento por enlace; borrado con `409`, impacto y hash de confirmación. |
| Redes y programas | `AdminCatalogosScreen` | **Completo** | Existente. |
| Configuración del motor | `AdminMotorScreen` | **Completo** | Existente. |
| Fichas y detalle | `AdminFichasScreen`, `AdminFichaDetailScreen` | **Completo** | Existente. |
| Proyectos y detalle | `AdminProjectsScreen`, `ProjectDetailScreen` | **Completo** | Existente. |
| Similitudes y detalle | `AdminSimilarityListScreen`, `AdminSimilarityDetailScreen` | **Completo** | Existente. |
| Reportes de fallas y detalle | `BugReportsScreen`, `BugReportDetailScreen` | **Completo** | Existente. |
| Notificaciones | `AlertsScreen` | **Completo** | Existente; enlace en dashboard. |
| Bitácora | `AdminBitacoraScreen` | **Completo** | Etiquetas actualizadas a los eventos nuevos (`reenviar_activacion`, `restablecer_clave_administrativa`). |
| Perfil | `AdminProfileScreen` | **Completo** | Existente. |

### Público / infraestructura

| Elemento web | Android | Estado | Notas |
|---|---|---|---|
| Landing pública (`/`) | No hay pantalla de marketing | **No aplica** | La app inicia en login; no hay flujo de rol ni datos que replicar. |
| 404 | `NotFoundScreen` | **Completo** | Existente. |
| Acceso denegado | Estados de error/permiso en cada pantalla | **Parcial** | No hay una pantalla dedicada; cada API 403 se muestra con el mensaje del backend. |
| Enlace de activación automático | Apertura manual del enlace | **Bloqueado por infraestructura** | Requiere App Links/asset links y cambios de despliegue no autorizados. |
| Publicación/descarga APK | `GET /v1/app-version/android`, `/apk/ProyecTWin.apk`, Home | **Completo** | APK `1.4.1/6` firmado, desplegado y descargable. El endpoint responde `1.4.1/6`; `/apk/ProyecTWin.apk` responde 200 y el resto de `/apk/**` 404. Actualización `1.4.0/5 → 1.4.1/6` verificada. |

## Contratos corregidos

- `POST /v1/auth/login`: `{ correo, password, recordarme? }`. El login por `username` queda rechazado y el DTO Android ya no lo envía.
- `POST /v1/auth/activation/validate` y `POST /v1/auth/activation`.
- `POST /v1/general-users` → `{usuario, activacion}`.
- `POST /v1/general-users/{id}/activacion/reenviar`.
- `POST /v1/general-users/{id}/credenciales/restablecer` → mensaje, sin credenciales temporales.
- `DELETE /v1/general-users/{id}` con `confirmar_eliminacion` y `confirmacion_hash`; parseo de `409`, `impacto` y `puede_desactivar`.
- `POST /v1/class-groups/{id}/apprentices` → `activacion`, sin `credenciales`.

## Pruebas ejecutadas (histórico 2-nov-2026)

> La tabla siguiente conserva los resultados del 2-nov-2026. La verificación
> vigente del 3-nov-2026 está en la sección “Actualización 3-nov-2026”.

| Comando | Resultado |
|---|---|
| `.\gradlew.bat :app:compileDebugKotlin --console=plain` | **Éxito** (solo warnings de deprecación de APIs de Compose/iconos). |
| `.\gradlew.bat :app:testDebugUnitTest --console=plain` | **Éxito**: 203 pruebas, 0 fallos, 0 errores. Incluye política de contraseña, activación, contratos, versionCode, SHA-256 y rutas de análisis. |
| `.\gradlew.bat :app:compileDebugAndroidTestKotlin --console=plain` | **Éxito**; pruebas instrumentadas compiladas. |
| `adb install -r` 1.3.0/4 → 1.4.0/5 | **Éxito** en emulador; `versionCode=5`, `versionName=1.4.0`, `firstInstallTime` conservado. |
| `.\gradlew.bat :app:lintDebug --console=plain` | **Éxito**; reporte en `Movil/app/build/reports/lint-results-debug.html`. |

## Actualización 3-nov-2026

- **Dashboards aprendiz e instructor:** se incorporaron los KPIs, la dona de
  estados, las barras por ficha/similitud, la serie temporal con selector de
  periodo y la actividad reciente que ya mostraba la web. El dashboard admin no
  se rediseñó.
- **Manual Android:** ruta compartida y accesos desde los 3 perfiles y el login.
  Incluye índice, búsqueda, contenido por rol, FAQ y solución de problemas.
- **Actualizaciones:** fecha de publicación visible, notificación local por
  versión, permiso Android 13+, acción para abrir el centro y marcador
  anti-repetición. Sin push ni cambios al backend.
- **Verificación:** 207 pruebas unitarias (33 suites, 0 fallos), `lintDebug` y
  `assembleDebug` correctos; login y dashboards de los 3 roles en emulador;
  actualización simulada con debug `1.4.0/4` sobre publicado `1.4.0/5`.
- **Release:** `assembleRelease` correcto con la keystore oficial; APK `1.4.1/6`
  en el repositorio. El despliegue queda a cargo del owner en Railway Dashboard.

## Alcance respetado

La fase inicial de paridad de Oscar no modificó `frontend/` ni `backend/`.
La fase posterior de autenticación exclusiva por correo sí ajustó el login del
backend, la SPA, Android, las pruebas y el manual, conservando activación por
correo, permisos y migraciones no destructivas. No se usaron cuentas reales,
datos de producción, credenciales ni endpoints inventados.

## Verificación pendiente en entorno aislado

Cuando exista backend/base aislados, AVD y cuentas de prueba autorizadas:

1. Login exclusivamente por correo; rechazo de username, cuenta suspendida y cuenta no activada.
2. Activación con enlace válido, vencido, reutilizado y con contraseña corta.
3. Alta de aprendiz e instructor; reenvío, restablecimiento y borrado con y sin
   historial (desactivar vs. eliminación definitiva).
4. Envío de propuesta y recorrido análisis → resultado → detalle de similitud.
5. Recorridos por rol aprendiz, instructor y admin, incluyendo 403/401.
6. Pruebas instrumentadas (`connectedDebugAndroidTest`) si el AVD está aprobado.

## Bloqueos reales

1. **Prueba funcional por rol: resuelta el 3-nov-2026** en emulador contra el
   backend desplegado, con cuentas de prueba autorizadas. Quedan sin recorrer
   los flujos que crearían o modificarían datos (activación, alta de propuesta,
   revisión, borrado y configuración del motor).
2. **Activación automática desde el correo**: App Links/asset links y cambios de
   infraestructura no autorizados.
3. **APK: producción verificada** el 3-nov-2026 (`1.4.0`/`versionCode 5`, URL
   HTTPS, SHA-256 y tamaño coincidentes). No se reemplazó el APK en esta sesión.
4. **Cobertura de dispositivo**: login y dashboards verificados en emulador; no
   se probó una actualización release firmada porque la keystore no está en este
   equipo. La simulación con debug `versionCode 4` confirmó aviso, permiso,
   notificación y navegación al centro.

## Nota de entorno

En la sesión del 3-nov-2026 no se creó `Movil/local.properties`: se usó
`ANDROID_HOME` temporal y `-Pproyectwin.apiBaseUrl.debug` para apuntar al
backend desplegado. El JDK 17 se tomó de `~/.gradle/jdks`; no se versionó ninguna
ruta de máquina. `Movil/local.properties` sigue ignorado por Git.

## Archivos principales modificados

- `Movil/app/src/main/java/com/example/proyectwin/data/api/ApiResult.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/dto/AuthDtos.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/dto/UserDtos.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/dto/FichaDtos.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/service/AuthApi.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/service/UsersApi.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/repository/AuthRepositoryImpl.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/repository/UsersRepositoryImpl.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/repository/FichasRepositoryImpl.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/mapper/DtoMappers.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/model/GeneralUser.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/model/Ficha.kt`
- `Movil/app/src/main/java/com/example/proyectwin/domain/util/PasswordPolicy.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/auth/ActivateAccountScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/auth/LoginScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/auth/ResetPasswordScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/aprendiz/AnalizandoProyectoScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/aprendiz/ResultadoAnalisisScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/aprendiz/DetalleInstructorScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/admin/NewUserScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/admin/UserDetailScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/admin/UserManagementScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/navigation/AppNavigation.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/viewmodel/AuthViewModel.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/viewmodel/UserDetailViewModel.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/viewmodel/ResultadoAnalisisViewModel.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/viewmodel/DetalleInstructorViewModel.kt`

### Archivos de la actualización 3-nov-2026

- `Movil/app/src/main/AndroidManifest.xml`
- `Movil/app/src/main/java/com/example/proyectwin/MainActivity.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ProyecTwinApp.kt`
- `Movil/app/src/main/java/com/example/proyectwin/notifications/UpdateNotificationManager.kt`
- `Movil/app/src/main/java/com/example/proyectwin/domain/util/UpdateNotificationPolicy.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/local/UpdatePreferences.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/api/dto/DashboardDtos.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/model/DashboardMetrics.kt`
- `Movil/app/src/main/java/com/example/proyectwin/data/mapper/DtoMappers.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/components/DashboardCharts.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/viewmodel/DashboardViewModel.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/aprendiz/DashboardScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/instructor/InstructorDashboardScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/screens/ManualScreen.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ui/manual/ManualContent.kt`
- `Movil/app/src/main/res/drawable-nodpi/manual_*.jpg`
- `Movil/app/src/test/java/com/example/proyectwin/domain/util/UpdateNotificationPolicyTest.kt`
- `Movil/app/src/test/java/com/example/proyectwin/data/mapper/DtoSerializationTest.kt`
