<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

// Bloquea tokens de cuentas suspendidas (el login ya lo hace, pero los
// tokens emitidos antes de la suspensión seguirían válidos sin esto).
class CuentaActiva
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();
        if ($user && !$user->estado) {
            return response()->json(['message' => 'Cuenta suspendida. Contacta al administrador.'], 403);
        }

        // Defensa: si la cuenta deja de estar verificada con la sesión abierta,
        // se corta el acceso. 401 obliga al cliente a cerrar sesión y volver al
        // login (donde verá el motivo).
        if ($user && $user->rol !== 'admin' && !$user->estaVerificado()) {
            return response()->json([
                'message' => 'Tu cuenta ya no está verificada. Vuelve a iniciar sesión.',
            ], 401);
        }

        return $next($request);
    }
}
