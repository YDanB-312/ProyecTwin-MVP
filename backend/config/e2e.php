<?php

return [
    // Ruta del lock exclusivo del reseed E2E. El valor predeterminado debe
    // permanecer dentro de storage/framework; las pruebas pueden sobrescribir
    // esta clave en memoria con un archivo temporal.
    'reset_lock_path' => env('E2E_RESET_LOCK_PATH', storage_path('framework/e2e-reset.lock')),
];
