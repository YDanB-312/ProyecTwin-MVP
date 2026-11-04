# Release Android firmado — ProyecTwin MVP

> **Estado actual del APK.** El artefacto `backend/public/apk/ProyecTWin.apk`
> es **1.4.1 / versionCode 6**, `applicationId com.example.proyectwin`, sin
> `application-debuggable`, firmas v2/v3 y certificado `CN=Oscar` (RSA 2048).
> SHA-256 del APK:
> `F47D2A8321840F9C9CF2836192140C7AC16D7A8BBF695ECEFEB43286AE4D47BC`.
> SHA-256 del certificado:
> `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D`.
>
> **Publicación preparada.** El APK se sirve únicamente en
> `/apk/ProyecTWin.apk`; el resto de `/apk/**` responde 404. La Home muestra
> la descarga solo cuando `/v1/app-version/android` devuelve una URL HTTPS y
> la app Android valida `versionCode`, tamaño, SHA-256, paquete y firma. No
> generes una clave nueva ni cambies el `applicationId`: romperías la
> actualización de las instalaciones existentes.
>
> Este documento es el procedimiento para validar y publicar futuras versiones.
> Todos los valores entre `<...>` son marcadores: **ningún secreto debe
> escribirse en este archivo ni en el repo**.

## 0. Decisiones que debe tomar el owner

Estas decisiones son bloqueantes antes de la primera release oficial. Las
propuestas son recomendaciones técnicas, no decisiones ya tomadas.

| Decisión | Propuesta | Consecuencia |
|---|---|---|
| Custodio de la clave | El owner del proyecto (con un suplente designado) | Nadie más genera, copia ni usa la clave; la custodia se documenta |
| Alias de la clave | `app_ProyecTWin` | Coincide con el alias de la keystore original `C:\Users\Epsilon\ProyecTWin.jks` (fuera del repo) |
| `applicationId` | Mantener `com.example.proyectwin` en la primera release firmada | El release no se instala sobre instalaciones debug 1.2.0/3 (firma distinta); hay que desinstalar. Si se cambia, es una app nueva |
| Minify / R8 | `isMinifyEnabled = false` en la primera release firmada | Aísla el cambio de firma; R8 queda como optimización posterior con pruebas completas |
| Versión de la release actual | `versionName = "1.4.1"`, `versionCode = 6` | Versión nueva firmada con la misma clave; se instala sobre 1.4.0/5 |
| Autorización de publicación | Autorizada expresamente por el owner para esta iteración | El APK se sirve solo en `/apk/ProyecTWin.apk` |
| Canal de distribución | APK directo servido por Railway en `/apk/ProyecTWin.apk` | No hay Play Store ni App Bundle en este plan |
| APK en Git | El artefacto autorizado permanece tracked para que el Dockerfile lo incluya | El repo crece ~15 MB por release; valorar artefactos externos |
| Aviso obligatorio | `ANDROID_APP_ENFORCE_MINIMUM=false` en la release de transición | Los usuarios con debug 1.2.0/3 deben desinstalar antes de instalar el release |

**Criterio de aceptación de la release:** APK firmado con la clave del owner (no
debug), sin `application-debuggable`, con firma v2/v3, `versionCode` 5
para esta release, URL de
API HTTPS, SHA-256 registrado y endpoint `/v1/app-version/android` coherente con
el APK publicado.

## 1. Requisitos de la clave de firma

### 1.1 Custodia y autorización

- La clave la genera y custodia **solo el owner autorizado**. No la genera un
  subagente, un compañero, un servicio externo ni una IA.
- La clave de firma es la identidad permanente de la app. Si se pierde, no se
  pueden publicar actualizaciones para las instalaciones existentes. Si se
  filtra, un tercero podría firmar APK que Android aceptaría como actualización.
- No existe revocación para APK distribuidos directamente: la recuperación ante
  pérdida o filtración es restaurar el backup o publicar una app con otro
  `applicationId` (nueva identidad, sin actualización directa).
- Designa un suplente y documenta la transferencia de custodia (quién, cuándo,
  dónde están el keystore y las contraseñas). El suplente no debe tener acceso
  permanente; solo acceso de emergencia.
- Si en el futuro se usa Google Play, la clave de subida (upload key) es distinta
  de la clave de firma de la app (Play App Signing). Este plan es para APK
  directo, no para Play.

### 1.2 Datos de la clave

| Dato | Valor actual | Nota |
|---|---|---|
| Formato | PKCS#12 (`.jks`) | Android lo acepta |
| Alias | `app_ProyecTWin` | No usar `androiddebugkey` |
| Algoritmo | RSA 2048 bits | Firma v2/v3 verificada |
| DN del certificado | `C=co, ST=Cauca, L=Popayan, CN=Oscar` | Identidad existente de la app |
| Ubicación | `C:\Users\Epsilon\ProyecTWin.jks` | **Fuera** del repositorio |
| Contraseñas | Custodia del owner | Nunca en el repo, logs ni BuildConfig |

La huella SHA-256 del certificado (no la del APK) es pública y se registra en el
expediente de release. Se obtiene con `keytool -list -v` o con
`apksigner verify --print-certs`.

