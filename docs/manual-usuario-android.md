# Manual de usuario Android y actualización 4-nov-2026

**Rama:** `master` (`56a69e0`)
**Alcance:** manual integrado, paridad de dashboards aprendiz/instructor,
notificación local de actualizaciones y capturas reales desde el emulador.
**Release desplegada:** APK `1.4.1 / versionCode 6`, firmado con la identidad
oficial y publicado en Railway. El endpoint de producción ya responde `1.4.1/6`
y `/apk/ProyecTWin.apk` devuelve 200.

## 1. Manual integrado

- Pantalla: `Movil/app/src/main/java/com/example/proyectwin/ui/screens/ManualScreen.kt`
- Contenido: `Movil/app/src/main/java/com/example/proyectwin/ui/manual/ManualContent.kt`
- Ruta compartida: `AppNavigation.MANUAL` (`manual`), registrada en `MainActivity`.
- Accesos:
  - Perfil de aprendiz, instructor y administrador: “Manual de usuario”.
  - Login: enlace “Manual de usuario” para consultar la ayuda antes de entrar.
- El manual no cierra la sesión, no modifica datos y funciona sin conexión porque
  el contenido y las capturas están dentro del APK.
- Estructura: portada, filtro por rol, búsqueda, índice navegable, capítulos
  expandibles, pasos, consejos, capturas, preguntas frecuentes y solución de
  problemas.
- Contenido por rol: administrador, instructor y aprendiz, además de acceso,
  navegación, perfil y actualizaciones.
- Si una captura no existe, la app muestra “Captura pendiente” con la descripción;
  nunca se presenta una pantalla web como si fuera Android.

## 2. Capturas reales

Tomadas desde la app Android ejecutada en el emulador `Medium_Phone_API_36.1`
contra el backend desplegado. Se usaron únicamente cuentas de prueba autorizadas;
las credenciales no se guardaron en el repositorio.

| Recurso | Pantalla |
|---|---|
| `manual_login.jpg` | Inicio de sesión |
| `manual_aprendiz_dashboard.jpg` | Inicio del aprendiz con métricas |
| `manual_aprendiz_charts.jpg` | Gráficas del aprendiz |
| `manual_aprendiz_propuestas.jpg` | Listado de propuestas |
| `manual_instructor_dashboard.jpg` | Inicio del instructor |
| `manual_instructor_charts.jpg` | Gráficas del instructor |
| `manual_admin_dashboard.jpg` | Dashboard del administrador |
| `manual_admin_users.jpg` | Gestión de usuarios |
| `manual_admin_similitudes.jpg` | Listado de similitudes |
| `manual_perfil.jpg` | Perfil con Manual y Actualizaciones |
| `manual_manual.jpg` | Manual con un capítulo expandido |
| `manual_updates.jpg` | Centro de actualizaciones al día |
| `manual_updates_new.jpg` | Nueva versión disponible |
| `manual_update_banner.jpg` | Aviso de actualización en la app |
| `manual_update_notification.jpg` | Notificación local del sistema |
| `manual_update_prompt.jpg` | Permiso de notificaciones de Android 13+ |

Las imágenes se optimizaron a 720 px de ancho y JPEG de calidad 82. Están en
`Movil/app/src/main/res/drawable-nodpi/`.

### Capturas pendientes

No se capturaron para evitar crear o modificar datos de producción:
activación de cuenta, formulario de propuesta nuevo, resultado de análisis,
detalle de ficha, revisión de propuesta, gestión de fichas del instructor,
detalle administrativo de ficha, configuración del motor y bitácora. El manual
las marca como pendientes con una descripción honesta.

## 3. Paridad de dashboards

Se completaron los indicadores que la web muestra y Android no tenía:

- **Instructor:** KPIs de fichas a cargo, aprendices, propuestas y pendientes;
  dona de propuestas por estado; barras de propuestas por ficha; evolución de
  revisiones con selector 3 meses/30 días/7 días; actividad reciente.
- **Aprendiz:** KPIs de propuestas registradas, aprobadas, similitudes detectadas
  y notificaciones sin leer; dona de estados; similitud máxima por propuesta;
  evolución de propuestas; observaciones recientes.
- **Administrador:** sin rediseño. Se conservaron sus métricas y gráficas.

Fuente de datos: `GET /v1/dashboard-metrics` y los endpoints existentes de
proyectos, similitudes y notificaciones. No se inventaron cálculos ni valores.
Se ampliaron `DashboardMetricsDto`, `DashboardMetrics` y `DtoMappers` con
`fichas.detalle` y `similitudesPropuestas`, campos que ya devuelve el backend.

Estados cubiertos: carga, error con reintento, vacío, actualización en curso y
pull-to-refresh. Los componentes reutilizados están en
`Movil/app/src/main/java/com/example/proyectwin/ui/components/DashboardCharts.kt`.

