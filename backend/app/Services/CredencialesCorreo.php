<?php

namespace App\Services;

use App\Models\GeneralUser;
use App\Notifications\CredencialesUsuario;

// Envío de credenciales iniciales al correo personal. El envío nunca rompe el
// alta: si el correo falla, se registra el error y el admin puede reenviar.
class CredencialesCorreo
{
    public function enviar(GeneralUser $usuario, string $temporal): bool
    {
        try {
            $usuario->notify(new CredencialesUsuario($usuario->username, $temporal));
            $usuario->update([
                'credenciales_enviadas_en' => now(),
                'credenciales_error' => null,
            ]);
            return true;
        } catch (\Throwable $e) {
            $usuario->update([
                'credenciales_error' => mb_substr($e->getMessage(), 0, 500),
            ]);
            return false;
        }
    }
}
