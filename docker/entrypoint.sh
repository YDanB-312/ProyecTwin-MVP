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

# No se exige antes del primer deploy: Railway asigna el dominio después.
# Si está definida en producción, debe ser HTTPS.
if [ -z "${APP_URL}" ]; then
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

echo "[entrypoint] nginx escuchará en el puerto ${PORT}"
envsubst '${PORT}' < /opt/docker/nginx.conf > /etc/nginx/http.d/default.conf

cd /app

echo "[entrypoint] descubriendo paquetes de Laravel…"
php artisan package:discover --ansi

echo "[entrypoint] cacheando configuración…"
php artisan config:cache

echo "[entrypoint] ejecutando migraciones…"
intentos=0
until php artisan migrate --force; do
    intentos=$((intentos + 1))
    if [ "$intentos" -ge 24 ]; then
        echo "[entrypoint] ERROR: la base de datos no respondió tras 2 minutos" >&2
        exit 1
    fi
    echo "[entrypoint] base de datos no disponible; reintento ${intentos}/24 en 5 s…"
    sleep 5
done

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
