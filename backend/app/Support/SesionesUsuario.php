<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Str;

// Revocación común de credenciales de acceso de una cuenta. Se usa al
// suspenderla y al eliminarla: los tokens Bearer, los enlaces de recuperación,
// las sesiones de servidor y la cookie persistente de "recordarme" dejan de
// ser válidos de inmediato.
class SesionesUsuario
{
    public static function revocar(GeneralUser $usuario): void
    {
        $usuario->tokens()->delete();

        DB::table('password_reset_tokens')
            ->where('email', strtolower((string) $usuario->correo))
            ->delete();

        if (Schema::hasTable('sessions')) {
            DB::table('sessions')->where('user_id', $usuario->id)->delete();
        }

        // Rota el token persistente: invalida la cookie "recordarme" y cambia
        // la versión de sesión que valida CuentaActiva (también con driver file).
        $usuario->forceFill(['remember_token' => Str::random(60)])->save();
    }
}
