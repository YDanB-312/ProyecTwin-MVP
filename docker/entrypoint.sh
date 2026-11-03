#!/bin/sh
# =============================================================================
# Arranque del contenedor de ProyecTwin:
#   1. genera la config de nginx con el puerto que inyecta Railway ($PORT)
#   2. descubre paquetes y cachea la configuración de Laravel
#   3. espera la base de datos y ejecuta migraciones (con reintentos)
#   4. siembra datos demo solo fuera de producción (RUN_SEED=true, base vacía)
#   5. levanta php-fpm (segundo plano) y nginx (primer plano)
# =============================================================================
set -e

PORT="${PORT:-8080}"
export PORT

# Normaliza APP_ENV antes de cachear la configuración. Laravel compara el
# entorno de forma sensible a mayúsculas; "Production" no debe saltarse los
# guards de producción.
APP_ENV="$(printf '%s' "${APP_ENV:-production}" | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
[ -n "${APP_ENV}" ] || APP_ENV=production
export APP_ENV

if [ -z "${APP_KEY}" ]; then
    echo "[entrypoint] ERROR: falta la variable APP_KEY (php artisan key:generate --show)" >&2
    exit 1
fi

# Laravel acepta una clave base64 de 32 bytes o una clave cruda de 32
# caracteres. Rechazar formatos inválidos evita que el contenedor arranque
# con una configuración que fallará al cifrar sesiones o cookies.
if ! php -r '
$k = (string) ($argv[1] ?? "");
if (str_starts_with($k, "base64:")) {
    $decoded = base64_decode(substr($k, 7), true);
    exit(is_string($decoded) && strlen($decoded) === 32 ? 0 : 1);
}
exit(strlen($k) === 32 ? 0 : 1);
' "$APP_KEY"; then
    echo "[entrypoint] ERROR: APP_KEY no tiene un formato válido (base64 de 32 bytes o 32 caracteres)" >&2
    exit 1
fi

# Railway puede exponer el dominio público en RAILWAY_PUBLIC_DOMAIN. Se usa
# solo si APP_URL no fue configurado explícitamente.
if [ -z "${APP_URL}" ] && [ -n "${RAILWAY_PUBLIC_DOMAIN}" ]; then
    case "${RAILWAY_PUBLIC_DOMAIN}" in
        https://*) APP_URL="${RAILWAY_PUBLIC_DOMAIN}" ;;
        *) APP_URL="https://${RAILWAY_PUBLIC_DOMAIN}" ;;
    esac
    export APP_URL
    echo "[entrypoint] APP_URL derivado de RAILWAY_PUBLIC_DOMAIN: ${APP_URL}"
fi

# En producción APP_URL es obligatorio: los enlaces de correo y las cookies
# Secure dependen de un origen HTTPS correcto.
if [ -z "${APP_URL}" ]; then
    if [ "${APP_ENV}" = "production" ]; then
        echo "[entrypoint] ERROR: APP_URL es obligatorio en producción (https://<dominio>)" >&2
        exit 1
    fi
    echo "[entrypoint] ADVERTENCIA: APP_URL vacío; define https://<dominio> para enlaces y cookies correctos" >&2
elif [ "${APP_ENV}" = "production" ]; then
    case "${APP_URL}" in
        https://?*) ;;
        *)
            echo "[entrypoint] ERROR: APP_URL debe empezar por https:// en producción" >&2
            exit 1
            ;;
    esac
fi

# FRONTEND_URL alimenta los enlaces de correo y Sanctum. Si no se configuró,
# se deriva del mismo origen público; en producción debe ser HTTPS.
if [ -z "${FRONTEND_URL}" ] && [ -n "${APP_URL}" ]; then
    FRONTEND_URL="${APP_URL}"
    export FRONTEND_URL
    echo "[entrypoint] FRONTEND_URL derivado de APP_URL: ${FRONTEND_URL}"
fi

