<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Laravel\Sanctum\TransientToken;

// Bloquea tokens/sesiones de cuentas suspendidas y valida la versión de las
// sesiones de cookie. La versión se guarda al iniciar sesión y se rota al
// desactivar la cuenta, lo que invalida también las sesiones con driver file.
class CuentaActiva
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();
        if (!$user) {
            return $next($request);
        }

        $esSesion = $user->currentAccessToken() instanceof TransientToken;

        if (!$user->estado) {
            if ($esSesion) {
                $this->invalidarSesion($request);
            }

            return response()->json(['message' => 'Cuenta suspendida. Contacta al administrador.'], 403);
        }

        if ($esSesion && !$this->versionSesionValida($request, $user)) {
            // Un login automático por "recordarme" crea una sesión nueva sin
            // marcador; se inicializa con la versión vigente. Las sesiones
            // antiguas con marcador desactualizado no pasan por aquí.
            if (auth('web')->viaRemember() && !$request->session()->has('auth_session_version')) {
                $request->session()->put(
                    'auth_session_version',
                    (string) $user->remember_token
                );
            } else {
                $this->invalidarSesion($request);

                return response()->json([
                    'message' => 'Sesión invalidada. Inicia sesión de nuevo.',
                ], 401);
            }
        }

        return $next($request);
    }

    private function versionSesionValida(Request $request, $user): bool
    {
        if (!$request->hasSession()) {
            return false;
        }

        $version = $request->session()->get('auth_session_version');

        return is_string($version)
            && hash_equals((string) $user->remember_token, $version);
    }

    private function invalidarSesion(Request $request): void
    {
        if (!$request->hasSession()) {
            return;
        }

        auth('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();
    }
}
