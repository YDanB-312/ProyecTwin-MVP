<?php

namespace App\Http\Middleware;

use Illuminate\Auth\Middleware\Authenticate as Middleware;
use Illuminate\Http\Request;

class Authenticate extends Middleware
{
    /**
     * La API no tiene una ruta web `login`: el handler de excepciones responde
     * 401 JSON. Devolver `route('login')` aquí provocaba un 500 en cualquier
     * petición sin sesión que no enviara `Accept: application/json`.
     */
    protected function redirectTo(Request $request): ?string
    {
        return null;
    }
}