> El APK 1.4.0/5 usa la identidad existente `CN=Oscar`. No se debe generar una
> clave nueva para la misma app: rompería las actualizaciones instaladas.

### 1.3 Generación (solo el owner, fuera del repo)

> **No ejecutes esta sección.** La identidad de la app ya está fijada por
> `CN=Oscar`; generar otra clave rompería las actualizaciones de las
> instalaciones existentes.

El comando siguiente se documenta para que el owner lo ejecute en su equipo. No
lo ejecutes en un agente ni en CI. `keytool` pide las contraseñas de forma
interactiva; **no** las pases con `-storepass`/`-keypass` en la línea de comandos
(quedan en el historial y en la lista de procesos).

```powershell
$keytool = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
$dir = "$env:USERPROFILE\.proyecttwin"
New-Item -ItemType Directory -Force $dir | Out-Null

& $keytool -genkeypair -v `
  -keystore "$dir\proyecttwin-release.jks" `
  -alias proyecttwin-release `
  -keyalg RSA -keysize 4096 -validity 10000 `
  -storetype PKCS12
```

Si `keytool` no está en esa ruta, usa el JBR del Android Studio instalado o
`where.exe keytool`. Al terminar, verifica la entrada y anota la huella:

```powershell
& $keytool -list -v -keystore "$dir\proyecttwin-release.jks" -alias proyecttwin-release
```

Debe aparecer `Entry type: PrivateKeyEntry` y una línea `SHA256:` con la huella
del certificado. Guárdala en el expediente (no es secreta); las contraseñas no se
guardan ahí.

### 1.4 Backup y recuperación

- Haz **dos copias** del keystore en medios offline distintos (por ejemplo, dos
  unidades USB cifradas guardadas en ubicaciones físicas separadas) y una copia
  cifrada en la nube solo si la clave de cifrado se custodia por separado.
- No guardes el keystore y sus contraseñas juntos en el mismo backup: si ese
  soporte se pierde o se roba, el atacante tiene la identidad completa.
- Registra el SHA-256 del archivo del keystore para detectar copias corruptas.
- Prueba la restauración **sin firmar nada**: restaura una copia en una carpeta
  temporal y ejecuta `keytool -list -v` sobre ella. No generes una clave nueva
  para "probar".
- Revisa la custodia si el owner cambia de equipo o deja el proyecto.

### 1.5 Prohibiciones explícitas

- **No usar la clave debug** (`%USERPROFILE%\.android\debug.keystore`, alias
  `androiddebugkey`, contraseña pública `android`). El APK 1.3.0/4 **no** usa esa
  clave: está firmado con `CN=Oscar` (RSA 2048). `CN=Android Debug` solo aplica
  a builds debug anteriores (1.2.0/3 y previos).
- No reutilizar la clave de otro proyecto, de otro equipo o de una release
  anterior que no sea de ProyecTwin.
- No commitear el keystore, `keystore.properties`, contraseñas, `mapping.txt` de
  R8 ni logs que contengan credenciales.
- No compartir la clave por chat, correo, ticket, captura de pantalla o variable
  de entorno de un servicio no autorizado.
- No firmar un APK `debuggable` aunque se use la clave release: sería un build de
  pruebas con identidad de producción.

## 2. Configuración de firma en Gradle

Los cambios de esta sección ya están aplicados en `Movil/app/build.gradle.kts`.
La keystore permanece fuera del repositorio y se lee desde
`%USERPROFILE%\.proyecttwin\keystore.properties` o variables de entorno.

> **Estado actual:** `signingConfigs.release` usa la clave original
> `C:\Users\Epsilon\ProyecTWin.jks`, alias `app_ProyecTWin` y firma v2/v3.
> `assembleRelease` produce un APK firmado 1.4.0/5. Nunca generes otra clave ni
> publiques el antiguo APK sin firma.

### 2.1 `.gitignore`

Añadir al `.gitignore` de `Movil/` (y al de la raíz si existe) estas entradas,
aunque el keystore se guarde fuera del repo (defensa en profundidad):

```gitignore
# Firma Android: nunca versionar
*.jks
*.keystore
*.p12
keystore.properties
**/keystore.properties
```

Verificación (no debe listar coincidencias):

```powershell
git status --porcelain | Select-String -Pattern '\.jks$|\.keystore$|\.p12$|keystore\.properties'
git status --porcelain | Select-String -Pattern '\.jks$|\.keystore$|\.p12$|keystore\.properties'
```

Si el keystore está fuera del repo, `git check-ignore` no lo verá; la prueba real
es que `git status` no muestre secretos.

### 2.2 `keystore.properties` fuera del repo

Ruta recomendada: `%USERPROFILE%\.proyecttwin\keystore.properties` (fuera de
`MVP/`). Contenido de ejemplo, con marcadores (nunca valores reales):

```properties
# %USERPROFILE%\.proyecttwin\keystore.properties
storeFile=C:/Users/Epsilon/ProyecTWin.jks
storePassword=<contraseña-del-keystore>
keyAlias=app_ProyecTWin
keyPassword=<contraseña-de-la-clave>
```

- Usa barras `/` o barras invertidas escapadas; una ruta sin escapar puede
  romper el archivo `.properties`.
