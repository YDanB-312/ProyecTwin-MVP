<?php

// Versión publicada de la app Android y política de actualización. Todo se
// define por variables de entorno (ver .env.example): publicar una versión
// nueva = subir el APK y actualizar estas variables, sin tocar código.
return [

    'android' => [
        // Última versión publicada. Se compara numéricamente en el cliente
        // (1.10.0 > 1.9.0), nunca de forma alfabética. Coincide con el APK
        // verificado que se distribuye desde /apk/ProyecTWin.apk.
        'version' => env('ANDROID_APP_VERSION', '1.3.0'),

        // Versión mínima soportada: con `enforce_minimum` activo, los clientes
        // por debajo reciben un aviso obligatorio (no posponible). La política
        // está desactivada por defecto.
        'min_version' => env('ANDROID_APP_MIN_VERSION', '1.0.0'),
        'enforce_minimum' => filter_var(env('ANDROID_APP_ENFORCE_MINIMUM', false), FILTER_VALIDATE_BOOLEAN),

        // Ruta same-origin del APK incluido en la imagen. El controlador la
        // convierte en URL absoluta HTTPS cuando APP_URL es HTTPS (Android
        // exige https://). Un valor vacío desactiva el anuncio; una URL
        // externa HTTPS también es válida.
        'download_url' => env('ANDROID_APP_DOWNLOAD_URL', '/apk/ProyecTWin.apk'),

        // Nota breve opcional que acompaña el aviso.
        'release_notes' => env('ANDROID_APP_RELEASE_NOTES', ''),
    ],

];
