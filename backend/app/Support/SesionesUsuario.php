<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Str;

// Revocación común de credenciales de acceso de una cuenta. Se usa al
// suspenderla, eliminarla, cambiar/restablecer la contraseña y cambiar el
// correo. Distingue entre revocar todo y revocar las demás sesiones para
// conservar la sesión actual cuando el propio usuario cambia su contraseña.
class SesionesUsuario
{
    public static function revocar(GeneralUser $usuario): void
    {
        self::revocarOtras($usuario);
    }

    public static function revocarOtras(
        GeneralUser $usuario,
        ?string $sessionIdActual = null,
        ?int $tokenActualId = null,
        ?string $correoAnterior = null
    ): void {
        $tokens = $usuario->tokens();

        if ($tokenActualId !== null) {
            $tokens->where('id', '!=', $tokenActualId);
        }

        $tokens->delete();

        self::borrarResetTokens($usuario->correo);
        self::borrarResetTokens($correoAnterior);

        if (Schema::hasTable('sessions')) {
            $sesiones = DB::table('sessions')->where('user_id', $usuario->id);

            if ($sessionIdActual !== null) {
                $sesiones->where('id', '!=', $sessionIdActual);
            }

            $sesiones->delete();
        }

        // Rota el token persistente: invalida la cookie "recordarme" y cambia
        // la versión de sesión que valida CuentaActiva (también con driver file).
        $nuevoRemember = Str::random(60);
        $usuario->forceFill(['remember_token' => $nuevoRemember])->save();

        // Si se conserva la sesión actual, su marcador debe apuntar a la nueva
        // versión; de lo contrario el siguiente request la invalidaría.
        if ($sessionIdActual !== null) {
            $request = request();

            if ($request->hasSession() && $request->session()->getId() === $sessionIdActual) {
                $request->session()->put('auth_session_version', (string) $nuevoRemember);
            }
        }
    }

    private static function borrarResetTokens(?string $correo): void
    {
        if ($correo === null || trim($correo) === '') {
            return;
        }

        DB::table('password_reset_tokens')
            ->where('email', strtolower(trim($correo)))
            ->delete();
    }
}
