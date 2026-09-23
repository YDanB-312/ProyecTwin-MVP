<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

// Uso: ->middleware('rol:admin') o ->middleware('rol:admin,instructor').
// Compara contra GeneralUser.rol (valores: aprendiz, instructor, admin).
class RolMiddleware
{
    public function handle(Request $request, Closure $next, ...$roles)
    {
        $user = $request->user();
        if (!$user) {
            return response()->json(['message' => 'No autenticado.'], 401);
        }
        if (!empty($roles) && !in_array($user->rol, $roles, true)) {
            return response()->json(['message' => 'Sin permiso para esta acción.'], 403);
        }
        return $next($request);
    }
}