- Restringe los permisos del archivo al usuario del owner (ACL de Windows o
  `chmod 600` en Unix).
- La variable de entorno `PROYECTTWIN_KEYSTORE_PROPERTIES` puede apuntar a otra
  ruta absoluta. En CI es preferible usar directamente las variables
  `PROYECTTWIN_STORE_*` (sección 7), sin archivo de propiedades.
- No pongas estas claves en `gradle.properties`, `local.properties`, `BuildConfig`
  ni en ningún archivo versionado.

### 2.3 `Movil/app/build.gradle.kts`

Añadir la lectura de credenciales **antes** del bloque `android { ... }` y la
configuración de firma dentro de `android { ... }`. El ejemplo mantiene el resto
de la configuración actual (API, `applicationId`, versiones) sin cambios:

```kotlin
import java.util.Properties

// ------------------------------------------------------------------ FIRMA
// Credenciales SIEMPRE fuera del repositorio:
//   1) PROYECTTWIN_KEYSTORE_PROPERTIES apunta a un keystore.properties absoluto.
//   2) Si no existe, se usa ~/.proyecttwin/keystore.properties.
//   3) Cada valor se puede sobrescribir con variables de entorno (CI).
val keystoreProperties = Properties().apply {
    val ruta = System.getenv("PROYECTTWIN_KEYSTORE_PROPERTIES")
        ?: "${System.getProperty("user.home")}/.proyecttwin/keystore.properties"
    val archivo = rootProject.file(ruta)
    if (archivo.isFile) archivo.inputStream().use { load(it) }
}

fun signingValue(env: String, prop: String): String? =
    System.getenv(env)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(prop)?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("PROYECTTWIN_STORE_FILE", "storeFile")
val releaseStorePassword = signingValue("PROYECTTWIN_STORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("PROYECTTWIN_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("PROYECTTWIN_KEY_PASSWORD", "keyPassword")
```

Dentro de `android { ... }`, antes de `buildTypes`:

```kotlin
signingConfigs {
    create("release") {
        // Si falta algún valor, assembleRelease falla al firmar; no produce un
        // APK publicable. assembleDebug no usa esta configuración.
        storeFile = releaseStoreFile?.let { file(it) }
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
    }
}
```

Y en `buildTypes { release { ... } }` (sustituye el bloque `release` existente;
no añadas un segundo bloque con el mismo nombre):

```kotlin
release {
    signingConfig = signingConfigs.getByName("release")
    isDebuggable = false
    isMinifyEnabled = false
    isShrinkResources = false
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
    buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl("release")}\"")
}
```

Notas:

- `storeFile` admite ruta absoluta; el ejemplo la obtiene del `.properties` o de
  `PROYECTTWIN_STORE_FILE`. No la escribas en el repo.
- Si falta la configuración, `assembleRelease` falla en la tarea de firma con un
  error del tipo `Keystore file not set for signing config release`. Si aun así
  aparece `app-release-unsigned.apk`, **no lo publiques**: la validación de la
  sección 4 lo rechaza.
- `assembleDebug` debe seguir funcionando sin keystore; si se rompe, la lectura
  de credenciales se está ejecutando de forma incorrecta.
- `proguard-rules.pro` se mantiene aunque R8 esté desactivado; no agregues reglas
  de secretos ni rutas absolutas.

### 2.4 `applicationId`, minify y R8

**`applicationId`:** la propuesta es mantener `com.example.proyectwin` para la
primera release firmada. Con el mismo ID, Android exige la **misma firma** para
actualizar; al cambiar de debug a release hay que desinstalar (sección 6). Si el
owner decide un ID definitivo, debe hacerlo **antes** de la primera release
oficial: cambiar después crea una app nueva, sin actualización directa y con los
datos locales separados. `namespace` puede seguir siendo
`com.example.proyectwin`; no hace falta renombrar paquetes para cambiar solo
`applicationId`. Un ID público debería ser un DNS inverso controlado por la
institución/equipo (por ejemplo, `co.edu.<institución>.proyecttwin`), no
`com.example.*`, y requiere autorización de uso del dominio/marca.

**Minify/R8:** la propuesta es `isMinifyEnabled = false` y
`isShrinkResources = false` en la primera release firmada. Así el único cambio
frente al APK ya probado es la firma y la URL de producción; `BuildConfig.DEBUG`
ya es `false` y el logging HTTP de `NetworkModule` queda desactivado. Activar R8
es un cambio funcional que exige:

- keep rules para Retrofit, kotlinx.serialization, Hilt/Dagger y reflexión;
- ejecutar la suite de pruebas y un smoke manual del APK release en un
  dispositivo limpio (no solo `assembleDebug`);
- archivar `app/build/outputs/mapping/release/mapping.txt` para desofuscar
  crashes; no publicarlo ni commitearlo si contiene rutas internas.

Si el owner decide activar R8 en esta misma release, no se debe publicar hasta
completar esas pruebas. Para el MVP se recomienda dejarlo para un lote posterior.

## 3. URL de API para release

La URL se compila en `BuildConfig.API_BASE_URL`; no es configurable en tiempo de
ejecución. Debe ser **HTTPS** y terminar en `/v1/` (Retrofit exige la barra
final). Definirla en `Movil/local.properties` (archivo local, no versionado):

