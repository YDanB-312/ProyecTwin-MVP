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

La verificación funcional por rol está pendiente: este checkout no tiene un
backend/base aislados, AVD ni cuentas de prueba autorizadas. Por eso los estados
"Completo" significan contrato + compilación + pruebas unitarias, no una prueba
manual de extremo a extremo.

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
| Publicación/descarga APK | `GET /v1/app-version/android`, `/apk/ProyecTWin.apk`, Home | **Completo (código)** | APK 1.4.0/5 firmado y validado; pendiente desplegar a Railway con autorización. |

## Contratos corregidos

- `POST /v1/auth/login`: `{ correo, password, recordarme? }`. El login por `username` queda rechazado y el DTO Android ya no lo envía.
- `POST /v1/auth/activation/validate` y `POST /v1/auth/activation`.
- `POST /v1/general-users` → `{usuario, activacion}`.
- `POST /v1/general-users/{id}/activacion/reenviar`.
- `POST /v1/general-users/{id}/credenciales/restablecer` → mensaje, sin credenciales temporales.
- `DELETE /v1/general-users/{id}` con `confirmar_eliminacion` y `confirmacion_hash`; parseo de `409`, `impacto` y `puede_desactivar`.
- `POST /v1/class-groups/{id}/apprentices` → `activacion`, sin `credenciales`.

## Pruebas ejecutadas

| Comando | Resultado |
|---|---|
| `.\gradlew.bat :app:compileDebugKotlin --console=plain` | **Éxito** (solo warnings de deprecación de APIs de Compose/iconos). |
| `.\gradlew.bat :app:testDebugUnitTest --console=plain` | **Éxito**: 203 pruebas, 0 fallos, 0 errores. Incluye política de contraseña, activación, contratos, versionCode, SHA-256 y rutas de análisis. |
| `.\gradlew.bat :app:compileDebugAndroidTestKotlin --console=plain` | **Éxito**; pruebas instrumentadas compiladas. |
| `adb install -r` 1.3.0/4 → 1.4.0/5 | **Éxito** en emulador; `versionCode=5`, `versionName=1.4.0`, `firstInstallTime` conservado. |
| `.\gradlew.bat :app:lintDebug --console=plain` | **Éxito**; reporte en `Movil/app/build/reports/lint-results-debug.html`. |

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

1. **Prueba funcional por rol**: falta backend/base aislados, AVD y cuentas de
   prueba autorizadas. No se usaron cuentas reales ni producción.
2. **Activación automática desde el correo**: App Links/asset links y cambios de
   infraestructura no autorizados.
3. **APK**: el 1.4.0/5 se generó, firmó y validó localmente; no se desplegó a
   Railway ni se publicó en producción.
4. **Cobertura de dispositivo**: se probó la instalación/actualización con
   `adb install -r` en emulador; no se recorrió login real ni sesión por falta
   de cuentas de prueba autorizadas.

## Nota de entorno

Para compilar se creó `Movil/local.properties` con `sdk.dir` apuntando al SDK
Android local. El archivo está ignorado por Git y no forma parte del cambio.

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
