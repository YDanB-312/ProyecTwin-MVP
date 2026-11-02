# Actualizaciones Android — implementación y verificación

Fecha: 2026-11-02
Rama: `master`

## Artefacto

- `applicationId`: `com.example.proyectwin`
- Versión publicada: `1.4.0`
- `versionCode`: `5`
- `minSdk`: 26
- `targetSdk`: 36
- APK: `backend/public/apk/ProyecTWin.apk`
- Tamaño: `14.920.772` bytes
- SHA-256 del APK:
  `F462FD283B14D9C7308095131318DB363D9F15F0A667DFB5C65982D183B7A386`
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

## Pruebas ejecutadas

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

## Pendiente de despliegue

No se hizo commit, push ni despliegue. Para activar el flujo en producción hay
que autorizar y ejecutar el despliegue, actualizando antes las variables de
Railway:

```text
ANDROID_APP_VERSION=1.4.0
ANDROID_APP_VERSION_CODE=5
ANDROID_APP_MIN_VERSION=1.3.0
ANDROID_APP_MIN_VERSION_CODE=4
ANDROID_APP_DOWNLOAD_URL=https://proyectwin-mvp-production.up.railway.app/apk/ProyecTWin.apk
ANDROID_APP_APK_SHA256=F462FD283B14D9C7308095131318DB363D9F15F0A667DFB5C65982D183B7A386
ANDROID_APP_APK_SIZE=14920772
ANDROID_APP_PUBLISHED_AT=2026-11-02T00:00:00Z
ANDROID_APP_ENFORCE_MINIMUM=false
ANDROID_APP_RELEASE_NOTES=<notas breves de la versión>
```

Después del despliegue verificar:

1. `/v1/app-version/android` devuelve `download_url` HTTPS.
2. `/apk/ProyecTWin.apk` devuelve 200 y el APK coincide con el hash.
3. La Home muestra el botón.
4. Un dispositivo con 1.3.0/4 recibe el aviso, descarga y abre el instalador.
5. La sesión y los datos locales se conservan tras la actualización.

## Bloqueos/limitaciones

- No hay Docker local, por lo que no se validó la imagen Nginx final; sí se
  validó el archivo estático y la configuración.
- No hay MySQL local, por lo que PHPUnit del backend no pudo ejecutarse.
- No se probó un login real ni la conservación de una sesión real en el
  emulador por falta de cuentas de prueba autorizadas.
- La detección E2E contra producción queda pendiente hasta desplegar con
  autorización.
