<?php

// Reglas de identidad institucional (SENA): el registro valida contra el
// padrón y los correos deben ser de dominio institucional según el rol.
return [
    'dominios' => [
        'aprendiz' => ['soy.sena.edu.co', 'misena.edu.co', 'sena.edu.co'],
        'instructor' => ['sena.edu.co', 'misena.edu.co'],
        'admin' => ['sena.edu.co'],
    ],

    'activacion' => [
        // Vigencia del código de activación en minutos (72 h por defecto).
        'minutos' => (int) env('IDENTIDAD_ACTIVACION_MINUTOS', 4320),
        // En local/demo se devuelve el código en la respuesta para poder activar
        // sin correo; en producción solo viaja por email.
        'exponer_codigo' => env('IDENTIDAD_EXPONER_CODIGO', env('APP_ENV', 'production') === 'local'),
    ],

    // Intentos por minuto en los endpoints sensibles de identidad.
    'throttle' => [
        'login' => (int) env('IDENTIDAD_THROTTLE_LOGIN', 5),
        'activacion' => (int) env('IDENTIDAD_THROTTLE_ACTIVACION', 3),
        'recuperacion' => (int) env('IDENTIDAD_THROTTLE_RECUPERACION', 5),
    ],
];
