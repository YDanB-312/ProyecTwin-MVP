<?php

return [
    // Contraseña compartida por las cuentas de demostración. DatabaseSeeder
    // solo se ejecuta en local/testing/e2e; `demo:preparar` solo admite local
    // y la base proyectwin_mvp. Se puede sobreescribir con DEMO_PASSWORD.
    'password' => env('DEMO_PASSWORD', 'ProyecTwin.Demo#2026'),
];
