<?php

namespace App\Http\Middleware;

use Illuminate\Foundation\Http\Middleware\VerifyCsrfToken as Middleware;

class VerifyCsrfToken extends Middleware
{
    /**
     * The URIs that should be excluded from CSRF verification.
     *
     * @var array<int, string>
     */
    protected $except = [
        // Endpoints públicos (demo de la landing): no dependen de sesión ni
        // de CSRF; así funcionan aunque el frontend no haya obtenido la cookie.
        'v1/public/*',
    ];
}
