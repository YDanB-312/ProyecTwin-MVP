<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

// Con contraseña temporal pendiente, el usuario no puede usar el sistema: las
// rutas de sesión (me/password/logout) viven fuera de este grupo, así que aquí
// se bloquea todo lo demás hasta que defina su contraseña definitiva.
class CambioContrasenaObligatorio
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();

        if ($user && $user->must_change_password) {
            return response()->json([
                'message' => 'Debes cambiar tu contraseña temporal antes de continuar.',
                'must_change_password' => true,
            ], 403);
        }

        return $next($request);
    }
}
