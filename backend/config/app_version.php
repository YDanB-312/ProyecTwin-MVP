<?php

// Versión publicada de la app Android y política de actualización. Todo se
// define por variables de entorno (ver .env.example): publicar una versión
// nueva = subir el APK y actualizar estas variables, sin tocar código.
return [

    'android' => [
        // Metadatos de la app Android publicada.
        'version' => env('ANDROID_APP_VERSION', '1.4.0'),
        // Criterio principal de actualización; debe crecer en cada release.
        'version_code' => (int) env('ANDROID_APP_VERSION_CODE', 5),

        // Versión mínima soportada: con `enforce_minimum` activo, los clientes
        // por debajo reciben un aviso obligatorio (no posponible). La política
        // está desactivada por defecto.
        'min_version' => env('ANDROID_APP_MIN_VERSION', '1.3.0'),
        'min_version_code' => (int) env('ANDROID_APP_MIN_VERSION_CODE', 4),
        'enforce_minimum' => filter_var(env('ANDROID_APP_ENFORCE_MINIMUM', false), FILTER_VALIDATE_BOOLEAN),

        // Descarga estable. En producción Railway ya define esta variable con
        // la URL HTTPS del APK publicado.
        'download_url' => env('ANDROID_APP_DOWNLOAD_URL', '/apk/ProyecTWin.apk'),
        'sha256' => env('ANDROID_APP_APK_SHA256', ''),
        'size' => (int) env('ANDROID_APP_APK_SIZE', 0),
        'published_at' => env('ANDROID_APP_PUBLISHED_AT', ''),

        // Nota breve opcional que acompaña el aviso.
        'release_notes' => env('ANDROID_APP_RELEASE_NOTES', ''),
    ],

];