if [ "${APP_ENV}" = "production" ]; then
    if [ -z "${FRONTEND_URL}" ]; then
        echo "[entrypoint] ERROR: FRONTEND_URL es obligatorio en producción" >&2
        exit 1
    fi

    case "${FRONTEND_URL}" in
        https://?*) ;;
        *)
            echo "[entrypoint] ERROR: FRONTEND_URL debe empezar por https:// en producción" >&2
            exit 1
            ;;
    esac

    APP_DEBUG_NORMALIZADO="$(printf '%s' "${APP_DEBUG:-false}" | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
    case "${APP_DEBUG_NORMALIZADO}" in
        ""|false|0) ;;
        *)
            echo "[entrypoint] ERROR: APP_DEBUG debe ser false/0 en producción" >&2
            exit 1
            ;;
    esac

    case "${SESSION_DRIVER:-file}" in
        database|redis|memcached) ;;
        *)
            echo "[entrypoint] ADVERTENCIA: SESSION_DRIVER=${SESSION_DRIVER:-file} en producción; las sesiones se perderán en cada redeploy. Usa database o un backend compartido." >&2
            ;;
    esac

    TRUSTED_PROXIES_NORMALIZADO="$(printf '%s' "${TRUSTED_PROXIES}" | tr -d '[:space:]')"
    case "${TRUSTED_PROXIES_NORMALIZADO}" in
        ""|"*")
            echo "[entrypoint] ERROR: TRUSTED_PROXIES es obligatorio en producción y no puede ser '*'. Define los CIDR/IP del proxy." >&2
            exit 1
            ;;
    esac
fi

echo "[entrypoint] nginx escuchará en el puerto ${PORT}"
envsubst '${PORT}' < /opt/docker/nginx.conf > /etc/nginx/http.d/default.conf

cd /app

echo "[entrypoint] descubriendo paquetes de Laravel…"
php artisan package:discover --ansi

echo "[entrypoint] cacheando configuración…"
php artisan config:cache

RUN_MIGRATIONS="$(printf '%s' "${RUN_MIGRATIONS:-true}" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')"
export RUN_MIGRATIONS

if [ "$RUN_MIGRATIONS" = "true" ] || [ "$RUN_MIGRATIONS" = "1" ]; then
    echo "[entrypoint] ejecutando migraciones con lock…"
    intentos=0
    until php artisan app:migrar-seguro --timeout=30; do
        codigo=$?
        if [ "$codigo" -ne 2 ] && [ "$codigo" -ne 3 ]; then
            echo "[entrypoint] ERROR: la migración falló con código ${codigo}; no se reintenta." >&2
            exit 1
        fi
        intentos=$((intentos + 1))
        if [ "$intentos" -ge 24 ]; then
            echo "[entrypoint] ERROR: la base de datos o el lock de migraciones no estuvo disponible tras 2 minutos" >&2
            exit 1
        fi
        if [ "$codigo" -eq 3 ]; then
            echo "[entrypoint] lock de migraciones ocupado; reintento ${intentos}/24 en 5 s…"
        else
            echo "[entrypoint] base de datos no disponible; reintento ${intentos}/24 en 5 s…"
        fi
        sleep 5
    done
elif [ "$RUN_MIGRATIONS" = "false" ] || [ "$RUN_MIGRATIONS" = "0" ]; then
    # Alternativa para releases: las migraciones se ejecutan fuera del arranque.
    # No se permite arrancar con migraciones pendientes.
    echo "[entrypoint] RUN_MIGRATIONS=false: verificando que no haya migraciones pendientes…"
    estado="$(php artisan migrate:status --no-ansi 2>&1)" || {
        echo "[entrypoint] ERROR: no se pudo consultar el estado de migraciones:" >&2
        echo "$estado" >&2
        exit 1
    }
    if echo "$estado" | grep -q 'Pending'; then
        echo "[entrypoint] ERROR: hay migraciones pendientes y RUN_MIGRATIONS=false." >&2
        echo "[entrypoint] Ejecuta el paso de release antes de arrancar la aplicación." >&2
        exit 1
    fi
    echo "[entrypoint] esquema al día; se omite migrate."
else
    echo "[entrypoint] ERROR: RUN_MIGRATIONS=${RUN_MIGRATIONS} no es válido (true/false)." >&2
    exit 1
fi

if [ "${RUN_SEED}" = "true" ]; then
    if [ "${APP_ENV}" = "production" ]; then
        echo "[entrypoint] RUN_SEED=true ignorado: los seeders demo están bloqueados en producción; usa admin:inicial" >&2
    else
        echo "[entrypoint] RUN_SEED=true: el seeder corre solo si la base está vacía"
        estado=$(php -r '
            require "/app/vendor/autoload.php";
            $app = require "/app/bootstrap/app.php";
            $app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();
            echo App\Models\GeneralUser::query()->exists() ? "con-datos" : "vacia";
        ' 2>/dev/null || echo "desconocido")

        if [ "$estado" = "vacia" ]; then
            echo "[entrypoint] base vacía: ejecutando seeder…"
            php artisan db:seed --force
        else
            echo "[entrypoint] la base ya tiene datos (${estado}); se omite el seeder"
        fi
    fi
fi

echo "[entrypoint] arrancando php-fpm y nginx…"
php-fpm -D
exec nginx -g "daemon off;"