```properties
proyectwin.apiBaseUrl.release=https://<dominio>/v1/
```

o pasarla al invocar Gradle:

```powershell
.\gradlew.bat :app:assembleRelease `
  -Pproyectwin.apiBaseUrl.release=https://<dominio>/v1/
```

> **Prioridad:** `local.properties` tiene prioridad sobre `-P`. Si el archivo
> local conserva una URL vieja, `-P` no la sobrescribe; actualiza o elimina la
> línea local antes de compilar.

El release usa `Movil/app/src/main/res/xml/network_security_config.xml`, que
tiene `cleartextTrafficPermitted="false"`. Si se omite la URL de release, el
fallback es `http://10.0.2.2:8000/v1/`; el APK se instala, pero **no podrá
conectar** porque el HTTP está bloqueado en release. No publiques un APK que use
el fallback.

El endpoint `/v1/app-version/android` publica la versión, `versionCode`, URL
HTTPS, SHA-256, tamaño y fecha del APK autorizado.

Verificación funcional: en un dispositivo/emulador limpio, instalar el release y
hacer login contra `https://<dominio>/v1/`. Si la app muestra un error genérico de
red, revisar la URL compilada y el certificado TLS del dominio.

## 4. Compilar y validar el APK

### 4.1 Requisitos

| Requisito | Valor |
|---|---|
| JDK | 17 (JBR de Android Studio o toolchain del proyecto) |
| SDK Android | Platform 36.1, `minSdk` 26, `targetSdk` 36 |
| Build-Tools | 36.1.0 (`aapt`, `apksigner`, `zipalign`) |
| `local.properties` | `sdk.dir` y `proyectwin.apiBaseUrl.release` HTTPS |
| Firma | `keystore.properties` fuera del repo o variables de entorno |
| Backend | Desplegado y accesible por HTTPS en `/v1/` |

La ruta de Build-Tools puede variar; los comandos usan la instalada en este
equipo:

```powershell
$bt = "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.1.0"
$apk = "app\build\outputs\apk\release\app-release.apk"
```

### 4.2 Compilar

Desde `MVP\Movil`:

```powershell
.\gradlew.bat clean :app:assembleRelease
```

Salida esperada: `app\build\outputs\apk\release\app-release.apk` (firmado). Si
falta la keystore, Gradle debe fallar en la tarea de firma; no publiques un
`app-release-unsigned.apk`.

### 4.3 Verificar versión y ausencia de `debuggable` con `aapt`

```powershell
$badging = & "$bt\aapt.exe" dump badging $apk
$badging | Select-String '^package:'
if ($badging -match 'application-debuggable') {
    throw "APK debuggable: no publicar"
}
```

Debe mostrar la versión esperada y **ninguna** línea `application-debuggable`:

```text
package: name='com.example.proyectwin' versionCode='5' versionName='1.4.0' ...
```

Comprobaciones adicionales:

- `versionCode` debe ser exactamente `5` para esta release y crecer en cada publicación.
- `versionName` debe coincidir exactamente con `ANDROID_APP_VERSION`.
- `sdkVersion:'26'` y `targetSdkVersion:'36'` deben ser los esperados.
- Si el manifiesto declara `android:debuggable="true"`, `aapt` lo muestra como
  `application-debuggable`; no lo publiques.

### 4.4 Verificar firma con `apksigner`

```powershell
& "$bt\apksigner.bat" verify --verbose --print-certs $apk
if ($LASTEXITCODE -ne 0) {
    throw "Firma inválida: no publicar"
}
```

Debe indicar que el APK verifica y que usa los esquemas v2 y v3:

```text
Verifies
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Signer #1 certificate DN: CN=ProyecTwin Release, O=..., C=CO
Signer #1 certificate SHA-256 digest: <huella registrada>
```

- Compara la huella SHA-256 del certificado con la anotada al crear la clave. Si
  no coincide, **no publiques**.
- Si el DN es `CN=Android Debug` o la huella es la debug
  (`72:05:D0:E6:...:80:93`), el APK se firmó con la clave equivocada.
- Con `minSdk 26`, v1 no es necesario; v2/v3 son los esquemas que Android usa.
  Si `apksigner` muestra v2 o v3 en `false`, el APK no es válido para distribuir.
- Opcional: `& "$bt\zipalign.exe" -c -v 4 $apk` debe terminar correctamente.
  Gradle ya alinea el APK al firmarlo.

### 4.5 SHA-256 del APK

```powershell
$hash = (Get-FileHash $apk -Algorithm SHA256).Hash
$hash
```

Registra el hash completo (64 caracteres hexadecimales) en el expediente de
release y en las notas del tag. Este es el hash que debe coincidir con el archivo
servido en producción. No confundir con la huella del certificado:

| Hash | Qué identifica |
|---|---|
| SHA-256 del APK | Integridad del archivo publicado |
| SHA-256 del certificado | Identidad del firmante (misma clave en futuras releases) |

### 4.6 Smoke test antes de publicar

- Instalar el APK en un dispositivo/emulador **limpio** (API 26+ y, si es
  posible, API 36).
