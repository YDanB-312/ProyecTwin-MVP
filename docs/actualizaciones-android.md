# Actualizaciones Android — implementación y verificación

Fecha: 2026-11-02 · **Actualizado: 2026-11-04**
Rama: `master`

> **Estado 4-nov-2026:** el APK `1.4.1 / versionCode 6` fue desplegado en
> Railway. El endpoint de producción responde `1.4.1/6`, la descarga directa
> responde 200 y la actualización desde `1.4.0/5` fue verificada en emulador
> conservando sesión y datos.

## Artefacto

- `applicationId`: `com.example.proyectwin`
- Versión preparada: `1.4.1`
- `versionCode`: `6`
- `minSdk`: 26
- `targetSdk`: 36
- APK: `backend/public/apk/ProyecTWin.apk`
- Tamaño: `16.392.336` bytes
- SHA-256 del APK:
  `F47D2A8321840F9C9CF2836192140C7AC16D7A8BBF695ECEFEB43286AE4D47BC`
- Certificado: `CN=Oscar`, RSA 2048, firma v2 y v3
- SHA-256 del certificado:
  `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D`

La keystore original está fuera del repositorio y no se copió ni se versionó.
Gradle la lee desde `%USERPROFILE%\.proyecttwin\keystore.properties` o variables
de entorno `PROYECTTWIN_*`. Las contraseñas no se documentan.

## Backend

`GET /v1/app-version/android` publica:

- `latest_version`
- `latest_version_code`
- `minimum_supported_version`
- `minimum_supported_version_code`
- `enforce_minimum`
- `download_url` HTTPS
- `apk_sha256`
- `apk_size`
- `published_at`
- `release_notes`

Nginx sirve únicamente `/apk/ProyecTWin.apk`; el resto de `/apk/**` responde
404. `.dockerignore` y `.railwayignore` excluyen todos los APK salvo el
autorizado. Si el hash o el tamaño no se configuran por entorno, el controlador
los calcula del archivo.

## Android

- La comparación principal es por `versionCode`.
- `versionName` se conserva como dato visible y respaldo de clientes antiguos.
- El aviso se ejecuta una vez por arranque; “Más tarde” silencia la misma
  versión durante 24 h.
- El centro de actualizaciones descarga con un cliente HTTP sin interceptor de
  autenticación, por lo que el token de sesión nunca viaja a la URL del APK.
- Se valida paquete, `versionCode`, firma, SHA-256 y tamaño.
- Se puede cancelar una descarga en curso.
- Se usa `FileProvider` y el instalador oficial de Android; no hay instalación
  silenciosa.
- No se borran datos de la app antes de la confirmación del instalador.

## Home web

La Home consulta `appVersion.android()` y muestra
**“Descargar aplicación Android”** solo si la respuesta contiene una URL HTTPS.
Si no hay versión publicada, muestra un aviso informativo en lugar de un enlace
roto. El botón es un enlace accesible y descarga el APK desde la URL estable.

## Pruebas ejecutadas (histórico 2-nov-2026)

> Las cifras de esta tabla corresponden a la sesión del 2-nov-2026. El estado
> vigente del 3-nov-2026 está en la sección “Verificación Android 3-nov-2026”.

| Prueba | Resultado |
|---|---|
| `aapt dump badging` APK 1.4.0/5 | `versionCode=5`, `versionName=1.4.0`, sin `application-debuggable` |
| `apksigner verify --verbose --print-certs` | Verifica v2/v3; certificado `CN=Oscar` y huella esperada |
| `sha256` del APK | Coincide con el hash publicado |
| Instalación 1.3.0/4 en emulador | `Success` |
| Actualización 1.4.0/5 sobre 1.3.0/4 con `adb install -r` | `Success`; `versionCode=5`, `versionName=1.4.0`; `firstInstallTime` conservado |
| Apertura de la app 1.4.0/5 | Proceso iniciado, sin `FATAL EXCEPTION` en Logcat |
| Servidor estático PHP `/apk/ProyecTWin.apk` | `200`, `application/vnd.android.package-archive` |
| `GET /v1/app-version/android` vía Tinker | Contrato completo con URL HTTPS, hash y tamaño |
| `php -l` backend modificado | Sin errores de sintaxis |
| Vitest frontend | **112 tests passed** (incluye 2 pruebas específicas del botón de descarga en Home) |
| ESLint frontend | **Sin errores** |
| `npm run build` frontend | **Build correcto** |
| `AssembleRelease` Android | APK firmado generado |
| Unit tests Android | **203 tests, 0 fallos, 0 errores** |
| `lintDebug` Android | **BUILD SUCCESSFUL** |
| PHPUnit `AppVersionTest` | **Bloqueado por entorno**: no hay MySQL local; la guarda exige una base desechable `ptwin_test_*` |

