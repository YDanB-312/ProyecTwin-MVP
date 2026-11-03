# Despliegue en Railway — ProyecTwin MVP

Guía del despliegue del MVP completo (**SPA React + API Laravel**) en un solo
servicio de Railway, con MySQL como base de datos.

> **Política del APK.** El APK autorizado **1.4.0 / versionCode 5** se incluye
> en la imagen y se sirve únicamente en `/apk/ProyecTWin.apk`. El endpoint
> `/v1/app-version/android` publica URL HTTPS, versión, hash y tamaño. No
> re-firmes ni cambies de identidad el APK sin autorización del owner.

## Arquitectura

```text
Internet ── HTTPS ──► Railway (proxy TLS)
                        │
                        ▼
              ┌───────────────────────┐      ┌──────────────┐
              │  servicio "backend"   │      │    MySQL     │
              │  nginx + php-fpm      │─────►│  (plugin)    │
              │  ├─ /v1/**  → Laravel │      └──────────────┘
              │  ├─ /sanctum/**       │
              │  ├─ /up     → liveness │
              │  ├─ /ready  → readiness│
              │  ├─ /apk/** → 404      │
              │  └─ resto   → SPA      │
              └───────────────────────┘
```

**Un solo servicio** porque el SPA usa cookie de sesión httpOnly y CSRF
(`/sanctum/csrf-cookie`), con `/v1` relativo. Al compartir dominio y origen no
hay CORS entre sitios ni cookies cross-site.

## Archivos del despliegue

| Archivo | Para qué |
|---|---|
| `Dockerfile` | Compila el SPA (Vite), instala Composer y arma nginx + php-fpm |
| `docker/nginx.conf` | Rutas: API → PHP, SPA con fallback `index.html`, `/up`, `/ready` y `/apk/**` cerrado |
| `docker/entrypoint.sh` | Espera MySQL, migra con lock (`app:migrar-seguro`), cachea y arranca; el seed demo no corre en producción |
| `railway.json` | Builder Dockerfile + healthcheck `/up` (liveness); `/ready` para readiness |
| `.dockerignore` / `.railwayignore` | Excluyen secretos, dependencias, `Movil/` y el APK histórico |

## Variables de entorno

### Obligatorias

| Variable | Valor |
|---|---|
| `APP_NAME` | `ProyecTwin-MVP` |
| `APP_ENV` | `production` |
| `APP_DEBUG` | `false` |
| `APP_KEY` | salida de `php artisan key:generate --show` |
| `APP_URL` | `https://<dominio>` (sin barra final). Si falta, el entrypoint intenta derivarlo de `RAILWAY_PUBLIC_DOMAIN`; en producción falla si no queda un origen HTTPS. |
| `APP_TIMEZONE` | `America/Bogota` |
| `LOG_CHANNEL` | `stderr` |
| `LOG_LEVEL` | `warning` |
| `DB_CONNECTION` | `mysql` |
| `DB_HOST` | host del plugin MySQL |
| `DB_PORT` | puerto del plugin MySQL |
| `DB_DATABASE` | base del plugin MySQL |
| `DB_USERNAME` | usuario del plugin MySQL |
| `DB_PASSWORD` | contraseña del plugin MySQL |
| `SESSION_DRIVER` | `database` |
| `FRONTEND_URL` | `https://<dominio>` (sin barra final) |
| `SANCTUM_STATEFUL_DOMAINS` | `<dominio>` sin `https://` ni barra final |
| `TRUSTED_PROXIES` | **Obligatorio en producción**: CIDR/IP del proxy de Railway separados por coma. El entrypoint y el middleware abortan si falta o es `*`; verificar que el contenedor no reciba tráfico directo. Con `config:cache`, debe existir en el entorno del proceso (Railway), no solo en `.env`. |
| `MAIL_MAILER` | `brevo` |
| `BREVO_API_KEY` | clave API v3 de Brevo |
| `MAIL_FROM_ADDRESS` | remitente verificado en Brevo |
| `ANDROID_APP_VERSION` / `ANDROID_APP_VERSION_CODE` | versión visible y `versionCode` del APK |
| `ANDROID_APP_DOWNLOAD_URL` | URL HTTPS estable de `/apk/ProyecTWin.apk` |
| `ANDROID_APP_APK_SHA256` / `ANDROID_APP_APK_SIZE` | integridad del APK (opcionales; se calculan del archivo si faltan) |

Puedes usar las referencias `${{MySQL.*}}` del plugin de Railway o una única
`DATABASE_URL` proporcionada por el plugin. **No mezcles ambas formas.** No
inventes nombres de servicios: ajusta las referencias al nombre real del plugin.

