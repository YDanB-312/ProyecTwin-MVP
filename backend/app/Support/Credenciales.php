<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Support\Str;

// Generación del username institucional de las cuentas nuevas. El username se
// conserva como contrato de login de Android y clientes legacy; no se genera
// ninguna contraseña temporal.
class Credenciales
{
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
