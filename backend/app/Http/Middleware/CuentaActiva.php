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
        return $next($request);
    }
}