### Opcionales o con valor predeterminado seguro

| Variable | Valor recomendado |
|---|---|
| `MAIL_FROM_NAME` | `ProyecTwin` |
| `SESSION_LIFETIME` | `120` (minutos de inactividad de la sesión web) |
| `SESSION_SECURE_COOKIE` | `true`; en producción `Secure` se fuerza igualmente |
| `SANCTUM_EXPIRATION` | `1440` (24 h para tokens Bearer móviles) |
| `CACHE_DRIVER` | `file` (aceptable con una réplica) |
| `QUEUE_CONNECTION` | `sync` |
| `FILESYSTEM_DISK` | `local` |
| `API_RATE_LIMIT` | `300` para la demo; el valor por defecto es `60` |
| `ANDROID_APP_*` | Versión, `versionCode`, mínimo, URL HTTPS, SHA-256, tamaño y notas del APK autorizado |

### Temporal

| Variable | Uso |
|---|---|
| `ADMIN_BOOTSTRAP_ALLOW=1` | Solo durante la ejecución de `admin:inicial`; retirar inmediatamente después |

El entrypoint valida que `APP_KEY` sea una clave base64 de 32 bytes o una
clave cruda de 32 caracteres. Si `APP_URL` falta, intenta derivarlo de
`RAILWAY_PUBLIC_DOMAIN`; en producción falla si no queda un origen HTTPS.
También deriva `FRONTEND_URL` de `APP_URL` si falta, exige HTTPS para ambas,
rechaza cualquier `APP_DEBUG` distinto de `false`/`0`, exige `TRUSTED_PROXIES` distinto de `*` y advierte si
`SESSION_DRIVER` no es compartido. La imagen fija `SESSION_DRIVER=database` y
no copia el APK histórico.

No definas `RUN_SEED=true` en producción: el entrypoint lo ignora y
`DemoSeeder` está bloqueado. No uses `MAIL_MAILER=log` para cuentas reales:
`admin:inicial` lo rechaza y la activación no llegaría.

## Cookies, sesiones y tokens

- `SESSION_LIFETIME=120` es el **TTL de inactividad de la sesión web** y de su
  cookie. No es la vida del token móvil.
- `SANCTUM_EXPIRATION=1440` es la **edad absoluta de los tokens Bearer de la
  app móvil** (24 h, sin renovación automática). No aplica a la sesión por
  cookie del SPA.
- "Recordarme" usa la cookie persistente de Laravel (recaller). Su duración por
  defecto es de 400 días, aunque la sesión inactiva siga venciendo a los 120
  minutos. Si la política debe ser más estricta, decide y configura
  `auth.guards.web.remember` en una fase separada.
- El logout rota `remember_token` para revocar copias de la cookie. Como
  `CuentaActiva` usa ese token como versión de sesión, también se invalidan las
  sesiones de cookie de otros dispositivos. Es el alcance aceptado para no
  implementar tokens por dispositivo en el MVP.
- `SESSION_DRIVER=database` requiere la migración de `sessions` y permite que
  la sesión sobreviva a un redeploy. `SESSION_DOMAIN` debe quedar vacío
  (cookie host-only). `SameSite=Lax` y `HttpOnly` están fijados.
- `Referrer-Policy: same-origin` es necesario para que Sanctum reconozca las
  peticiones GET del SPA como stateful. No la cambies a `no-referrer`.
- `SANCTUM_STATEFUL_DOMAINS` reemplaza los valores por defecto; si falta el
  dominio real, el login puede responder 200 sin sesión y `/auth/me` fallar
  tras recargar.

## Pasos iniciales (PowerShell)

```powershell
# 1. CLI de Railway
npm i -g @railway/cli

# 2. Autenticación
railway login --browserless

# 3. Proyecto y base MySQL
railway init -n proyecttwin
railway add --database mysql

# 4. Variables (ajusta las referencias al nombre real del plugin)
#    Define también APP_KEY, APP_URL, FRONTEND_URL, SANCTUM_STATEFUL_DOMAINS,
#    SESSION_DRIVER=database, TRUSTED_PROXIES y las variables de Brevo antes
#    del primer uso real. TRUSTED_PROXIES debe ser el CIDR real del proxy.
railway variables --set 'APP_ENV=production' --set 'APP_DEBUG=false' `
  --set 'SESSION_DRIVER=database' `
  --set 'TRUSTED_PROXIES=<cidr-proxy-railway>' `
  --set 'APP_KEY=<clave-generada>' `
  --set 'DB_CONNECTION=mysql' `
  --set 'DB_HOST=${{MySQL.MYSQLHOST}}' `
  --set 'DB_PORT=${{MySQL.MYSQLPORT}}' `
  --set 'DB_DATABASE=${{MySQL.MYSQLDATABASE}}' `
  --set 'DB_USERNAME=${{MySQL.MYSQLUSER}}' `
  --set 'DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}'