- Iniciar sesión contra producción, recorrer las pantallas principales y
  comprobar que no hay tráfico HTTP ni errores de red.
- Verificar que no aparece el aviso de depuración ni logs HTTP en Logcat.
- Probar el aviso de actualización contra el endpoint real (sección 5).
- Si el APK viene de un build anterior con otra firma, Android puede rechazar la
  instalación; ver sección 6.

## 5. Publicación en Railway

**Estado actual:** el APK **1.4.1/6** está tracked, se incluye en la imagen y
Nginx lo sirve únicamente en `/apk/ProyecTWin.apk`; cualquier otra ruta
`/apk/**` responde 404. El endpoint `/v1/app-version/android` publica la URL
HTTPS, `versionCode`, SHA-256 y tamaño, y la Home muestra la descarga cuando
recibe una URL válida.

La publicación fue autorizada expresamente por el owner. No reemplaces el APK
sin validar firma, versión, hash y actualización sobre la instalación anterior.

El procedimiento de despliegue general está en
[`deploy-railway.md`](deploy-railway.md); esta sección solo añade el artefacto
Android.

### 5.1 Subir versión

La versión actual es **1.4.1 / versionCode 6**, firmada con la clave original.
Para la próxima release incrementa el `versionCode` (nunca reutilizarlo) y
actualiza `versionName` junto con las variables del backend.

Antes de compilar, en `Movil/app/build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.example.proyectwin" // mantener salvo decisión del owner
    minSdk = 26
    targetSdk = 36
    versionCode = 6        // > 5 (el actual); incrementar en cada publicación
    versionName = "1.4.1"  // debe coincidir con ANDROID_APP_VERSION
}
```

Reglas:

- `versionCode` debe ser estrictamente mayor que cualquier APK ya instalable o
  publicado. Nunca reutilizar un `versionCode`.
- `versionName` es la versión visible. El criterio principal del aviso in-app
  es `versionCode`; `versionName` se conserva para mostrar y como respaldo de
  clientes antiguos.
- Cada publicación debe subir `versionCode`; no publiques dos APK con el mismo
  `versionCode` y distinto contenido.
- Si se cambia `applicationId`, actualizar este documento y las instrucciones de
  transición; no es una actualización de la app existente.

### 5.2 Distribución del APK

`.dockerignore` y `.railwayignore` excluyen cualquier APK salvo
`backend/public/apk/ProyecTWin.apk`; Docker lo copia y Nginx lo sirve solo en
esa ruta exacta. El resto de `/apk/**` responde 404.

Para una release futura, reemplaza únicamente el APK autorizado, actualiza
`ANDROID_APP_VERSION`, `ANDROID_APP_VERSION_CODE`, `ANDROID_APP_APK_SHA256`,
`ANDROID_APP_APK_SIZE` y `ANDROID_APP_PUBLISHED_AT`, y valida firma, MIME,
hash y actualización antes de desplegar. El endpoint calcula hash y tamaño del
archivo si las variables se omiten.

### 5.3 Metadatos de versión

Variables publicadas: `ANDROID_APP_VERSION`, `ANDROID_APP_VERSION_CODE`,
`ANDROID_APP_MIN_VERSION`, `ANDROID_APP_MIN_VERSION_CODE`,
`ANDROID_APP_ENFORCE_MINIMUM`, `ANDROID_APP_RELEASE_NOTES`,
`ANDROID_APP_DOWNLOAD_URL`, `ANDROID_APP_APK_SHA256`,
`ANDROID_APP_APK_SIZE` y `ANDROID_APP_PUBLISHED_AT`.

La URL puede ser absoluta HTTPS o una ruta relativa; una URL no-HTTPS se
devuelve vacía para que Android no descargue nada inseguro.

### 5.4 Verificar el estado seguro

1. Healthcheck:

```powershell
curl.exe -fsS https://<dominio>/up
```

2. Endpoint de versión:

```powershell
curl.exe -fsS https://<dominio>/v1/app-version/android
```

Debe devolver una `download_url` HTTPS, `latest_version_code`, `apk_sha256` y
`apk_size` coherentes con el APK servido.

3. La descarga:

```powershell
curl.exe -I https://<dominio>/apk/ProyecTWin.apk
```

Debe responder `200`, con tipo `application/vnd.android.package-archive` y
`Content-Disposition: attachment`. Las rutas `/apk/otro.apk` y `/apk/` deben
responder `404`.

### 5.5 Aviso in-app y prueba de actualización

- La app consulta la versión **una vez por arranque** (sin caché entre
  procesos); "Más tarde" silencia esa misma versión durante 24 h. No hay un
  límite de 6 h: si se publica una versión nueva, se detecta al reabrir la app.
- Para probar una actualización real, instala la versión anterior firmada
  (1.3.0/4) y publica la nueva (1.4.0/5); abre la app con sesión iniciada.
- Si el dispositivo tiene una instalación antigua debug 1.2.0/3, el aviso
  aparecerá, pero el instalador
  rechazará el APK firmado por el owner: es el comportamiento esperado
  (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). Ver sección 6.
- No uses `ENFORCE_MINIMUM=true` en la release de transición sin avisar antes de
  la desinstalación; un aviso obligatorio que no se puede instalar deja al
  usuario bloqueado.

