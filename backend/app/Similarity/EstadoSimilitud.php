<?php

namespace App\Similarity;

/**
 * Estados del cálculo de similitud de una propuesta.
 *
 * `sin_referencias` es un resultado válido: no existen aprobadas en el ámbito,
 * no un 0 % de comparación. Solo `actualizado` y `sin_referencias` permiten
 * aprobar; el resto obliga a esperar o recalcular.
 */
final class EstadoSimilitud
{
    public const PENDIENTE = 'pendiente';
    public const EN_PROCESO = 'en_proceso';
    public const ACTUALIZADO = 'actualizado';
    public const SIN_REFERENCIAS = 'sin_referencias';
    public const ERROR = 'error';

    public static function aprobable(?string $estado): bool
    {
        return in_array($estado, [self::ACTUALIZADO, self::SIN_REFERENCIAS], true);
    }
}