# 5. Desplegar el Dockerfile de la raíz
railway up

# 6. Dominio público HTTPS
railway domain
```

Después de crear el dominio, define `APP_URL`, `FRONTEND_URL` y
`SANCTUM_STATEFUL_DOMAINS` con ese valor. Railway redespliega al cambiar
variables; usa `--skip-deploys` si necesitas agrupar cambios.

## Primer administrador

Requisitos: base migrada y vacía de usuarios, `APP_ENV=production`,
`APP_URL`/`FRONTEND_URL` HTTPS y Brevo con remitente verificado.

```sh
# Abre una shell dentro del servicio desplegado:
railway ssh
cd /app
php artisan admin:inicial --check-only

ADMIN_BOOTSTRAP_ALLOW=1 php artisan admin:inicial \
  --correo="admin@dominio.com" \
  --nombre="..." \
  --apellido="..." \
  --tipo-documento=CC \
  --documento="..." \
  --confirmar-base="<nombre-exacto-de-la-base>"
```

El comando crea una cuenta pendiente, sin contraseña temporal, y envía el
enlace de activación. No imprime tokens ni contraseñas.

1. Activa la cuenta desde el correo con una contraseña de 12+ caracteres.
2. Verifica el login y `/v1/auth/me`.
3. Retira `ADMIN_BOOTSTRAP_ALLOW` de Railway.
4. Comprueba con `--check-only` que existe 1 administrador.

Si el correo falla, la cuenta queda pendiente:

```sh
ADMIN_BOOTSTRAP_ALLOW=1 php artisan admin:inicial \
  --correo="admin@dominio.com" \
  --reenviar \
  --confirmar-base="<nombre-exacto-de-la-base>"
```

No ejecutes el alta dos veces en paralelo; el procedimiento está pensado para
una ejecución única del owner.

## Verificación posterior al despliegue

- `https://<dominio>/up` → `{"status":"ok"}` (liveness, sin sesión).
- `https://<dominio>/ready` → `{"status":"ready"}` con MySQL disponible.
- `/` carga el SPA y las rutas internas no dan 404 al recargar.
- Login con cookie, recarga del SPA, `/v1/auth/me` y logout.
- Activación de una cuenta nueva y recuperación de contraseña.
- `/v1/app-version/android` responde `1.4.0`, `versionCode 5` y una URL HTTPS.
- `/apk/ProyecTWin.apk` responde 200; `/apk/otro.apk`, `/apk/` y
  `/apk/ProyecTWin.apk.php` responden 404.
- El Home muestra el botón de descarga cuando la URL es válida.
- Logs sin tokens, query strings sensibles ni credenciales.

## Correo real con Brevo

El backend envía activación, recuperación y avisos por la API HTTP de Brevo.
Variables:

```text
MAIL_MAILER=brevo
BREVO_API_KEY=<clave API v3>
MAIL_FROM_ADDRESS=<remitente verificado>
MAIL_FROM_NAME=ProyecTwin
```

Requisitos:

1. Remitente verificado en Brevo.
2. Si la seguridad por IP está activa, autorizar la salida de Railway o
   desactivar la restricción en `app.brevo.com/security/authorised_ips`.
3. Probar activación y reset reales; abrir el enlace en Gmail/Outlook y
   comprobar que el fragmento `#token=...` se conserva tras el click tracking.
4. Revisar que `account_activations.sent_at` tenga valor y `error` esté vacío.

Los errores de Brevo se registran en `railway logs` (401/400, remitente no
verificado, IP no autorizada). No se registra la clave.

## APK en Railway

**Estado actual:** el APK **1.4.0 / versionCode 5**, firmado con `CN=Oscar`, está
tracked en `backend/public/apk/ProyecTWin.apk` y se incluye en la imagen.
`.dockerignore` y `.railwayignore` excluyen cualquier APK salvo ese archivo;
Nginx lo sirve únicamente en `/apk/ProyecTWin.apk` y responde 404 para el resto
de `/apk/**`. El controlador publica URL HTTPS, versión, hash y tamaño.

Requisitos para una nueva versión:

1. Firmar con la misma keystore original y aumentar `versionCode`.
2. Validar firma, SHA-256, tamaño, MIME y actualización in-app.
3. Actualizar las variables `ANDROID_APP_*` y desplegar solo con autorización.
4. No reemplazar el APK publicado por otro con distinta identidad de firma.

## Una sola réplica y migraciones

