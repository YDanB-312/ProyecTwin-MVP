<?php

namespace App\Support;

use App\Models\Apprentice;

// Genera el código único del perfil de aprendiz (AP-###).
class CodigoAprendiz
{
    public static function disponible(): string
    {
        $n = (int) Apprentice::max('id') + 1;
        do {
            $codigo = 'AP-' . str_pad((string) $n, 3, '0', STR_PAD_LEFT);
            $n++;
        } while (Apprentice::where('codigo', $codigo)->exists());

        return $codigo;
    }
}
