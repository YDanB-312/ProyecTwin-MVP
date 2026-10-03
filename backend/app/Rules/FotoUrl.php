<?php

namespace App\Rules;

use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

// Acepta una URL http(s) o una imagen embebida (data URL) JPEG/PNG/WebP de
// máximo 1.5 MB. Evita blobs arbitrarios en la columna `foto_url`.
class FotoUrl implements ValidationRule
{
    private const MAX_BYTES = 1572864; // 1.5 MB

    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        if ($value === null || $value === '') {
            return;
        }

        if (!is_string($value) || strlen($value) > 3_000_000) {
            $fail('La foto no es válida.');
            return;
        }

        if (preg_match('#^data:image/(jpeg|png|webp);base64,(.+)$#', $value, $m)) {
            $bin = base64_decode($m[2], true);
            if ($bin === false || strlen($bin) > self::MAX_BYTES) {
                $fail('La foto debe ser JPEG, PNG o WebP y pesar máximo 1.5 MB.');
            }
            return;
        }

        if (!preg_match('#^https?://#i', $value) || filter_var($value, FILTER_VALIDATE_URL) === false) {
            $fail('La foto debe ser una URL http(s) o una imagen subida.');
        }
    }
}