## 6. Actualización sobre instalaciones debug

Android solo permite actualizar una app si el APK nuevo está firmado con la
**misma clave** que el instalado. Las instalaciones **debug 1.2.0/3** usan la
clave debug; el release **1.3.0/4 y posteriores** usa la clave del owner
(`CN=Oscar`), que es distinta. Por tanto:

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package com.example.proyectwin signatures
do not match previously installed version; ignoring!
```

Opciones, en orden de recomendación:

### Opción A — Mantener `applicationId` y desinstalar el debug (recomendada)

1. Comunicar a los testers que deben desinstalar la app de pruebas **antes** de
   instalar la release.
2. Desinstalar:

```powershell
adb uninstall com.example.proyectwin
```

   o Ajustes → Aplicaciones → ProyecTwin → Desinstalar.

3. Instalar el APK release con `adb install` o transferencia local; la descarga
   pública desde `/apk/` permanece deshabilitada hasta autorización.
4. Iniciar sesión de nuevo.

**Pérdida:** se borran los datos locales de la app (preferencias, token de sesión,
caché). Los datos del servidor no se borran. La sesión de Sanctum del servidor
puede seguir viva hasta su expiración (`SANCTUM_EXPIRATION=1440` minutos), pero el
dispositivo ya no tiene el token. Si se quiere cerrar del todo, el usuario puede
cerrar sesión antes de desinstalar o un admin puede revocar la sesión.

**Ventaja:** una sola app, mismo ID, actualizaciones futuras directas.

### Opción B — Cambiar `applicationId` (nueva app)

Si el owner decide un ID definitivo, el release se instala como una app distinta
y puede convivir con el debug. No hay migración de datos locales ni actualización
directa: el usuario debe instalar la nueva app y volver a iniciar sesión. El aviso
in-app del debug abrirá el APK nuevo; el instalador lo tratará como otra app. Es
útil para una transición con ambos artefactos en paralelo, pero confunde a los
usuarios y divide la base instalada. No se recomienda salvo que el owner ya haya
decidido el ID definitivo.

### Opción C — No válida: firmar un debug con la clave release

Un APK `debuggable` sigue siendo un build de pruebas aunque se firme con la clave
del owner; además, si se configura la firma release en `debug`, se mezcla la
identidad de producción con el logging y las excepciones de red de debug. **No
hacerlo.**

### Comunicación de la transición

Incluye en `ANDROID_APP_RELEASE_NOTES` y en el canal habitual (correo, chat),
sin publicar una página de descarga:

> Esta es la primera versión firmada. Si tienes instalada una versión de pruebas
> (1.2.0 o anterior), desinstálala antes de instalar esta actualización; de lo
> contrario Android mostrará un error de firma. Desinstalar borra la sesión
> local: vuelve a iniciar sesión.

Para el aviso in-app, `ENFORCE_MINIMUM=false` permite posponer; si se activa
`true`, el usuario no podrá usar la app hasta actualizar, pero seguirá necesitando
desinstalar el debug. No actives la obligatoriedad hasta que la instrucción de
desinstalación esté comunicada.

## 7. CI con secretos (opción recomendada)

Automatizar la firma reduce el riesgo de publicar un `assembleDebug` o un APK
sin firmar. El keystore se guarda como **secreto** y se decodifica solo en el
runner efímero. La codificación Base64 **no es cifrado**: no la pegues en el repo,
en un issue ni en un log.

### 7.1 Secretos

Configurar en el repositorio/entorno protegido (por ejemplo, GitHub Actions →
Settings → Secrets and variables → Actions, environment `android-release`):

| Secreto | Contenido |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Keystore `.jks` codificado en Base64 (una sola línea) |
| `ANDROID_KEYSTORE_SHA256` | SHA-256 del archivo `.jks` en minúsculas, para verificar la decodificación |
| `ANDROID_KEYSTORE_PASSWORD` | Contraseña del keystore |
| `ANDROID_KEY_ALIAS` | `app_ProyecTWin` |
| `ANDROID_KEY_PASSWORD` | Contraseña de la clave |
| `RAILWAY_TOKEN` | Solo si el deploy se automatiza; token de proyecto, no de cuenta |

Para crear el Base64 en Windows (sin imprimirlo en la consola):

```powershell
[Convert]::ToBase64String(
  [IO.File]::ReadAllBytes("$env:USERPROFILE\.proyecttwin\proyecttwin-release.jks")
) | Set-Clipboard
```

Pégalo directamente en el campo del secreto. En Linux: `base64 -w0 release.jks`;
en macOS: `base64 -i release.jks | tr -d '\n'`. No uses `cat` en un log.

`Get-FileHash` devuelve el hash en mayúsculas; para el secreto
`ANDROID_KEYSTORE_SHA256` conviértelo a minúsculas:

```powershell
(Get-FileHash "$env:USERPROFILE\.proyecttwin\proyecttwin-release.jks" -Algorithm SHA256).Hash.ToLowerInvariant()
```

### 7.2 Esquema de workflow (GitHub Actions)

El repositorio ya incluye CI web (`.github/workflows/ci.yml`); este esquema
sigue siendo la referencia para una futura CI de release Android. Ajusta las
versiones del SDK a las de `ejecutar-android.md` (Platform 36.1,
Build-Tools 36.1.0).

```yaml
name: android-release

