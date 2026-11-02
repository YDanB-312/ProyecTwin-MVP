# Despliegue en Railway — ProyecTwin MVP

Guía del despliegue del MVP completo (**SPA React + API Laravel**) en un solo
servicio de Railway, con MySQL como base de datos.

> **Estado del APK (Fase 6C).** El artefacto actual es `ProyecTWin.apk`
> versión **1.3.0 / versionCode 4**, firmado con el certificado
> `CN=Oscar`, sin `application-debuggable`.
> SHA-256 del APK:
> `A6C63BFE0B01C1020509649B8B163715AA8F21852D664740392FB8256B69B99E`.
>
> La imagen web **no incluye el APK** mientras no exista autorización escrita
> de Oscar. Nginx responde `404` en `/apk/ProyecTWin.apk` y el botón del Home
> permanece oculto. Para publicarlo, sigue
> [`release-android.md`](release-android.md) y la sección
> [APK en Railway](#apk-en-railway). No re-firmes ni modifiques el APK actual.

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
              │  ├─ /up     → health  │
              │  ├─ /apk/ProyecTWin.apk (opcional, autorizado)
              │  └─ resto   → SPA     │
              └───────────────────────┘
```

**Un solo servicio** porque el SPA usa cookie de sesión httpOnly y CSRF
(`/sanctum/csrf-cookie`), con `/v1` relativo. Al compartir dominio y origen no
hay CORS entre sitios ni cookies cross-site.

## Archivos del despliegue

| Archivo | Para qué |
|---|---|
| `Dockerfile` | Compila el SPA (Vite), instala Composer y arma nginx + php-fpm |
| `docker/nginx.conf` | Rutas: API → PHP, SPA con fallback `index.html` y APK opcional |
| `docker/entrypoint.sh` | Espera MySQL, migra, cachea y arranca; el seed demo no corre en producción |
| `railway.json` | Builder Dockerfile + healthcheck `/up` |
| `.dockerignore` / `.railwayignore` | Excluyen secretos, dependencias, `Movil/` y el APK no autorizado |

## Variables de entorno

### Obligatorias

| Variable | Valor |
|---|---|
| `APP_NAME` | `ProyecTwin-MVP` |
| `APP_ENV` | `production` |
| `APP_DEBUG` | `false` |
| `APP_KEY` | salida de `php artisan key:generate --show` |
| `APP_URL` | `https://<dominio>` (sin barra final) |
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
| `MAIL_MAILER` | `brevo` |
| `BREVO_API_KEY` | clave API v3 de Brevo |
| `MAIL_FROM_ADDRESS` | remitente verificado en Brevo |

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
| `ANDROID_APP_*` | Solo si se publica el APK; ver más abajo |

### Temporal

| Variable | Uso |
|---|---|
| `ADMIN_BOOTSTRAP_ALLOW=1` | Solo durante la ejecución de `admin:inicial`; retirar inmediatamente después |

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
#    SESSION_DRIVER=database y las variables de Brevo antes del primer uso real.
railway variables --set 'APP_ENV=production' --set 'APP_DEBUG=false' `
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

- `https://<dominio>/up` → `{"status":"ok"}`.
- `/` carga el SPA y las rutas internas no dan 404 al recargar.
- Login con cookie, recarga del SPA, `/v1/auth/me` y logout.
- Activación de una cuenta nueva y recuperación de contraseña.
- `/v1/app-version/android` responde y `download_url` está vacío mientras no
  haya APK autorizado.
- `/apk/ProyecTWin.apk` responde 404 mientras no se publique el APK.
- `/apk/otro.apk`, `/apk/` y `/apk/ProyecTWin.apk.php` también responden 404.
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

**Estado actual:** el APK no está incluido en la imagen. `docker/nginx.conf`
tiene una ruta dedicada que responde 404 si el archivo no existe, y
`.dockerignore`/`.railwayignore` excluyen `backend/public/apk` y `*.apk`.
El botón del Home se oculta porque `ANDROID_APP_DOWNLOAD_URL` debe permanecer
vacío hasta que la descarga esté verificada.

Para publicar, **solo con autorización escrita de Oscar**:

1. Recibe el APK release firmado (no debug) y valida versión, firma y SHA-256
   según [`release-android.md`](release-android.md).
2. Coloca únicamente `backend/public/apk/ProyecTWin.apk`.
3. Re-incluye solo ese archivo en `.dockerignore` y `.railwayignore`, en este
   orden y al final de las reglas:
   ```text
   !backend/public/apk/
   !backend/public/apk/ProyecTWin.apk
   ```
4. Reconstruye y despliega. Verifica:
   - `GET /apk/ProyecTWin.apk` → 200.
   - `Content-Type: application/vnd.android.package-archive`.
   - `Content-Disposition: attachment; filename="ProyecTWin.apk"`.
   - SHA-256 remoto idéntico al local.
5. Solo entonces define:
   ```text
   ANDROID_APP_VERSION=1.3.0
   ANDROID_APP_MIN_VERSION=1.0.0
   ANDROID_APP_ENFORCE_MINIMUM=false
   ANDROID_APP_DOWNLOAD_URL=https://<dominio>/apk/ProyecTWin.apk
   ANDROID_APP_RELEASE_NOTES=...
   ```
6. El botón "Descargar APK de ProyecTWIN" del Home aparecerá
   automáticamente al leer `download_url` desde `/v1/app-version/android`.

Si no hay autorización, no cambies las exclusiones, no definas
`ANDROID_APP_DOWNLOAD_URL` y mantén la ruta en 404. La app Android no debe
anunciar una actualización que no existe.

## Una sola réplica y migraciones

El entrypoint ejecuta `php artisan migrate --force` en cada arranque. La
migración de compatibilidad `2026_11_01_000001` añade `email_verified_at` si una
base anterior no lo tiene; con una réplica y despliegues serializados el riesgo
es bajo. **No escales a varias réplicas** mientras las migraciones se ejecuten
automáticamente: MySQL no hace DDL transaccional y dos procesos pueden competir
por la misma tabla. Si más adelante se necesita escalar, mueve las migraciones a
un paso único o usa un lock compartido; no actives `--isolated` con
`CACHE_DRIVER=file`.

No expongas el contenedor con un puerto TCP directo: `TrustProxies='*'` asume
que todo el tráfico entra por el proxy de Railway. Si se abre un acceso directo,
configura los proxies confiables antes de permitirlo.

`CACHE_DRIVER=file` tampoco se comparte entre réplicas. Con una réplica es
suficiente para el MVP.

## Checklist de staging

1. Crear un proyecto/servicio Railway **separado** de producción y un plugin
   MySQL propio.
2. Configurar todas las variables obligatorias de la tabla anterior; usar
   `APP_ENV=production`, `SESSION_DRIVER=database` y Brevo de staging.
3. Desplegar el Dockerfile con una sola réplica y verificar `/up`.
4. Ejecutar `php artisan admin:inicial --check-only`; después, el alta con
   `ADMIN_BOOTSTRAP_ALLOW=1` y `--confirmar-base`.
5. Activar el administrador desde el correo, iniciar sesión y retirar
   `ADMIN_BOOTSTRAP_ALLOW`.
6. Probar recuperación de contraseña y activación de una cuenta nueva.
7. Verificar la sesión: login, recarga del SPA, `/v1/auth/me`, logout y 419/CSRF.
8. Dejar `/apk/ProyecTWin.apk` en 404 y `ANDROID_APP_DOWNLOAD_URL` vacío hasta
   que Oscar autorice el APK y se verifique el 200.
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
| `/apk/ProyecTWin.apk` da 404 | APK no autorizado/incluido; es el comportamiento esperado hasta publicar |
| Error 500 al exportar PDF | Revisar fuentes/permisos de dompdf; `dom` y `mbstring` ya están incluidas en la imagen |

## Notas

- **Seguridad HTTP:** Nginx envía `Strict-Transport-Security: max-age=86400`
  sin `includeSubDomains`/`preload`, además de `nosniff`, `X-Frame-Options` y
  `Referrer-Policy: same-origin`. No se habilita una CSP estricta todavía:
  el SPA usa un script inline y Google Fonts, y las fotos son `data:`; requiere
  un inventario/nonce antes de activarla.
- **Android:** `Movil/` no se despliega en Railway. La app consume
  `/v1/app-version/android`; el APK se publica únicamente con autorización y
  validación de Oscar.
- **Cloudinary:** no está implementado en este backend. Las fotos son data URL
  en `general_users.foto_url`; no configures variables `CLOUDINARY_*`.
- **Costos:** el servicio y MySQL consumen del crédito del plan de Railway.