## Publicación desplegada (4-nov-2026)

El APK release `1.4.1/6` está en el repositorio y fue desplegado en Railway.
Las variables del servicio quedaron con estos valores:

```text
ANDROID_APP_VERSION=1.4.1
ANDROID_APP_VERSION_CODE=6
ANDROID_APP_MIN_VERSION=1.3.0
ANDROID_APP_MIN_VERSION_CODE=4
ANDROID_APP_DOWNLOAD_URL=https://proyectwin-mvp-production.up.railway.app/apk/ProyecTWin.apk
ANDROID_APP_APK_SHA256=F47D2A8321840F9C9CF2836192140C7AC16D7A8BBF695ECEFEB43286AE4D47BC
ANDROID_APP_APK_SIZE=16392336
ANDROID_APP_PUBLISHED_AT=2026-11-04T06:34:14Z
ANDROID_APP_ENFORCE_MINIMUM=false
ANDROID_APP_RELEASE_NOTES=Actualización 1.4.1: manual de usuario integrado, nuevas métricas en los paneles de aprendiz e instructor y notificaciones locales de actualización.
```

Después del despliegue verificar:

1. `/v1/app-version/android` devuelve `download_url` HTTPS.
2. `/apk/ProyecTWin.apk` devuelve 200 y el APK coincide con el hash.
3. La Home muestra el botón.
4. Un dispositivo con 1.3.0/4 recibe el aviso, descarga y abre el instalador.
5. La sesión y los datos locales se conservan tras la actualización.

## Verificación Android 4-nov-2026

| Prueba | Resultado |
|---|---|
| `testDebugUnitTest` | **207 pruebas, 33 suites, 0 fallos, 0 errores** |
| `lintDebug` | **BUILD SUCCESSFUL** |
| `assembleDebug` | **BUILD SUCCESSFUL** |
| `assembleRelease` | **BUILD SUCCESSFUL:** APK `1.4.1/6`, v2/v3, certificado `CN=Oscar` |
| Login y dashboards aprendiz/instructor/admin | Verificados en emulador contra producción |
| Actualización simulada debug 1.4.0/4 sobre publicado 1.4.0/5 | Aviso visible, permiso de Android 13 solicitado, notificación publicada y acción abierta al centro |
| Reinstalación debug | Sesión local conservada |
| Fecha de publicación | Visible en el centro de actualizaciones |
| APK release `1.4.1/6` | SHA-256 `F47D...47BC`, 16.392.336 bytes, firma oficial |
| Actualización real `1.4.0/5 → 1.4.1/6` | APK descargado y verificado; instalador abierto; sesión y datos conservados |
| Endpoint de producción | `1.4.1`, `versionCode 6`, hash/tamaño nuevos |
| Descarga `/apk/ProyecTWin.apk` | 200, MIME correcto, `Cache-Control: no-cache`; rutas no autorizadas 404 |

La notificación es **local**, no push: se emite cuando la app consulta el
endpoint al abrirse o al entrar a Actualizaciones. Se añadió el canal
`proyectwin_updates`, el permiso `POST_NOTIFICATIONS`, un marcador por versión
para no repetirla y una acción que abre el centro de actualizaciones.

## Bloqueos/limitaciones

- No hay Docker local, por lo que no se validó la imagen Nginx final; sí se
  validó el archivo estático y la configuración.
- No hay MySQL local, por lo que PHPUnit del backend no pudo ejecutarse.
- No se probó un login real ni la conservación de una sesión real en el
  emulador por falta de cuentas de prueba autorizadas.
- La detección E2E contra producción queda pendiente hasta desplegar con
  autorización.