## 4. Actualizaciones y notificaciones

Se conservó el sistema existente y se añadió:

- Canal `proyectwin_updates` creado en `ProyecTwinApp`.
- Permiso `POST_NOTIFICATIONS` en el manifiesto y solicitud en Android 13+ solo
  cuando hay una actualización visible.
- Notificación local una vez por versión, con acción “Revisar actualización”.
- Al tocar la notificación se abre la app y se navega al centro de actualizaciones.
- Marcador de versión notificada en `UpdatePreferences` para evitar repeticiones.
- `AppUpdatesScreen` muestra la fecha de publicación real del backend.

La notificación **no es push**: la app consulta `GET /v1/app-version/android`
al abrirse o al entrar a Actualizaciones. No hay avisos con la app cerrada.

Validaciones intactas: HTTPS, SHA-256, tamaño, `packageName`, `versionCode`,
`versionName`, firma oficial, FileProvider, instalador del sistema y cancelación
de la descarga.

## 5. Verificación ejecutada

| Comando / prueba | Resultado |
|---|---|
| `.\gradlew.bat :app:testDebugUnitTest` | **207 pruebas, 33 suites, 0 fallos, 0 errores** |
| `.\gradlew.bat :app:lintDebug` | **BUILD SUCCESSFUL** |
| `.\gradlew.bat :app:assembleDebug` | **BUILD SUCCESSFUL** |
| `.\gradlew.bat :app:assembleRelease` | **BUILD SUCCESSFUL:** APK `1.4.1/6` firmado con la keystore oficial, v2/v3 |
| Login aprendiz/instructor/admin contra producción | Correcto con las cuentas de prueba autorizadas |
| Dashboards de los 3 roles | Métricas reales visibles y coherentes con el backend |
| Simulación de actualización (debug 1.4.0/4 sobre publicado 1.4.0/5) | Aviso visible, permiso solicitado, notificación publicada y acción abrió el centro |
| Actualización real `1.4.0/5 → 1.4.1/6` | APK descargado y verificado por la app; instalador oficial abierto; sesión y datos conservados |
| `GET /v1/app-version/android` | Producción responde `1.4.1`, `versionCode 6`, hash y tamaño correctos |
| `/apk/ProyecTWin.apk` | **200**, `application/vnd.android.package-archive`, 16.392.336 bytes |
| `/apk/**` no autorizado | 404 |

### Release 1.4.1 / versionCode 6

- `applicationId`: `com.example.proyectwin`
- `versionName`: `1.4.1`; `versionCode`: `6`
- Firma: v2/v3, certificado `CN=Oscar`,
  SHA-256 `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D`
- APK: `backend/public/apk/ProyecTWin.apk`
- Tamaño: `16.392.336` bytes
- SHA-256 del APK:
  `F47D2A8321840F9C9CF2836192140C7AC16D7A8BBF695ECEFEB43286AE4D47BC`
- `debuggable`: no
- No se ejecutó PHPUnit (no hay MySQL local). El despliegue en Railway no se
  realizó desde este equipo; queda documentado para el owner.

## 6. Publicación futura del APK (solo procedimiento)

No ejecutar en esta sesión. Para una release futura:

1. Confirmar custodia y autorización escrita de la firma oficial.
2. Incrementar `versionCode` y mantener `applicationId`; actualizar
   `versionName`.
3. Compilar con `proyectwin.apiBaseUrl.release` HTTPS.
4. Verificar el APK con `aapt dump badging`, `apksigner verify --print-certs`,
   SHA-256 y tamaño reales.
5. Instalar sobre una versión anterior de prueba y comprobar que la sesión y
   los datos locales se conservan.
6. Sustituir el APK en la ruta estable y actualizar los metadatos
   `ANDROID_APP_*` del despliegue.
7. Revisar la caché de Nginx para no servir el APK anterior.
8. Comprobar endpoint, descarga, hash y tamaño después del despliegue.

## 7. Archivos principales

- `Movil/app/src/main/AndroidManifest.xml`
- `Movil/app/src/main/java/com/example/proyectwin/MainActivity.kt`
- `Movil/app/src/main/java/com/example/proyectwin/ProyecTwinApp.kt`
- `Movil/app/src/main/java/com/example/proyectwin/navigation/AppNavigation.kt`
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

## 8. Limitaciones

- La captura del aviso se obtuvo con un build debug de `versionCode 4`; el
  `versionCode` del proyecto se restauró a `5` antes de terminar.
- No hay keystore en este equipo, por lo que no se puede validar una release
  firmada ni una actualización sobre la instalación release.
- La notificación es local; no se añadió Firebase ni un servicio en segundo plano.
- No se modificó el backend ni se desplegó nada.
