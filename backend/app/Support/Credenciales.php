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

    // Username seguro para cuentas nuevas: fragmento legible del nombre y los
    // apellidos + parte aleatoria (random_int). Sin sufijos secuenciales.
    public static function usernameSeguro(string $nombre, string $apellido): string
    {
        $nombreLimpio = self::limpiar($nombre);
        $iniciales = self::letraMayuscula(substr($nombreLimpio, 0, 1))
            . (substr($nombreLimpio, 1, 1) ?: self::letraMinuscula());

        $palabras = array_values(array_filter(explode(' ', trim($apellido))));
        if ($palabras) {
            $primera = self::limpiar($palabras[0]);
            $ultima = self::limpiar($palabras[count($palabras) - 1]);
            $apellidos = self::letraMayuscula(substr($primera, 0, 1))
                . (substr($ultima, -1) ?: self::letraMinuscula());
        } else {
            $apellidos = self::letraMayuscula() . self::letraMinuscula();
        }

        for ($intento = 0; $intento < 10; $intento++) {
            $candidato = $iniciales . $apellidos . '_' . self::parteAleatoria(5);
            if (!GeneralUser::where('username', $candidato)->exists()) {
                return $candidato;
            }
        }

        // Respaldo improbable: más aleatoriedad si todas las combinaciones chocaron.
        do {
            $candidato = $iniciales . $apellidos . '_' . self::parteAleatoria(7);
        } while (GeneralUser::where('username', $candidato)->exists());

        return $candidato;
    }

    // Contraseña temporal legible y segura: X8K2-7P4M (se cambia al ingresar).
    public static function passwordTemporal(): string
    {
        return Str::upper(Str::random(4)) . '-' . Str::upper(Str::random(4));
    }

    // Letra mayúscula: la recibida o una aleatoria si viene vacía.
    private static function letraMayuscula(?string $letra = null): string
    {
        if ($letra === null || $letra === '') {
            return chr(random_int(65, 90));
        }
        return strtoupper($letra);
    }

    private static function letraMinuscula(): string
    {
        return chr(random_int(97, 122));
    }

    // Parte aleatoria de estructura variable: al menos una letra y un dígito.
    private static function parteAleatoria(int $largo): string
    {
        $alfabeto = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        do {
            $parte = '';
            for ($i = 0; $i < $largo; $i++) {
                $parte .= $alfabeto[random_int(0, strlen($alfabeto) - 1)];
            }
        } while (!preg_match('/[A-Za-z]/', $parte) || !preg_match('/[0-9]/', $parte));

        return $parte;
    }

    // Minúsculas sin tildes ni caracteres especiales (para usernames).
    private static function limpiar(string $texto): string
    {
        $texto = Str::ascii(mb_strtolower(trim($texto)));
        return preg_replace('/[^a-z0-9]/', '', $texto) ?? '';
    }
}
