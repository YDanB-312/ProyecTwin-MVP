<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

// Bloquea el resto de la API mientras la cuenta tenga una contraseña temporal
// (creada o reiniciada por el admin) sin cambiar. Las rutas de sesión
// (`auth/me`, `auth/password`, `auth/logout`) quedan fuera del bloqueo.
class PasswordCambiada
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();

        if ($user && $user->debe_cambiar_password) {
            return response()->json([
                'code' => 'password_pendiente',
                'message' => 'Debes cambiar tu contraseña temporal antes de continuar.',
            ], 403);
        }

        return $next($request);
    }
}
