<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Laravel\Sanctum\TransientToken;

// Bloquea el uso de una cuenta que todavía no completó la activación por
// correo. Es defensa en profundidad: el login ya rechaza estas cuentas, pero
// también impide reutilizar una sesión o token previo.
class CuentaActivada
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();

        if (! $user || $user->email_verified_at !== null) {
            return $next($request);
        }

        if ($user->currentAccessToken() instanceof TransientToken && $request->hasSession()) {
            auth('web')->logoutCurrentDevice();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        }

        return response()->json([
            'message' => 'Tu cuenta está pendiente de activación. Revisa el correo de activación.',
            'code' => 'cuenta_no_activada',
        ], 403);
    }
}
