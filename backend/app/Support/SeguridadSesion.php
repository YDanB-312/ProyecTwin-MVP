<?php

namespace App\Support;

// Decide si la cookie de sesión debe llevar el atributo Secure.
//
// En producción, ni un valor ausente/inválido ni un false explícito pueden
// desactivar Secure. En local/testing se conserva false para permitir HTTP.
// Si no hay un booleano explícito, se deduce del esquema de APP_URL.
final class SeguridadSesion
{
    public static function cookieSegura(
        ?string $appUrl,
        mixed $explicito = null,
        ?string $entorno = null
    ): bool {
        $produccion = strtolower(trim((string) $entorno)) === 'production';

        if ($explicito !== null && $explicito !== '') {
            $valor = filter_var(
                $explicito,
                FILTER_VALIDATE_BOOLEAN,
                FILTER_NULL_ON_FAILURE
            );

            if ($valor !== null) {
                // En producción la política segura prevalece: false explícito
                // no puede desactivar Secure. En local/testing sí se respeta.
                return $valor || $produccion;
            }
        }

        if (str_starts_with(strtolower(trim((string) $appUrl)), 'https://')) {
            return true;
        }

        return $produccion;
    }
}
