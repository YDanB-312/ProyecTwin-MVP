<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Support\Str;

// Generación de credenciales institucionales: username recordable a partir del
// nombre y la contraseña temporal segura del primer ingreso.
class Credenciales
{
    // "Carlos Martínez López" → "cmartinez" (si existe: cmartinez1, cmartinez2…).
    public static function username(string $nombre, string $apellido): string
    {
        $inicial = self::limpiar(substr(trim($nombre), 0, 1));
        $apellido = self::limpiar(explode(' ', trim($apellido))[0] ?? '');
        $base = ($inicial . $apellido) ?: 'usuario';

        $candidato = $base;
        $sufijo = 1;
        while (GeneralUser::where('username', $candidato)->exists()) {
            $candidato = $base . $sufijo;
            $sufijo++;
        }

        return $candidato;
    }

    // Contraseña temporal legible y segura: X8K2-7P4M (se cambia al ingresar).
    public static function passwordTemporal(): string
    {
        return Str::upper(Str::random(4)) . '-' . Str::upper(Str::random(4));
    }

    // Minúsculas sin tildes ni caracteres especiales (para usernames).
    private static function limpiar(string $texto): string
    {
        $texto = Str::ascii(mb_strtolower(trim($texto)));
        return preg_replace('/[^a-z0-9]/', '', $texto) ?? '';
    }
}
