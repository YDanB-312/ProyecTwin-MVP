<?php

namespace App\Services;

use App\Models\GeneralUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Password;

// Envío del enlace de recuperación. Centraliza el manejo de fallos para que
// un error del mailer no convierta la respuesta genérica en un 500 ni deje un
// token válido sin entregar.
class RecuperacionPassword
{
    /**
     * @return array{enviado: bool, token: ?string, estado: string}
     */
    public function enviar(GeneralUser $usuario): array
    {
        $tokenPlano = null;

        try {
            $estado = Password::sendResetLink(
                ['correo' => $usuario->correo],
                function ($user, $token) use (&$tokenPlano) {
                    $tokenPlano = $token;
                    $user->sendPasswordResetNotification($token);
                }
            );
        } catch (\Throwable $e) {
            // El broker crea el token antes de invocar el callback. Si el
            // correo falla, se elimina para no dejar un enlace huérfano.
            if ($tokenPlano) {
                DB::table('password_reset_tokens')
                    ->where('email', strtolower((string) $usuario->correo))
                    ->delete();
            }

            report($e);

            return ['enviado' => false, 'token' => null, 'estado' => 'error_correo'];
        }

        return [
            'enviado' => $estado === Password::RESET_LINK_SENT || $estado === Password::RESET_THROTTLED,
            'token' => $tokenPlano,
            'estado' => $estado,
        ];
    }
}
