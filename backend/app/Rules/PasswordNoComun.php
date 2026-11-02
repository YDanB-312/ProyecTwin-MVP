<?php

namespace App\Rules;

use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

// Lista local y acotada de contraseñas comunes. No consulta servicios externos
// (evita depender de red en pruebas y no envía hashes a terceros).
class PasswordNoComun implements ValidationRule
{
    private const COMUNES = [
        '123456789012',
        '1234567890123',
        '12345678901234',
        'password1234',
        'password12345',
        'contrasena123',
        'contraseña123',
        'qwertyuiop12',
        'qwertyuiopas',
        'abcdefghijkl',
        'administrador',
        'administrator',
        'proyectwin123',
        'proyecttwin123',
        'sena12345678',
        'iloveyou1234',
        'welcome12345',
        'letmein12345',
        'monkey123456',
        'dragon123456',
    ];

    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        if (! is_string($value)) {
            return;
        }

        $normalizada = mb_strtolower(trim($value));
        $compacta = preg_replace('/[\s\-_]+/u', '', $normalizada) ?? $normalizada;

        if (in_array($normalizada, self::COMUNES, true) || in_array($compacta, self::COMUNES, true)) {
            $fail('La contraseña es demasiado común. Elige una frase más difícil de adivinar.');
            return;
        }

        // Series obvias o un único carácter repetido.
        if (preg_match('/^(.)\1{11,}$/u', $value) === 1) {
            $fail('La contraseña es demasiado débil. Elige una frase más difícil de adivinar.');
        }
    }
}
