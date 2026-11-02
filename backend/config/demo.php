<?php

return [
    // Contraseña compartida por las cuentas de demostración. Solo la usan
    // DatabaseSeeder y `php artisan demo:preparar`; ambos bloqueados fuera de
    // local/testing. Se puede sobreescribir con DEMO_PASSWORD en el .env local.
    'password' => env('DEMO_PASSWORD', 'ProyecTwin.Demo#2026'),
];
