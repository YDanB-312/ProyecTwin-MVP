<?php

// Versión publicada de la app Android y política de actualización. Todo se
// define por variables de entorno (ver .env.example): publicar una versión
// nueva = subir el APK y actualizar estas variables, sin tocar código.
return [

    'android' => [
        // Metadatos de la app Android publicada.
        'version' => env('ANDROID_APP_VERSION', '1.4.6'),
        // Criterio principal de actualización; debe crecer en cada release.
        'version_code' => (int) env('ANDROID_APP_VERSION_CODE', 11),

        // Versión mínima soportada: con `enforce_minimum` activo, los clientes
        // por debajo reciben un aviso obligatorio (no posponible). La política
        // está desactivada por defecto.
        'min_version' => env('ANDROID_APP_MIN_VERSION', '1.3.0'),
        'min_version_code' => (int) env('ANDROID_APP_MIN_VERSION_CODE', 4),
        'enforce_minimum' => filter_var(env('ANDROID_APP_ENFORCE_MINIMUM', false), FILTER_VALIDATE_BOOLEAN),

        // Descarga estable. En producción Railway ya define esta variable con
        // la URL HTTPS del APK publicado.
        'download_url' => env('ANDROID_APP_DOWNLOAD_URL', '/apk/ProyecTWin.apk'),
        'sha256' => env('ANDROID_APP_APK_SHA256', '56B7609E03D5E36E3768B4E56C32A1718F3834ACA9AABBC9723C47157F50B62D'),
        'size' => (int) env('ANDROID_APP_APK_SIZE', 17214676),
        'published_at' => env('ANDROID_APP_PUBLISHED_AT', '2026-11-05T22:04:45Z'),

        // Nota breve opcional que acompaña el aviso.
        'release_notes' => env('ANDROID_APP_RELEASE_NOTES', 'Actualización 1.4.6: dos paneles en tabletas, resaltado de selección, accesibilidad y notificaciones.'),
    ],

];