on:
  push:
    tags: ["android-v*"]
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest
    environment: android-release # required reviewers / secrets protegidos
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - uses: android-actions/setup-android@v3

      - name: Instalar SDK/Build-Tools
        run: sdkmanager "platforms;android-36.1" "build-tools;36.1.0"

      - name: Decodificar keystore
        env:
          KEYSTORE_B64: ${{ secrets.ANDROID_KEYSTORE_BASE64 }}
          KEYSTORE_SHA256: ${{ secrets.ANDROID_KEYSTORE_SHA256 }}
        run: |
          set -euo pipefail
          echo "$KEYSTORE_B64" | base64 -d > "$RUNNER_TEMP/release.jks"
          echo "$KEYSTORE_SHA256  $RUNNER_TEMP/release.jks" | sha256sum -c -
          echo "PROYECTTWIN_STORE_FILE=$RUNNER_TEMP/release.jks" >> "$GITHUB_ENV"

      - name: Pruebas unitarias
        run: |
          cd Movil
          ./gradlew :app:testDebugUnitTest

      - name: Compilar release
        env:
          PROYECTTWIN_STORE_PASSWORD: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          PROYECTTWIN_KEY_ALIAS: ${{ secrets.ANDROID_KEY_ALIAS }}
          PROYECTTWIN_KEY_PASSWORD: ${{ secrets.ANDROID_KEY_PASSWORD }}
        run: |
          cd Movil
          ./gradlew :app:assembleRelease \
            -Pproyectwin.apiBaseUrl.release="https://<dominio>/v1/"

      - name: Verificar APK
        run: |
          set -euo pipefail
          BT="$ANDROID_HOME/build-tools/36.1.0"
          APK="Movil/app/build/outputs/apk/release/app-release.apk"
          BADGING="$("$BT/aapt" dump badging "$APK")"
          echo "$BADGING" | grep '^package:'
          ! echo "$BADGING" | grep -q 'application-debuggable'
          "$BT/apksigner" verify --verbose --print-certs "$APK"
          sha256sum "$APK" | tee apk.sha256

      - uses: actions/upload-artifact@v4
        with:
          name: proyecttwin-apk
          path: |
            Movil/app/build/outputs/apk/release/app-release.apk
            apk.sha256
          if-no-files-found: error
```

Reglas de seguridad del CI:

- El job de firma usa un `environment` con revisores obligatorios y secretos
  limitados a ramas/tags protegidos. No expongas secretos a PR de forks.
- `permissions: contents: read`; no des permisos de escritura al job de build.
- No imprimas `KEYSTORE_B64`, contraseñas ni `keystore.properties`; GitHub enmascara
  los secretos, pero un `echo` explícito sigue siendo un riesgo.
- El runner es efímero: no cachees `$RUNNER_TEMP` ni subas `release.jks` como
  artefacto.
- Verifica el SHA-256 del keystore antes de compilar; si no coincide, falla.
- El deploy a Railway es un job separado, manual o con aprobación. **No debe
  copiar el APK ni publicar `/apk/`**: la distribución permanece deshabilitada
  hasta autorización escrita y un cambio de código explícito.
- Si el keystore o sus contraseñas se filtran, asume que la clave está
  comprometida: la clave de firma no se puede rotar para instalaciones
  existentes y cambiar la contraseña no invalida una copia ya filtrada. Con Play
  App Signing existe rotación de la clave de subida; en APK directo, la
  alternativa es una nueva identidad (`applicationId`).

## 8. Rollback y errores comunes

### 8.1 Rollback

- Conserva el APK anterior y las variables anteriores fuera del repo.
- Si el deploy falla antes de publicar, restaura las variables y redespliega.
  No copies el APK al contexto de build mientras la distribución esté cerrada.
- Si el APK ya se publicó y los usuarios lo instalaron, **no publiques un
  `versionCode` menor**: Android rechaza el downgrade. Publica una corrección con
  `versionCode` mayor.
- Si el problema es la firma (clave equivocada), no hay rollback de identidad:
  restaura la clave correcta desde el backup y vuelve a firmar con el mismo
  `versionCode`/`versionName` (o uno mayor) antes de que los usuarios instalen el
  APK defectuoso. Si ya lo instalaron, necesitarás una nueva identidad.
- Si el problema es la URL de API, corrige `proyectwin.apiBaseUrl.release`,
  recompila y publica con `versionCode` mayor.

### 8.2 Diagnóstico

| Síntoma | Causa probable | Acción |
|---|---|---|
| `Keystore file not set for signing config release` | Falta `PROYECTTWIN_STORE_FILE` o la ruta del `.properties` | Revisar ruta absoluta y permisos; no generar otra clave |
| `Keystore was tampered with, or password was incorrect` | Contraseña/alias incorrectos o keystore corrupto | Restaurar backup; verificar con `keytool -list` |
| `aapt` muestra `application-debuggable` | Se compiló `assembleDebug` o el manifiesto lo fuerza | Compilar `assembleRelease`; revisar el manifiesto |
| `apksigner` no verifica o v2/v3 en `false` | APK sin firmar, corrupto o firma incompleta | Volver a compilar; no publicar |
| El certificado es `CN=Android Debug` | Se usó la clave debug | Firmar con la clave del owner; no publicar |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Firma distinta (debug → release o clave cambiada) | Desinstalar el debug o cambiar `applicationId` (sección 6) |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | `versionCode` menor o igual al instalado | Subir `versionCode` |
| `baseUrl must end in /` | URL de release sin barra final | Usar `https://<dominio>/v1/` |
| La app no conecta en release | URL ausente, HTTP o dominio incorrecto | Recompilar con HTTPS; revisar `network_security_config` |
| El endpoint sigue con la versión anterior | Variables sin guardar o deploy sin terminar | Revisar `railway variables`, `railway logs`, redesplegar |
| `/apk/ProyecTWin.apk` responde 404 | APK no incluido en el contexto o ruta Nginx incorrecta | Verificar `.dockerignore`/`.railwayignore`, `docker/nginx.conf` y que el archivo exista |
| El aviso in-app no aparece | `download_url` vacío, no HTTPS o `versionCode` no mayor | Revisar variables y el contrato `/v1/app-version/android` |

