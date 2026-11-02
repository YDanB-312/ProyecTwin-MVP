<?php

namespace App\Support;

use App\Rules\PasswordNoComun;
use Illuminate\Validation\Rules\Password;

// Política única de contraseñas para alta, activación, cambio y recuperación.
// No exige mayúsculas, números ni símbolos: una frase larga con espacios es
// válida. El mínimo no se aplica al login de cuentas existentes.
class PasswordPolicy
{
    public const MIN_LENGTH = 12;

    public const MAX_LENGTH = 255;

    /**
     * @return array<int, mixed>
     */
    public static function reglas(bool $confirmada = true): array
    {
        $reglas = [
            'required',
            'string',
            Password::min(self::MIN_LENGTH)->max(self::MAX_LENGTH),
            new PasswordNoComun(),
        ];

        if ($confirmada) {
            $reglas[] = 'confirmed';
        }

        return $reglas;
    }

    /**
     * @return array<string, string>
     */
    public static function mensajes(): array
    {
        return [
            'password.required' => 'La nueva contraseña es obligatoria.',
            'password.string' => 'La nueva contraseña debe ser texto.',
            'password.min' => 'La contraseña debe tener al menos '.self::MIN_LENGTH.' caracteres.',
            'password.max' => 'La contraseña no puede superar los '.self::MAX_LENGTH.' caracteres.',
            'password.confirmed' => 'Las contraseñas no coinciden.',
        ];
    }
}
