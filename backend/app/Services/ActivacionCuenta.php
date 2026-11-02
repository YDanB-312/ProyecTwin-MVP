<?php

namespace App\Services;

use App\Models\AccountActivation;
use App\Models\GeneralUser;
use App\Notifications\ActivacionCuentaNotification;
use App\Support\Auditoria;
use App\Support\SesionesUsuario;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

// Activación de cuentas nuevas: token aleatorio, hash SHA-256, vencimiento y
// consumo atómico. El token plano solo existe en memoria y en el correo.
class ActivacionCuenta
{
    public const MENSAJE_INVALIDO = 'El enlace de activación es inválido o venció. Solicita uno nuevo al administrador.';

    public function crearToken(GeneralUser $usuario): string
    {
        $token = Str::random(64);

        AccountActivation::updateOrCreate(
            ['user_id' => $usuario->id],
            [
                'token_hash' => hash('sha256', $token),
                'expires_at' => now()->addMinutes((int) config('auth.activation_expire', 1440)),
                'sent_at' => null,
                'used_at' => null,
                'error' => null,
            ]
        );

        return $token;
    }

    /**
     * @return array{enviado: bool, error: ?string}
     */
    public function enviar(GeneralUser $usuario, string $token): array
    {
        try {
            $usuario->notify(new ActivacionCuentaNotification($token));
            $this->marcarEnvio($usuario, null);

            return ['enviado' => true, 'error' => null];
        } catch (\Throwable $e) {
            $error = mb_substr($e->getMessage(), 0, 500);
            $this->marcarEnvio($usuario, $error);

            return ['enviado' => false, 'error' => $error];
        }
    }

    /**
     * @return array{enviado: bool, error: ?string}
     */
    public function crearYEnviar(GeneralUser $usuario): array
    {
        return $this->enviar($usuario, $this->crearToken($usuario));
    }

    public function buscarVigente(string $token): ?AccountActivation
    {
        $activacion = AccountActivation::with('user')
            ->where('token_hash', hash('sha256', $token))
            ->first();

        if (! $activacion || $activacion->used_at || $activacion->expires_at->isPast()) {
            return null;
        }

        $usuario = $activacion->user;

        if (! $usuario || ! $usuario->estado || $usuario->email_verified_at) {
            return null;
        }

        return $activacion;
    }

    public function activar(string $token, string $password): GeneralUser
    {
        return DB::transaction(function () use ($token, $password) {
            $activacion = AccountActivation::where('token_hash', hash('sha256', $token))
                ->lockForUpdate()
                ->first();

            $usuario = $activacion?->user;

            if (
                ! $activacion
                || $activacion->used_at
                || $activacion->expires_at->isPast()
                || ! $usuario
                || ! $usuario->estado
                || $usuario->email_verified_at
            ) {
                throw ValidationException::withMessages([
                    'token' => self::MENSAJE_INVALIDO,
                ]);
            }

            if (Hash::check($password, $usuario->password)) {
                throw ValidationException::withMessages([
                    'password' => 'La nueva contraseña debe ser distinta a la actual.',
                ]);
            }

            $usuario->forceFill([
                'password' => Hash::make($password),
                'email_verified_at' => now(),
            ])->save();

            $activacion->forceFill([
                'used_at' => now(),
                'error' => null,
            ])->save();

            // Defensa en profundidad: una cuenta recién activada no debe
            // conservar tokens o sesiones de un estado anterior.
            SesionesUsuario::revocar($usuario->fresh());

            Auditoria::registrar('activar_cuenta', 'general_users', $usuario->id, [
                'correo' => $usuario->correo,
            ]);

            return $usuario->fresh();
        });
    }

    /**
     * @return array{enviado: bool, error: ?string}
     */
    public function reenviar(GeneralUser $usuario): array
    {
        if ($usuario->email_verified_at) {
            throw ValidationException::withMessages([
                'activacion' => 'La cuenta ya está activada.',
            ]);
        }

        if (! $usuario->estado) {
            throw ValidationException::withMessages([
                'activacion' => 'No se puede activar una cuenta suspendida.',
            ]);
        }

        return $this->crearYEnviar($usuario);
    }

    private function marcarEnvio(GeneralUser $usuario, ?string $error): void
    {
        AccountActivation::where('user_id', $usuario->id)->update([
            'sent_at' => $error === null ? now() : null,
            'error' => $error,
        ]);
    }
}