El entrypoint ejecuta `php artisan app:migrar-seguro`, que adquiere un lock
MySQL (`GET_LOCK`) y después ejecuta `migrate --force`. Si no hay migraciones
pendientes, no se ejecuta DDL. El lock evita carreras entre arranques
concurrentes. Las bases nuevas reciben `email_verified_at` desde la migración
inicial; una base legacy que no la tenga requiere una reparación manual con
backup y autorización, no una migración automática.

Alternativa de release: definir `RUN_MIGRATIONS=false` y ejecutar
`php artisan app:migrar-seguro` en un paso único antes del despliegue. Con ese
valor, el entrypoint verifica `migrate:status` y **aborta si hay pendientes**;
no arranca silenciosamente con un esquema incompatible.

**Mantén una sola réplica** mientras `CACHE_DRIVER=file` y el rate limit usen
almacenamiento local. El lock protege las migraciones; `CACHE_DRIVER=file` y el
rate limit no se comparten, mientras que `SESSION_DRIVER=database` sí comparte
sesiones entre réplicas. No actives `--isolated` con `CACHE_DRIVER=file`.

No expongas el contenedor con un puerto TCP directo. En producción
`TRUSTED_PROXIES` es obligatorio y `*` se rechaza; configura el CIDR real del
proxy de Railway y verifica que el contenedor no reciba tráfico directo.

`CACHE_DRIVER=file` tampoco se comparte entre réplicas. Con una réplica es
suficiente para el MVP.

## Checklist de staging

1. Crear un proyecto/servicio Railway **separado** de producción y un plugin
   MySQL propio.
2. Configurar todas las variables obligatorias de la tabla anterior; usar
   `APP_ENV=production`, `SESSION_DRIVER=database` y Brevo de staging.
3. Desplegar el Dockerfile con una sola réplica y verificar `/up` y `/ready`.
4. Ejecutar `php artisan admin:inicial --check-only`; después, el alta con
   `ADMIN_BOOTSTRAP_ALLOW=1` y `--confirmar-base`.
5. Activar el administrador desde el correo, iniciar sesión y retirar
   `ADMIN_BOOTSTRAP_ALLOW`.
6. Probar recuperación de contraseña y activación de una cuenta nueva.
7. Verificar la sesión: login, recarga del SPA, `/v1/auth/me`, logout y 419/CSRF.
8. Verificar que `/apk/ProyecTWin.apk` responde 200, que `/apk/` responde 404
   y que `/v1/app-version/android` publica la URL, hash y versión correctos.
9. Revisar logs de Railway: sin tokens, sin query strings sensibles, sin errores
   de Brevo ni de migraciones.
10. Solo entonces declarar staging validado; producción requiere repetir el
    smoke con sus propios secretos y base.

## Solución de problemas

| Síntoma | Causa probable |
|---|---|
| Healthcheck falla o migraciones en loop | `DB_*`/`DATABASE_URL` incorrectos o MySQL aún creándose; revisa `railway logs` |
| `No application encryption key` | Falta `APP_KEY` |
| Login 200 pero `/auth/me` 401 tras recargar | `SANCTUM_STATEFUL_DOMAINS`, `FRONTEND_URL` o `Referrer-Policy` incorrectos |
| 419/CSRF | Dominio stateful mal configurado; el SPA reintenta una vez renovando el token |
| No llega el correo de activación | Mailer en `log`, Brevo sin remitente/IP o `FRONTEND_URL` inválida |
| `/apk/ProyecTWin.apk` da 404 | El APK no entró en la imagen, ruta Nginx incorrecta o archivo ausente |

## Notas

- **Seguridad HTTP:** Nginx envía `Strict-Transport-Security: max-age=86400`
  sin `includeSubDomains`/`preload`, `nosniff`, `X-Frame-Options`,
  `Referrer-Policy: same-origin` y CSP `default-src 'self'` con
  `script-src 'self'` (el tema es `theme.js` externo). `style-src` permite
  `'unsafe-inline'` porque React usa atributos `style` inline, y autoriza solo
  `fonts.googleapis.com`; `font-src` autoriza `fonts.gstatic.com`. No se permite
  `unsafe-eval`. `server_tokens` está desactivado y `expose_php=Off` evita
  `X-Powered-By`.
- **Android:** `Movil/` no se despliega en Railway. La app consume
  `/v1/app-version/android` y descarga el APK autorizado desde
  `/apk/ProyecTWin.apk`.
- **Cloudinary:** no está implementado en este backend. Las fotos son data URL
  en `general_users.foto_url`; no configures variables `CLOUDINARY_*`.
- **Costos:** el servicio y MySQL consumen del crédito del plan de Railway.
