<?php

// Versión publicada de la app Android y política de actualización. Todo se
// define por variables de entorno (ver .env.example): publicar una versión
// nueva = subir el APK y actualizar estas variables, sin tocar código.
return [

    'android' => [
        // Metadatos de la app Android. El APK histórico se conserva en el
        // repositorio, pero no se distribuye ni se anuncia hasta que Oscar
        // autorice su publicación. El controlador mantiene download_url vacío.
        'version' => env('ANDROID_APP_VERSION', '1.3.0'),

        // Versión mínima soportada: con `enforce_minimum` activo, los clientes
        // por debajo reciben un aviso obligatorio (no posponible). La política
        // está desactivada por defecto.
        'min_version' => env('ANDROID_APP_MIN_VERSION', '1.0.0'),
        'enforce_minimum' => filter_var(env('ANDROID_APP_ENFORCE_MINIMUM', false), FILTER_VALIDATE_BOOLEAN),

        // Nota breve opcional que acompaña el aviso.
        'release_notes' => env('ANDROID_APP_RELEASE_NOTES', ''),
    ],

];