## 9. Checklist de release

- [ ] El owner confirmó custodio, alias, `applicationId`, minify/R8, versión y
      política de distribución.
- [ ] La clave se generó **fuera del repo**, con contraseñas en gestor y dos
      backups verificados.
- [ ] La huella SHA-256 del certificado está registrada.
- [ ] `.gitignore` cubre `*.jks`, `*.keystore`, `*.p12` y `keystore.properties`;
      `git status` no muestra secretos.
- [ ] `signingConfig` lee `keystore.properties`/variables de entorno; `assembleDebug`
      sigue funcionando sin clave.
- [ ] `proyectwin.apiBaseUrl.release=https://<dominio>/v1/`; no se usa el fallback
      HTTP.
- [ ] `versionCode` 6 y `versionName` 1.4.1 definidos; `ANDROID_APP_VERSION` coincide.
- [ ] `aapt dump badging`: versión correcta y sin `application-debuggable`.
- [ ] `apksigner verify --verbose --print-certs`: v2/v3 y certificado esperado.
- [ ] SHA-256 del APK calculado y registrado.
- [ ] `backend/public/apk/ProyecTWin.apk` es el APK autorizado 1.4.1/6 y su hash coincide.
- [ ] Variables de Railway actualizadas (`ANDROID_APP_VERSION`, `VERSION_CODE`,
      mínimo, URL, hash/tamaño y notas) y redeploy correcto.
- [ ] `/up` y `/v1/app-version/android` verificados con URL HTTPS, `versionCode`,
      SHA-256 y tamaño.
- [ ] `/apk/ProyecTWin.apk` responde 200 y el resto de `/apk/**` responde 404.
- [ ] Instalación/actualización probada sobre una instalación anterior firmada
      con la misma clave.
- [ ] Instrucciones de desinstalación del debug comunicadas.
- [ ] Backup del APK anterior archivado fuera de `public/`.
- [ ] Release registrada (Anexo A) y tag/commit autorizados.

## Anexo A. Registro de releases

Completar solo con releases reales; no inventar hashes ni fechas.

| Fecha | versionName | versionCode | applicationId | SHA-256 certificado | SHA-256 APK | Responsable | Estado |
|---|---|---|---|---|---|---|---|
| 2026-11-04 | 1.4.1 | 6 | com.example.proyectwin | DC:53:...:3D | F47D2A8321840F9C9CF2836192140C7AC16D7A8BBF695ECEFEB43286AE4D47BC | OpenCode | Firmado y en repositorio; pendiente despliegue |
|---|---:|---:|---|---|---|---|---|
| 2026-10-19 | 1.2.0 | 3 | `com.example.proyectwin` | `72:05:D0:E6:E6:13:58:74:A4:20:73:B0:C9:C8:EB:0D:42:D1:34:DA:63:E5:68:FA:74:F1:74:5E:88:F7:80:93` | — | — | Debug, no oficial |
| 2026-10-22 | 1.3.0 | 4 | `com.example.proyectwin` | `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D` | `A6C63BFE0B01C1020509649B8B163715AA8F21852D664740392FB8256B69B99E` | Oscar | Firmado; base anterior usada para prueba de actualización |
| 2026-11-02 | 1.4.0 | 5 | `com.example.proyectwin` | `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D` | `F462FD283B14D9C7308095131318DB363D9F15F0A667DFB5C65982D183B7A386` | Oscar | Firmado v2/v3; APK autorizado para `/apk/ProyecTWin.apk` |

## Referencias

- [`deploy-railway.md`](deploy-railway.md) — despliegue del backend/SPA y
  variables de Railway.
- [`ejecutar-android.md`](ejecutar-android.md) — compilación y ejecución del APK
  debug en desarrollo.
- `Movil/app/build.gradle.kts` — `applicationId`, versiones, URL de API y firma.
- `backend/config/app_version.php` — contrato de `/v1/app-version/android`.
- `backend/public/apk/ProyecTWin.apk` - APK autorizado 1.4.1/6; servido en
  `/apk/ProyecTWin.apk`.
