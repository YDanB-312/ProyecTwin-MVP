# =============================================================================
# ProyecTwin MVP — imagen única: SPA (React) + API (Laravel) en el mismo origen.
#
# Railway construye esta imagen en la nube a partir de `railway up` (no hace
# falta Docker local). Etapas:
#   1) frontend  → compila el SPA con Vite (dist/)
#   2) vendor    → instala dependencias PHP de producción (composer --no-dev)
#   3) runtime   → php-fpm + nginx sirviendo el SPA desde backend/public/
#
# El contenedor escucha en $PORT (lo inyecta Railway):
#   - /v1/** y /sanctum/**  → Laravel (API + cookie CSRF de Sanctum)
#   - /up                   → healthcheck (ver railway.json)
#   - el resto              → SPA con fallback a index.html (React Router)
# =============================================================================

# Versiones fijas de las imágenes base. Actualizar de forma deliberada.
ARG PHP_VERSION=8.3.36
ARG NODE_VERSION=22.23.4
ARG COMPOSER_VERSION=2.11.1

# ------------------------------------------------------- 1) SPA (frontend) ---
FROM node:${NODE_VERSION}-alpine3.23 AS frontend

WORKDIR /build

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
# La API se consume con ruta relativa: mismo origen, sin CORS.
ENV VITE_API_URL=/v1
RUN npm run build

# ------------------------------------------- 2) dependencias PHP (Composer) ---
# Composer se ejecuta con el mismo PHP 8.3 que el runtime. La imagen oficial
# de Composer solo aporta el ejecutable; no se usa su versión de PHP.
FROM composer:${COMPOSER_VERSION} AS composer

FROM php:${PHP_VERSION}-cli-alpine3.24 AS vendor

ENV COMPOSER_ALLOW_SUPERUSER=1

WORKDIR /app

COPY --from=composer /usr/bin/composer /usr/local/bin/composer
RUN apk add --no-cache unzip

COPY backend/composer.json backend/composer.lock ./
RUN composer install \
        --no-dev \
        --no-scripts \
        --no-interaction \
        --prefer-dist \
        --optimize-autoloader \
    && composer check-platform-reqs --no-dev

# ------------------------------------------------- 3) runtime (fpm + nginx) ---
FROM php:${PHP_VERSION}-fpm-alpine3.24 AS runtime

# nginx (servidor), gettext (envsubst para $PORT), tzdata (America/Bogota) y
# tini (reapa procesos y reenvía señales).
RUN apk add --no-cache nginx gettext tzdata tini

# pdo_mysql no viene compilado en la imagen oficial; mbstring, dom, fileinfo
# y opcache ya forman parte de PHP 8.3 Alpine.
COPY --from=mlocati/php-extension-installer:2.13.1 \
     /usr/bin/install-php-extensions /usr/local/bin/
RUN install-php-extensions pdo_mysql

# php-fpm escucha solo en localhost (nginx lo alcanza por 127.0.0.1:9000) y
# conserva las variables de entorno del contenedor. Su access log se descarta:
# nginx ya registra cada petición con un formato que omite el query string, y
# así no se duplican líneas ni se filtran tokens por rutas heredadas.
RUN printf '[www]\nlisten = 127.0.0.1:9000\nclear_env = no\naccess.log = /dev/null\n' \
        > /usr/local/etc/php-fpm.d/zz-local.conf

WORKDIR /app

# Aplicación Laravel + dependencias + SPA compilado dentro de public/
COPY backend/ ./
COPY --from=vendor /app/vendor ./vendor
COPY --from=frontend /build/dist ./public

# Directorios de escritura para php-fpm (www-data).
# backend/storage y backend/bootstrap/cache se excluyen del contexto
# y se recrean aquí; el entrypoint ejecuta package:discover/config:cache.
RUN rm -f bootstrap/cache/*.php \
    && mkdir -p \
        storage/app/public \
        storage/framework/cache/data \
        storage/framework/sessions \
        storage/framework/views \
        storage/logs \
        storage/fonts \
        bootstrap/cache \
    && chown -R www-data:www-data storage bootstrap/cache

# Plantilla de nginx y script de arranque (migra, cachea y levanta servicios).
COPY docker/ /opt/docker/
RUN chmod +x /opt/docker/entrypoint.sh

ENV APP_ENV=production \
    APP_DEBUG=false \
    LOG_CHANNEL=stderr \
    LOG_LEVEL=warning \
    PORT=8080

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--", "/opt/docker/entrypoint.sh"]
