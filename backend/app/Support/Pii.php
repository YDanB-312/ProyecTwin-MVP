<?php

namespace App\Support;

// Enmascarado de datos de identidad para la bitácora. Se aplica al escribir:
// los registros históricos no se reescriben.
class Pii
{
    private const CLAVES_CORREO = [
        'correo', 'email', 'correo_anterior', 'correo_nuevo',
        'email_anterior', 'email_nuevo',
    ];

    private const CLAVES_CREDENCIAL = [
        'password', 'password_actual', 'password_temporal', 'token',
        'token_hash', 'remember_token', 'secret', 'credenciales',
    ];

    // Devuelve el tipo y los últimos cuatro caracteres del documento.
    // Ejemplo: CC 12345678 -> "CC ****5678".
    public static function documento(?string $tipo, ?string $numero): ?string
    {
        $tipo = strtoupper(trim((string) $tipo));
        $numero = preg_replace('/[^0-9A-Za-z]/', '', (string) $numero);

        if ($tipo === '' && $numero === '') {
            return null;
        }

        // Con cuatro caracteres o menos no se muestra ningún dígito: de lo
        // contrario el "último cuatro" equivaldría al documento completo.
        $sufijo = strlen($numero) > 4 ? substr($numero, -4) : '****';

        return trim($tipo . ' ****' . $sufijo);
    }

    // Enmascara un correo conservando solo la inicial y el dominio.
    public static function correo(?string $correo): ?string
    {
        $correo = strtolower(trim((string) $correo));

        if ($correo === '' || ! str_contains($correo, '@')) {
            return $correo === '' ? null : '***';
        }

        [$local, $dominio] = explode('@', $correo, 2);

        if ($local === '' || $dominio === '') {
            return '***';
        }

        return mb_substr($local, 0, 1).'***@'.$dominio;
    }

    /**
     * Redacta recursivamente las claves sensibles del detalle de auditoría.
     *
     * @param  array<string, mixed>  $detalle
     * @return array<string, mixed>
     */
    public static function detalle(array $detalle): array
    {
        $salida = [];

        foreach ($detalle as $clave => $valor) {
            $normalizada = strtolower((string) $clave);

            if (is_array($valor)) {
                $salida[$clave] = self::detalle($valor);
            } elseif (in_array($normalizada, self::CLAVES_CORREO, true)) {
                $salida[$clave] = self::correo(is_string($valor) ? $valor : null);
            } elseif (in_array($normalizada, self::CLAVES_CREDENCIAL, true)) {
                $salida[$clave] = '[redactado]';
            } elseif ($normalizada === 'respuesta') {
                $salida[$clave] = '[texto omitido]';
            } else {
                $salida[$clave] = $valor;
            }
        }

        return $salida;
    }
}
